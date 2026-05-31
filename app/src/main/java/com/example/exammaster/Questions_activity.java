package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exammaster.network.QuestionApi;
import com.example.exammaster.network.QuestionCallback;
import com.example.exammaster.network.QuestionListCallback;
import com.example.exammaster.network.QuestionResponse;
import com.example.exammaster.network.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class Questions_activity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvEmpty;
    private RecyclerView rvQuestions;

    private SessionManager sessionManager;
    private QuestionApi questionApi;
    private QuestionsAdapter adapter;

    private final List<QuestionResponse> questions = new ArrayList<>();

    private long subjectId = -1;
    private String subjectName = "Дисциплина";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_activity);

        tvTitle = findViewById(R.id.tvTitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvQuestions = findViewById(R.id.rvQuestions);

        sessionManager = new SessionManager(this);
        questionApi = new QuestionApi();

        readIntentData();
        setupScreen();
        setupRecyclerView();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuestions();
    }

    private void readIntentData() {
        Intent intent = getIntent();

        subjectId = intent.getLongExtra("subjectId", -1);

        String nameFromIntent = intent.getStringExtra("subjectName");
        if (nameFromIntent != null && !nameFromIntent.trim().isEmpty()) {
            subjectName = nameFromIntent;
        }
    }

    private void setupScreen() {
        tvTitle.setText("Вопросы: " + subjectName);
    }

    private void setupRecyclerView() {
        adapter = new QuestionsAdapter(questions, new OnQuestionActionListener() {
            @Override
            public void onEdit(QuestionResponse question) {
                Intent intent = new Intent(Questions_activity.this, Edit_question_activity.class);
                intent.putExtra("questionId", question.getId());
                intent.putExtra("subjectId", subjectId);
                intent.putExtra("subjectName", subjectName);
                intent.putExtra("questionText", question.getQuestionText());
                intent.putExtra("correctAnswer", question.getCorrectAnswer());
                startActivity(intent);
            }

            @Override
            public void onDelete(QuestionResponse question) {
                confirmDeleteQuestion(question);
            }
        });

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(adapter);
    }

    private void loadQuestions() {
        if (subjectId == -1) {
            Toast.makeText(this, "Ошибка: дисциплина не выбрана", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            logoutToSignIn();
            return;
        }

        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText("Загрузка вопросов...");

        questionApi.getQuestionsBySubject(subjectId, token, new QuestionListCallback() {
            @Override
            public void onSuccess(List<QuestionResponse> loadedQuestions) {
                questions.clear();

                if (loadedQuestions != null) {
                    questions.addAll(loadedQuestions);
                }

                adapter.notifyDataSetChanged();

                if (questions.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("В этой дисциплине пока нет вопросов");
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Ошибка загрузки вопросов");

                Toast.makeText(
                        Questions_activity.this,
                        "Ошибка загрузки вопросов: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void confirmDeleteQuestion(QuestionResponse question) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить вопрос?")
                .setMessage("Вопрос будет удалён без возможности восстановления.")
                .setPositiveButton("Удалить", (dialog, which) -> deleteQuestion(question))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteQuestion(QuestionResponse question) {
        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            logoutToSignIn();
            return;
        }

        questionApi.deleteQuestion(question.getId(), token, new QuestionCallback() {
            @Override
            public void onSuccess(QuestionResponse deletedQuestion) {
                Toast.makeText(
                        Questions_activity.this,
                        "Вопрос удалён",
                        Toast.LENGTH_SHORT
                ).show();

                loadQuestions();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(
                        Questions_activity.this,
                        "Ошибка удаления вопроса: " + errorMessage,
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

    private void logoutToSignIn() {
        sessionManager.clear();

        Intent intent = new Intent(Questions_activity.this, SignIn_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(Questions_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Questions_activity.this, Profile_activity.class);
                startActivity(intent);
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);
        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(Questions_activity.this, Disciplines_activity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private interface OnQuestionActionListener {
        void onEdit(QuestionResponse question);

        void onDelete(QuestionResponse question);
    }

    private static class QuestionsAdapter
            extends RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder> {

        private final List<QuestionResponse> items;
        private final OnQuestionActionListener listener;

        public QuestionsAdapter(List<QuestionResponse> items,
                                OnQuestionActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question, parent, false);

            return new QuestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
            QuestionResponse question = items.get(position);
            holder.bind(question, listener, position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class QuestionViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvQuestionNumber;
            private final TextView tvQuestionText;
            private final TextView tvCorrectAnswer;
            private final Button btnEdit;
            private final Button btnDelete;

            public QuestionViewHolder(@NonNull View itemView) {
                super(itemView);

                tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
                tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
                tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
                btnEdit = itemView.findViewById(R.id.btnEditQuestion);
                btnDelete = itemView.findViewById(R.id.btnDeleteQuestion);
            }

            public void bind(QuestionResponse question,
                             OnQuestionActionListener listener,
                             int position) {

                tvQuestionNumber.setText("Вопрос №" + (position + 1));

                String questionText = question.getQuestionText();
                if (questionText == null || questionText.trim().isEmpty()) {
                    questionText = "Без текста вопроса";
                }

                String correctAnswer = question.getCorrectAnswer();
                if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
                    correctAnswer = "Ответ не указан";
                }

                tvQuestionText.setText(questionText);
                tvCorrectAnswer.setText("Ответ: " + correctAnswer);

                btnEdit.setOnClickListener(v -> listener.onEdit(question));
                btnDelete.setOnClickListener(v -> listener.onDelete(question));
            }
        }
    }
}