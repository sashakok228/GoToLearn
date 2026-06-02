package com.example.exammaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exammaster.network.ServerImageLoader;
import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.SubjectApi;
import com.example.exammaster.network.SubjectListCallback;
import com.example.exammaster.network.SubjectResponse;
import com.example.exammaster.network.UserProfileApi;
import com.example.exammaster.network.UserProfileCallback;
import com.example.exammaster.network.UserProfileResponse;

import java.util.ArrayList;
import java.util.List;

public class Home_page_activity extends AppCompatActivity {

    private ImageView ivProfileIcon;
    private TextView tvGreeting;
    private TextView tvStreakText;
    private TextView tvStreakNumber;
    private RecyclerView rvHomeDisciplines;

    private SessionManager sessionManager;
    private SubjectApi subjectApi;
    private UserProfileApi userProfileApi;
    private HomeDisciplineAdapter adapter;

    private final List<SubjectResponse> subjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_activity);

        /*
         * ВАЖНО:
         * В твоём home_page_activity.xml аватарка называется ivAvatar,
         * поэтому здесь используем R.id.ivAvatar, а не R.id.ivProfileIcon.
         */
        ivProfileIcon = findViewById(R.id.ivAvatar);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvStreakText = findViewById(R.id.tvStreakText);
        tvStreakNumber = findViewById(R.id.tvStreakNumber);
        rvHomeDisciplines = findViewById(R.id.rvHomeDisciplines);

        sessionManager = new SessionManager(this);
        subjectApi = new SubjectApi(this);
        userProfileApi = new UserProfileApi(this);

        setupRecyclerView();
        loadUserDataFromSession();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadUserDataFromSession();
        loadUserDataFromServer();
        loadSubjectsFromServer();
    }

    private void setupRecyclerView() {
        adapter = new HomeDisciplineAdapter(subjects, subject -> {
            Intent intent = new Intent(Home_page_activity.this, Prepare_training_activity.class);

            intent.putExtra("subjectId", subject.getId());
            intent.putExtra("subjectName", subject.getName());
            intent.putExtra("subjectImageUrl", subject.getImageUrl());
            intent.putExtra("MODE", "Test");

            startActivity(intent);
        });

        rvHomeDisciplines.setLayoutManager(new LinearLayoutManager(this));
        rvHomeDisciplines.setAdapter(adapter);
    }

    private void loadUserDataFromSession() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        String avatarUrl = sessionManager.getAvatarUrl();

        if (username == null || username.trim().isEmpty()) {
            username = makeUsernameFromEmail(email);
        }

        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }

        if (tvGreeting != null) {
            tvGreeting.setText("Привет, " + shortenUsername(username) + "!");        }

        if (ivProfileIcon != null) {
            ServerImageLoader.load(
                    ivProfileIcon,
                    avatarUrl,
                    R.drawable.ic_profile
            );
        }

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int streak = sharedPref.getInt("userStreak", 8);

        if (tvStreakText != null) {
            tvStreakText.setText(streak + " Дней стрик");
        }

        if (tvStreakNumber != null) {
            tvStreakNumber.setText(String.valueOf(streak));
        }
    }

    private void loadUserDataFromServer() {
        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            return;
        }

        userProfileApi.getMyProfile(token, new UserProfileCallback() {
            @Override
            public void onSuccess(UserProfileResponse profile) {
                sessionManager.saveProfile(profile);
                loadUserDataFromSession();
            }

            @Override
            public void onError(String errorMessage) {
                if (AuthRedirector.isUnauthorizedError(errorMessage)) {
                    AuthRedirector.logoutToSignIn(Home_page_activity.this);
                }
            }
        });
    }

    private String makeUsernameFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }

        String cleanEmail = email.trim();
        int atIndex = cleanEmail.indexOf("@");

        if (atIndex > 0) {
            return cleanEmail.substring(0, atIndex);
        }

        return cleanEmail;
    }

    private void loadSubjectsFromServer() {
        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            subjects.clear();

            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }

            Toast.makeText(
                    this,
                    "Нет токена. Сначала войдите в аккаунт",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        subjectApi.getSubjects(token, new SubjectListCallback() {
            @Override
            public void onSuccess(List<SubjectResponse> loadedSubjects) {
                subjects.clear();

                if (loadedSubjects != null) {
                    subjects.addAll(loadedSubjects);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                if (AuthRedirector.isUnauthorizedError(errorMessage)) {
                    AuthRedirector.logoutToSignIn(Home_page_activity.this);
                    return;
                }

                Toast.makeText(
                        Home_page_activity.this,
                        "Ошибка загрузки дисциплин: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private boolean isBadToken(String token) {
        return token == null
                || token.trim().isEmpty()
                || token.trim().equalsIgnoreCase("null");
    }

    private void setupBottomNavigation() {
        View navProfile = findViewById(R.id.navProfile);

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Home_page_activity.this, Profile_activity.class);
                startActivity(intent);
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);

        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(Home_page_activity.this, Disciplines_activity.class);
                startActivity(intent);
            });
        }

        View navHome = findViewById(R.id.navHome);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                // Уже на главном экране
            });
        }
    }

    private interface OnSubjectClickListener {
        void onSubjectClick(SubjectResponse subject);
    }
    private String shortenUsername(String username) {
        if (username == null) {
            return "User";
        }

        String cleanUsername = username.trim();

        if (cleanUsername.length() <= 10) {
            return cleanUsername;
        }

        return cleanUsername.substring(0, 10) + "...";
    }
    private static class HomeDisciplineAdapter
            extends RecyclerView.Adapter<HomeDisciplineAdapter.HomeDisciplineViewHolder> {

        private final List<SubjectResponse> items;
        private final OnSubjectClickListener listener;

        public HomeDisciplineAdapter(List<SubjectResponse> items,
                                     OnSubjectClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public HomeDisciplineViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_home_discipline, parent, false);

            return new HomeDisciplineViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeDisciplineViewHolder holder,
                                     int position) {
            SubjectResponse subject = items.get(position);
            holder.bind(subject, listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class HomeDisciplineViewHolder extends RecyclerView.ViewHolder {

            private final ImageView ivDisciplineAvatar;
            private final TextView tvSubjectTitle;
            private final TextView tvQuestionCount;
            private final ImageButton btnPlay;

            public HomeDisciplineViewHolder(@NonNull View itemView) {
                super(itemView);

                ivDisciplineAvatar = itemView.findViewById(R.id.ivDisciplineAvatar);
                tvSubjectTitle = itemView.findViewById(R.id.tvSubjectTitle);
                tvQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
                btnPlay = itemView.findViewById(R.id.btnPlay);
            }

            public void bind(SubjectResponse subject,
                             OnSubjectClickListener listener) {
                String subjectName = subject.getName();

                if (subjectName == null || subjectName.trim().isEmpty()) {
                    subjectName = "Без названия";
                }

                tvSubjectTitle.setText(subjectName);
                tvQuestionCount.setText(formatQuestionCount(subject.getQuestionCount()));

                ServerImageLoader.load(
                        ivDisciplineAvatar,
                        subject.getImageUrl(),
                        R.drawable.ic_ticket
                );

                btnPlay.setOnClickListener(v -> listener.onSubjectClick(subject));
                itemView.setOnClickListener(v -> listener.onSubjectClick(subject));
            }

            private String formatQuestionCount(int count) {
                int lastTwo = count % 100;
                int last = count % 10;

                if (lastTwo >= 11 && lastTwo <= 14) {
                    return count + " вопросов";
                }

                if (last == 1) {
                    return count + " вопрос";
                }

                if (last >= 2 && last <= 4) {
                    return count + " вопроса";
                }

                return count + " вопросов";
            }
        }

    }
}