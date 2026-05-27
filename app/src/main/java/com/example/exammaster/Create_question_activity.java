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
import com.example.exammaster.network.CreateTicketRequest;
import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.TicketApi;
import com.example.exammaster.network.TicketCallback;

import java.util.Collections;

public class Create_question_activity extends AppCompatActivity {

    private EditText etQuestion;
    private EditText etAnswer;
    private TextView tvTitle;
    private Button btnCreate;

    private SessionManager sessionManager;
    private TicketApi ticketApi;

    private long subjectId = -1;
    private String subjectName = "Дисциплина";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_question_activity);

        etQuestion = findViewById(R.id.etQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        tvTitle = findViewById(R.id.tvTitle);
        btnCreate = findViewById(R.id.btnCreateQuestion);

        sessionManager = new SessionManager(this);
        ticketApi = new TicketApi();

        readIntentData();
        setupTitle();
        setupCreateButton();
        setupNavigation();
    }

    private void readIntentData() {
        Intent intent = getIntent();

        subjectId = intent.getLongExtra("subjectId", -1);
        String nameFromIntent = intent.getStringExtra("subjectName");

        if (nameFromIntent != null && !nameFromIntent.trim().isEmpty()) {
            subjectName = nameFromIntent;
        }
    }

    private void setupTitle() {
        tvTitle.setText(subjectName);
    }

    private void setupCreateButton() {
        btnCreate.setOnClickListener(v -> createQuestion());
    }

    private void createQuestion() {
        if (subjectId == -1) {
            Toast.makeText(this, "Ошибка: дисциплина не выбрана", Toast.LENGTH_LONG).show();
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

        if (token == null || token.trim().isEmpty() || token.trim().equalsIgnoreCase("null")) {
            Toast.makeText(this, "Нет токена. Войдите заново", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Create_question_activity.this, SignIn_activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        btnCreate.setEnabled(false);
        btnCreate.setText("Создание...");

        CreateQuestionRequest question = new CreateQuestionRequest(
                1,
                questionText,
                correctAnswer
        );

        /*
         * Сервер создаёт вопросы через создание Ticket.
         * Поэтому создаём билет с одним вопросом внутри выбранной дисциплины.
         */
        CreateTicketRequest request = new CreateTicketRequest(
                subjectId,
                generateTicketNumber(),
                "Вопрос по дисциплине: " + subjectName,
                questionText,
                Collections.singletonList(question)
        );

        ticketApi.createTicket(request, token, new TicketCallback() {
            @Override
            public void onSuccess() {
                btnCreate.setEnabled(true);
                btnCreate.setText("Create question");

                Toast.makeText(
                        Create_question_activity.this,
                        "Вопрос добавлен в дисциплину",
                        Toast.LENGTH_SHORT
                ).show();

                /*
                 * Остаёмся на этом же экране,
                 * просто очищаем поля для следующего вопроса.
                 */
                etQuestion.setText("");
                etAnswer.setText("");

                etQuestion.requestFocus();
            }

            @Override
            public void onError(String errorMessage) {
                btnCreate.setEnabled(true);
                btnCreate.setText("Create question");

                Toast.makeText(
                        Create_question_activity.this,
                        "Ошибка создания вопроса: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private int generateTicketNumber() {
        return (int) (System.currentTimeMillis() % 1_000_000);
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(Create_question_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Create_question_activity.this, Profile_activity.class);
                startActivity(intent);
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);
        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(Create_question_activity.this, Disciplines_activity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}