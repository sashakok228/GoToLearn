package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.AuthApi;
import com.example.exammaster.network.AuthCallback;
import com.example.exammaster.network.AuthResponse;
import com.example.exammaster.network.LoginRequest;
import com.example.exammaster.network.SessionManager;

public class SignIn_activity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvForgotPassword, tvRegisterLink;

    private AuthApi authApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.sign_in_activity);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        authApi = new AuthApi();
        sessionManager = new SessionManager(this);

        btnSignIn.setOnClickListener(v -> handleSignIn());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn_activity.this, ForgotPassword_activity.class);
            startActivity(intent);
        });

        setupRegisterLink();
    }

    private void handleSignIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите почту и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSignIn.setEnabled(false);

        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request, new AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                btnSignIn.setEnabled(true);

                sessionManager.saveAuth(response);

                Toast.makeText(SignIn_activity.this,
                        "Вход выполнен",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignIn_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                btnSignIn.setEnabled(true);

                Toast.makeText(SignIn_activity.this,
                        "Ошибка входа: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRegisterLink() {
        String text = "Нет аккаунта? Зарегистрироваться";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignIn_activity.this, Registration_activity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.vidilenievsego));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        ss.setSpan(clickableSpan, 14, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRegisterLink.setText(ss);
        tvRegisterLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}