package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.CreateSubjectRequest;
import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.SubjectApi;
import com.example.exammaster.network.SubjectCallback;
import com.example.exammaster.network.SubjectResponse;

public class Disciplines_create_activity extends AppCompatActivity {

    private EditText etDisciplineName;
    private ImageView ivDisciplineAvatar;
    private TextView tvSelectAvatar;
    private Button btnCreate;

    private SessionManager sessionManager;
    private SubjectApi subjectApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disciplines_create_activity);

        etDisciplineName = findViewById(R.id.etDisciplineName);
        ivDisciplineAvatar = findViewById(R.id.ivDisciplineAvatar);
        tvSelectAvatar = findViewById(R.id.tvSelectAvatar);
        btnCreate = findViewById(R.id.btnCreate);

        sessionManager = new SessionManager(this);
        subjectApi = new SubjectApi();

        setupAvatarClick();
        setupCreateButton();
        setupNavigation();
    }

    private void setupAvatarClick() {
        View.OnClickListener selectAvatarListener = v -> {
            Toast.makeText(this, "Выбор иконки добавим позже", Toast.LENGTH_SHORT).show();
        };

        tvSelectAvatar.setOnClickListener(selectAvatarListener);
        ivDisciplineAvatar.setOnClickListener(selectAvatarListener);
    }

    private void setupCreateButton() {
        btnCreate.setOnClickListener(v -> createDiscipline());
    }

    private void createDiscipline() {
        String name = etDisciplineName.getText().toString().trim();

        if (name.isEmpty()) {
            etDisciplineName.setError("Введите название дисциплины");
            etDisciplineName.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty() || token.trim().equalsIgnoreCase("null")) {
            Toast.makeText(this, "Нет токена. Войдите в аккаунт заново", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Disciplines_create_activity.this, SignIn_activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        btnCreate.setEnabled(false);
        btnCreate.setText("Создание...");

        CreateSubjectRequest request = new CreateSubjectRequest(
                name,
                "Дисциплина: " + name
        );

        subjectApi.createSubject(request, token, new SubjectCallback() {
            @Override
            public void onSuccess(SubjectResponse response) {
                btnCreate.setEnabled(true);
                btnCreate.setText("Создать дисциплину");

                Toast.makeText(
                        Disciplines_create_activity.this,
                        "Дисциплина создана: " + response.getName(),
                        Toast.LENGTH_SHORT
                ).show();

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                btnCreate.setEnabled(true);
                btnCreate.setText("Создать дисциплину");

                Toast.makeText(
                        Disciplines_create_activity.this,
                        "Ошибка создания дисциплины: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(Disciplines_create_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Disciplines_create_activity.this, Profile_activity.class);
                startActivity(intent);
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);
        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(Disciplines_create_activity.this, Disciplines_activity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}