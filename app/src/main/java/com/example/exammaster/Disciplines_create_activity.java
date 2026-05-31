package com.example.exammaster;

import android.content.Intent;
import android.net.Uri;
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

    private static final int REQUEST_PICK_IMAGE = 1001;

    private EditText etDisciplineName;
    private ImageView ivDisciplineAvatar;
    private TextView tvSelectAvatar;
    private Button btnCreate;

    private SessionManager sessionManager;
    private SubjectApi subjectApi;

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disciplines_create_activity);

        etDisciplineName = findViewById(R.id.etDisciplineName);
        ivDisciplineAvatar = findViewById(R.id.ivDisciplineAvatar);
        tvSelectAvatar = findViewById(R.id.tvSelectAvatar);
        btnCreate = findViewById(R.id.btnCreate);

        sessionManager = new SessionManager(this);
        subjectApi = new SubjectApi(this);

        setupAvatarClick();
        setupCreateButton();
        setupNavigation();
    }

    private void setupAvatarClick() {
        View.OnClickListener listener = v -> openImagePicker();

        ivDisciplineAvatar.setOnClickListener(listener);
        tvSelectAvatar.setOnClickListener(listener);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        /*
         * Даём приложению право читать выбранное изображение.
         * FLAG_GRANT_PERSISTABLE_URI_PERMISSION нужен, чтобы доступ к фото
         * не пропал после перезапуска приложения.
         */
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_PICK_IMAGE) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        if (data == null) {
            return;
        }

        Uri imageUri = data.getData();

        if (imageUri == null) {
            return;
        }

        selectedImageUri = imageUri;

        /*
         * ВАЖНО:
         * Здесь нельзя передавать переменную takeFlags, из-за неё Android Studio
         * может выдавать ошибку.
         *
         * Передаём напрямую Intent.FLAG_GRANT_READ_URI_PERMISSION.
         */
        try {
            getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
            /*
             * Если Android не дал постоянное разрешение, это не критично.
             * Фото всё равно можно показать сейчас и попытаться отправить на сервер.
             */
        }

        ivDisciplineAvatar.setImageURI(selectedImageUri);
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

        if (isBadToken(token)) {
            Toast.makeText(
                    this,
                    "Нет токена. Войдите заново",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(
                    Disciplines_create_activity.this,
                    SignIn_activity.class
            );

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
                /*
                 * Сначала создаём дисциплину.
                 * Если пользователь выбрал фото, после создания отдельно
                 * отправляем фото на сервер по id созданной дисциплины.
                 */
                if (selectedImageUri != null && response.getId() > 0) {
                    uploadImage(response, token);
                } else {
                    finishSuccess(response);
                }
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

    private void uploadImage(SubjectResponse createdSubject, String token) {
        btnCreate.setText("Загрузка фото...");

        subjectApi.uploadSubjectImage(
                createdSubject.getId(),
                selectedImageUri,
                token,
                new SubjectCallback() {
                    @Override
                    public void onSuccess(SubjectResponse response) {
                        finishSuccess(response);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        btnCreate.setEnabled(true);
                        btnCreate.setText("Создать дисциплину");

                        Toast.makeText(
                                Disciplines_create_activity.this,
                                "Дисциплина создана, но фото не загрузилось: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();

                        finishSuccess(createdSubject);
                    }
                }
        );
    }

    private void finishSuccess(SubjectResponse response) {
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

    private boolean isBadToken(String token) {
        return token == null
                || token.trim().isEmpty()
                || token.trim().equalsIgnoreCase("null");
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Disciplines_create_activity.this,
                        Home_page_activity.class
                );

                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Disciplines_create_activity.this,
                        Profile_activity.class
                );

                startActivity(intent);
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);

        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(
                        Disciplines_create_activity.this,
                        Disciplines_activity.class
                );

                startActivity(intent);
                finish();
            });
        }
    }
}