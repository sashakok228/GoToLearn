package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.CreateQuestionRequest;
import com.example.exammaster.network.QuestionApi;
import com.example.exammaster.network.QuestionCallback;
import com.example.exammaster.network.QuestionResponse;
import com.example.exammaster.network.SessionManager;

public class Edit_question_activity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText etQuestion;
    private EditText etAnswer;
    private Button btnSaveQuestion;

    private SessionManager sessionManager;
    private QuestionApi questionApi;

    private long questionId = -1;
    private long subjectId = -1;
    private String subjectName = "Дисциплина";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_question_activity);

        tvTitle = findViewById(R.id.tvTitle);
        etQuestion = findViewById(R.id.etQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        btnSaveQuestion = findViewById(R.id.btnSaveQuestion);

        sessionManager = new SessionManager(this);
        questionApi = new QuestionApi();

        readIntentData();
        setupScreen();
        setupButtons();
        setupNavigation();
    }

    private void readIntentData() {
        Intent intent = getIntent();

        questionId = intent.getLongExtra("questionId", -1);
        subjectId = intent.getLongExtra("subjectId", -1);

        String nameFromIntent = intent.getStringExtra("subjectName");
        if (nameFromIntent != null && !nameFromIntent.trim().isEmpty()) {
            subjectName = nameFromIntent;
        }

        String questionText = intent.getStringExtra("questionText");
        String correctAnswer = intent.getStringExtra("correctAnswer");

        if (questionText != null) {
            etQuestion.setText(questionText);
        }

        if (correctAnswer != null) {
            etAnswer.setText(correctAnswer);
        }
    }

    private void setupScreen() {
        tvTitle.setText("Редактировать вопрос");
    }

    private void setupButtons() {
        btnSaveQuestion.setOnClickListener(v -> saveQuestion());
    }

    private void saveQuestion() {
        if (questionId == -1) {
            Toast.makeText(this, "Ошибка: вопрос не выбран", Toast.LENGTH_LONG).show();
            return;
        }

        String questionText = etQuestion.getText().toString().trim();
        String correctAnswer = etAnswer.getText().toString().trim();

        if (questionText.isEmpty()) {
            etQuestion.setError("Введите вопрос");
            etQuestion.requestFocus();
            return;
        }

        if (correctAnswer.isEmpty()) {
            etAnswer.setError("Введите правильный ответ");
            etAnswer.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            logoutToSignIn();
            return;
        }

        btnSaveQuestion.setEnabled(false);
        btnSaveQuestion.setText("Сохранение...");

        CreateQuestionRequest request = new CreateQuestionRequest(
                1,
                questionText,
                correctAnswer
        );

        questionApi.updateQuestion(questionId, request, token, new QuestionCallback() {
            @Override
            public void onSuccess(QuestionResponse question) {
                btnSaveQuestion.setEnabled(true);
                btnSaveQuestion.setText("Сохранить");

                Toast.makeText(
                        Edit_question_activity.this,
                        "Вопрос изменён",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }

            @Override
            public void onError(String errorMessage) {
                btnSaveQuestion.setEnabled(true);
                btnSaveQuestion.setText("Сохранить");

                Toast.makeText(
                        Edit_question_activity.this,
                        "Ошибка изменения вопроса: " + errorMessage,
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

        Intent intent = new Intent(Edit_question_activity.this, SignIn_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(Edit_question_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Edit_question_activity.this, Profile_activity.class);
                startActivity(intent);
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);
        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(Edit_question_activity.this, Disciplines_activity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}