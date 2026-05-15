package com.example.exammaster; // Проверь название своего пакета

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

public class SignIn_activity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvForgotPassword, tvRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.sign_in_activity); // Убедись, что название XML совпадает (может быть activity_login)

        // 1. Инициализация элементов
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        // 2. Логика кнопки входа (Sign In)
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignIn();
            }
        });

        // 3. Переход на экран восстановления пароля
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn_activity.this, ForgotPassword_activity.class);
                startActivity(intent);
            }
        });

        // 4. Настройка кликабельной ссылки "Register" внизу
        setupRegisterLink();
    }

    private void handleSignIn() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
        } else {
            // Здесь будет логика проверки данных через базу или сервер
            Toast.makeText(this, "Success! Logging in...", Toast.LENGTH_SHORT).show();

            // ПЕРЕХОД НА ГЛАВНЫЙ ЭКРАН ПРИЛОЖЕНИЯ (если он уже создан)
            // Intent intent = new Intent(SignIn_activity.this, MainActivity.class);
            // startActivity(intent);
        }
    }

    private void setupRegisterLink() {
        String text = "Don’t have an account? Register";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Переход на экран регистрации
                Intent intent = new Intent(SignIn_activity.this, Registration_activity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                // Оранжевый цвет для слова "Register"
                ds.setColor(getResources().getColor(R.color.vidilenievsego));
                ds.setUnderlineText(false); // Убираем подчеркивание
                ds.setFakeBoldText(true);   // Делаем жирным
            }
        };

        // Указываем индексы для слова "Register" (начинается с 23 символа)
        ss.setSpan(clickableSpan, 23, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvRegisterLink.setText(ss);
        tvRegisterLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}