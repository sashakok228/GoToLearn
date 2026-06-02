package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.PasswordRecoveryApi;
import com.example.exammaster.network.SimpleCallback;

public class ForgotPassword_activity extends AppCompatActivity {

    private EditText etEmail;
    private View btnSendPassword;
    private View tvBackToLogin;

    private PasswordRecoveryApi passwordRecoveryApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpassword_activity);

        etEmail = findViewById(R.id.etEmail);
        btnSendPassword = findViewById(R.id.btnSendPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        passwordRecoveryApi = new PasswordRecoveryApi();

        setupButtons();
    }

    private void setupButtons() {
        if (btnSendPassword != null) {
            btnSendPassword.setOnClickListener(v -> sendNewPassword());
        }

        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(ForgotPassword_activity.this, SignIn_activity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void sendNewPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Введите почту");
            etEmail.requestFocus();
            return;
        }

        if (!email.contains("@")) {
            etEmail.setError("Некорректная почта");
            etEmail.requestFocus();
            return;
        }

        btnSendPassword.setEnabled(false);

        Toast.makeText(
                this,
                "Отправляем новый пароль...",
                Toast.LENGTH_SHORT
        ).show();

        passwordRecoveryApi.sendNewPassword(email, new SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                btnSendPassword.setEnabled(true);

                Toast.makeText(
                        ForgotPassword_activity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                Intent intent = new Intent(ForgotPassword_activity.this, SignIn_activity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                btnSendPassword.setEnabled(true);

                Toast.makeText(
                        ForgotPassword_activity.this,
                        "Ошибка восстановления пароля: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}