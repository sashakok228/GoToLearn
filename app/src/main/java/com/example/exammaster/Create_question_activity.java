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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.CreateQuestionRequest;
import com.example.exammaster.network.CreateSubjectRequest;
import com.example.exammaster.network.CreateTicketRequest;
import com.example.exammaster.network.ServerImageLoader;
import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.SubjectApi;
import com.example.exammaster.network.SubjectCallback;
import com.example.exammaster.network.SubjectResponse;
import com.example.exammaster.network.TicketApi;
import com.example.exammaster.network.TicketCallback;

import java.util.Collections;

public class Create_question_activity extends AppCompatActivity {

    private static final int REQUEST_PICK_IMAGE = 2001;

    private ImageView ivDisciplineAvatar;
    private TextView tvChangeAvatar;

    private EditText etDisciplineName;
    private EditText etQuestion;
    private EditText etAnswer;

    private TextView tvTitle;

    private Button btnSaveDisciplineName;
    private Button btnDeleteDiscipline;
    private Button btnCreate;
    private Button btnQuestionList;

    private SessionManager sessionManager;
    private SubjectApi subjectApi;
    private TicketApi ticketApi;

    private long subjectId = -1;
    private String subjectName = "Дисциплина";
    private String subjectImageUrl = "";

    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_question_activity);

        ivDisciplineAvatar = findViewById(R.id.ivDisciplineAvatar);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);

        etDisciplineName = findViewById(R.id.etDisciplineName);
        etQuestion = findViewById(R.id.etQuestion);
        etAnswer = findViewById(R.id.etAnswer);

        tvTitle = findViewById(R.id.tvTitle);

        btnSaveDisciplineName = findViewById(R.id.btnSaveDisciplineName);
        btnDeleteDiscipline = findViewById(R.id.btnDeleteDiscipline);
        btnCreate = findViewById(R.id.btnCreateQuestion);
        btnQuestionList = findViewById(R.id.btnQuestionList);

        sessionManager = new SessionManager(this);
        subjectApi = new SubjectApi(this);
        ticketApi = new TicketApi();

        readIntentData();
        setupScreen();
        setupButtons();
        setupNavigation();
    }

    private void readIntentData() {
        Intent intent = getIntent();

        subjectId = intent.getLongExtra("subjectId", -1);

        String nameFromIntent = intent.getStringExtra("subjectName");
        if (nameFromIntent != null && !nameFromIntent.trim().isEmpty()) {
            subjectName = nameFromIntent;
        }

        String imageUrlFromIntent = intent.getStringExtra("subjectImageUrl");
        if (imageUrlFromIntent != null && !imageUrlFromIntent.trim().isEmpty()) {
            subjectImageUrl = imageUrlFromIntent;
        }
    }

    private void setupScreen() {
        tvTitle.setText(subjectName);
        etDisciplineName.setText(subjectName);

        if (subjectImageUrl != null && !subjectImageUrl.trim().isEmpty()) {
            ServerImageLoader.load(
                    ivDisciplineAvatar,
                    subjectImageUrl,
                    R.drawable.ic_ticket
            );
        } else {
            ivDisciplineAvatar.setImageResource(R.drawable.ic_ticket);
        }
    }

    private void setupButtons() {
        btnSaveDisciplineName.setOnClickListener(v -> updateDisciplineName());
        btnDeleteDiscipline.setOnClickListener(v -> confirmDeleteDiscipline());
        btnCreate.setOnClickListener(v -> createQuestion());
        btnQuestionList.setOnClickListener(v -> openQuestionList());

        ivDisciplineAvatar.setOnClickListener(v -> openImagePicker());
        tvChangeAvatar.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

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

        try {
            getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }

        ivDisciplineAvatar.setImageURI(selectedImageUri);

        uploadNewDisciplineImage();
    }

    private void uploadNewDisciplineImage() {
        if (subjectId == -1) {
            Toast.makeText(this, "Ошибка: дисциплина не выбрана", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Фото не выбрано", Toast.LENGTH_LONG).show();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            logoutToSignIn();
            return;
        }

        tvChangeAvatar.setText("Загрузка фото...");

        subjectApi.uploadSubjectImage(
                subjectId,
                selectedImageUri,
                token,
                new SubjectCallback() {
                    @Override
                    public void onSuccess(SubjectResponse response) {
                        tvChangeAvatar.setText("Сменить фото");

                        subjectImageUrl = response.getImageUrl();

                        Toast.makeText(
                                Create_question_activity.this,
                                "Фото дисциплины обновлено",
                                Toast.LENGTH_SHORT
                        ).show();

                        setResult(RESULT_OK);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        tvChangeAvatar.setText("Сменить фото");

                        Toast.makeText(
                                Create_question_activity.this,
                                "Ошибка загрузки фото: " + errorMessage,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void openQuestionList() {
        if (subjectId == -1) {
            Toast.makeText(this, "Ошибка: дисциплина не выбрана", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(Create_question_activity.this, Questions_activity.class);
        intent.putExtra("subjectId", subjectId);
        intent.putExtra("subjectName", subjectName);
        startActivity(intent);
    }

    private void updateDisciplineName() {
        if (subjectId == -1) {
            Toast.makeText(this, "Ошибка: дисциплина не выбрана", Toast.LENGTH_LONG).show();
            return;
        }

        String newName = etDisciplineName.getText().toString().trim();

        if (newName.isEmpty()) {
            etDisciplineName.setError("Введите название дисциплины");
            etDisciplineName.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            logoutToSignIn();
            return;
        }

        btnSaveDisciplineName.setEnabled(false);
        btnSaveDisciplineName.setText("Сохранение...");

        CreateSubjectRequest request = new CreateSubjectRequest(
                newName,
                "Дисциплина: " + newName
        );

        subjectApi.updateSubject(subjectId, request, token, new SubjectCallback() {
            @Override
            public void onSuccess(SubjectResponse response) {
                btnSaveDisciplineName.setEnabled(true);
                btnSaveDisciplineName.setText("Сохранить");

                subjectName = response.getName();
                subjectImageUrl = response.getImageUrl();

                tvTitle.setText(subjectName);
                etDisciplineName.setText(subjectName);

                Toast.makeText(
                        Create_question_activity.this,
                        "Название дисциплины изменено",
                        Toast.LENGTH_SHORT
                ).show();

                setResult(RESULT_OK);
            }

            @Override
            public void onError(String errorMessage) {
                btnSaveDisciplineName.setEnabled(true);
                btnSaveDisciplineName.setText("Сохранить");

                Toast.makeText(
                        Create_question_activity.this,
                        "Ошибка изменения названия: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void confirmDeleteDiscipline() {
        if (subjectId == -1) {
            Toast.makeText(this, "Ошибка: дисциплина не выбрана", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Удалить дисциплину?")
                .setMessage(
                        "Будет удалена дисциплина \"" + subjectName + "\" и все её вопросы. " +
                                "Это действие нельзя отменить."
                )
                .setPositiveButton("Удалить", (dialog, which) -> deleteDiscipline())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteDiscipline() {
        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            logoutToSignIn();
            return;
        }

        btnDeleteDiscipline.setEnabled(false);
        btnDeleteDiscipline.setText("Удаление...");

        subjectApi.deleteSubject(subjectId, token, new SubjectCallback() {
            @Override
            public void onSuccess(SubjectResponse response) {
                Toast.makeText(
                        Create_question_activity.this,
                        "Дисциплина удалена",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(Create_question_activity.this, Disciplines_activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                btnDeleteDiscipline.setEnabled(true);
                btnDeleteDiscipline.setText("Удалить");

                Toast.makeText(
                        Create_question_activity.this,
                        "Ошибка удаления дисциплины: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
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

        if (isBadToken(token)) {
            logoutToSignIn();
            return;
        }

        btnCreate.setEnabled(false);
        btnCreate.setText("Создание...");

        CreateQuestionRequest question = new CreateQuestionRequest(
                1,
                questionText,
                correctAnswer
        );

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
                btnCreate.setText("Создать вопрос");

                Toast.makeText(
                        Create_question_activity.this,
                        "Вопрос добавлен",
                        Toast.LENGTH_SHORT
                ).show();

                etQuestion.setText("");
                etAnswer.setText("");
            }

            @Override
            public void onError(String errorMessage) {
                btnCreate.setEnabled(true);
                btnCreate.setText("Создать вопрос");

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

    private boolean isBadToken(String token) {
        return token == null
                || token.trim().isEmpty()
                || token.trim().equalsIgnoreCase("null");
    }

    private void logoutToSignIn() {
        sessionManager.clear();

        Intent intent = new Intent(Create_question_activity.this, SignIn_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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