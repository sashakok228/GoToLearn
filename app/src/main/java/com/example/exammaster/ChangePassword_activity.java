package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.SimpleCallback;
import com.example.exammaster.network.UserProfileApi;

public class ChangePassword_activity extends AppCompatActivity {

    private EditText etNewPassword;
    private EditText etConfirmPassword;

    private SessionManager sessionManager;
    private UserProfileApi userProfileApi;

    private String oldPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword_activity);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        sessionManager = new SessionManager(this);
        userProfileApi = new UserProfileApi(this);

        readIntentData();
        setupButtons();
        setupNavigation();
    }

    private void readIntentData() {
        Intent intent = getIntent();

        String oldPasswordFromIntent = intent.getStringExtra("oldPassword");

        if (oldPasswordFromIntent != null) {
            oldPassword = oldPasswordFromIntent;
        }
    }

    private void setupButtons() {
        View btnSavePassword = findViewById(R.id.btnSavePassword);

        if (btnSavePassword != null) {
            btnSavePassword.setOnClickListener(v -> changePassword());
        }
    }

    private void changePassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Старый пароль не передан. Вернитесь в профиль и попробуйте снова",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Введите новый пароль");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Пароль должен быть минимум 6 символов");
            etNewPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Повторите новый пароль");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            etConfirmPassword.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            AuthRedirector.logoutToSignIn(this);
            return;
        }

        View btnSavePassword = findViewById(R.id.btnSavePassword);

        if (btnSavePassword != null) {
            btnSavePassword.setEnabled(false);
        }

        Toast.makeText(this, "Меняем пароль...", Toast.LENGTH_SHORT).show();

        userProfileApi.changePassword(oldPassword, newPassword, token, new SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                if (btnSavePassword != null) {
                    btnSavePassword.setEnabled(true);
                }

                Toast.makeText(
                        ChangePassword_activity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                etNewPassword.setText("");
                etConfirmPassword.setText("");

                Intent intent = new Intent(ChangePassword_activity.this, Profile_activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                if (btnSavePassword != null) {
                    btnSavePassword.setEnabled(true);
                }

                Toast.makeText(
                        ChangePassword_activity.this,
                        "Ошибка смены пароля: " + errorMessage,
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

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(ChangePassword_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);

        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(ChangePassword_activity.this, Disciplines_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ChangePassword_activity.this, Profile_activity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}