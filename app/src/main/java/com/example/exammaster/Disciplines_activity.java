package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.SubjectApi;
import com.example.exammaster.network.SubjectListCallback;
import com.example.exammaster.network.SubjectResponse;

import java.util.ArrayList;
import java.util.List;

public class Disciplines_activity extends AppCompatActivity {

    private RecyclerView rvDisciplines;
    private Button btnCreateDiscipline;

    private SessionManager sessionManager;
    private SubjectApi subjectApi;
    private DisciplineAdapter adapter;

    private final List<SubjectResponse> disciplineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disciplines_activity);

        rvDisciplines = findViewById(R.id.rvDisciplines);
        btnCreateDiscipline = findViewById(R.id.btnCreateDiscipline);

        sessionManager = new SessionManager(this);
        subjectApi = new SubjectApi();

        setupRecyclerView();
        setupCreateButton();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDisciplines();
    }

    private void setupRecyclerView() {
        rvDisciplines.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DisciplineAdapter(disciplineList, new OnDisciplineActionListener() {
            @Override
            public void onOpen(SubjectResponse subject) {
                Toast.makeText(
                        Disciplines_activity.this,
                        "Дисциплина: " + subject.getName(),
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onSettings(SubjectResponse subject) {
                /*
                 * При нажатии на шестерёнку открываем экран создания вопроса.
                 * Передаём id и название дисциплины.
                 */
                Intent intent = new Intent(Disciplines_activity.this, Create_question_activity.class);
                intent.putExtra("subjectId", subject.getId());
                intent.putExtra("subjectName", subject.getName());
                startActivity(intent);
            }
        });

        rvDisciplines.setAdapter(adapter);
    }

    private void setupCreateButton() {
        if (btnCreateDiscipline != null) {
            btnCreateDiscipline.setOnClickListener(v -> {
                Intent intent = new Intent(Disciplines_activity.this, Disciplines_create_activity.class);
                startActivity(intent);
            });
        }
    }

    private void loadDisciplines() {
        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty() || token.trim().equalsIgnoreCase("null")) {
            Toast.makeText(this, "Сначала войдите в аккаунт", Toast.LENGTH_SHORT).show();
            disciplineList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        subjectApi.getSubjects(token, new SubjectListCallback() {
            @Override
            public void onSuccess(List<SubjectResponse> subjects) {
                disciplineList.clear();

                if (subjects != null) {
                    disciplineList.addAll(subjects);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(
                        Disciplines_activity.this,
                        "Ошибка загрузки дисциплин: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(Disciplines_activity.this, Home_page_activity.class));
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Disciplines_activity.this, Profile_activity.class);
                startActivity(intent);
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);
        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                // Уже на экране дисциплин
            });
        }
    }

    private interface OnDisciplineActionListener {
        void onOpen(SubjectResponse subject);

        void onSettings(SubjectResponse subject);
    }

    private static class DisciplineAdapter extends RecyclerView.Adapter<DisciplineAdapter.DisciplineViewHolder> {

        private final List<SubjectResponse> items;
        private final OnDisciplineActionListener listener;

        public DisciplineAdapter(List<SubjectResponse> items, OnDisciplineActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public DisciplineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_discipline_settings, parent, false);
            return new DisciplineViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DisciplineViewHolder holder, int position) {
            holder.bind(items.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class DisciplineViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvSubjectTitle;
            private final TextView tvProgressRatio;
            private final ProgressBar pbProgress;
            private final ImageButton btnSettings;

            public DisciplineViewHolder(@NonNull View itemView) {
                super(itemView);

                tvSubjectTitle = itemView.findViewById(R.id.tvSubjectTitle);
                tvProgressRatio = itemView.findViewById(R.id.tvProgressRatio);
                pbProgress = itemView.findViewById(R.id.pbProgress);

                /*
                 * У тебя в item_discipline_settings.xml шестерёнка/кнопка
                 * по id называется btnPlay.
                 */
                btnSettings = itemView.findViewById(R.id.btnPlay);
            }

            public void bind(SubjectResponse subject, OnDisciplineActionListener listener) {
                String name = subject.getName();

                if (name == null || name.trim().isEmpty()) {
                    name = "Без названия";
                }

                tvSubjectTitle.setText(name);

                tvProgressRatio.setText("0/0");
                pbProgress.setMax(100);
                pbProgress.setProgress(0);

                itemView.setOnClickListener(v -> listener.onOpen(subject));

                /*
                 * Именно здесь обработка нажатия на шестерёнку.
                 */
                btnSettings.setOnClickListener(v -> listener.onSettings(subject));
            }
        }
    }
}