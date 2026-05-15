package com.example.exammaster; // Проверь, чтобы это совпадало с твоим названием пакета

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

public class Registration_activity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.registration_activity);

        // 1. Инициализация всех элементов по их ID из XML
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // 2. Логика кнопки регистрации
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // 3. Настройка кликабельной ссылки "Log In" внизу
        setupLoginLink();
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String confirmPass = etConfirmPassword.getText().toString();


        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.equals(confirmPass)) {

            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Registration_activity.this, SignIn_activity.class);
            startActivity(intent);

            finish();
        } else {
            // Если пароли НЕ совпали
            etConfirmPassword.setError("Passwords do not match!");
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupLoginLink() {
        String text = "Already Registered? Log In";
        SpannableString ss = new SpannableString(text);

        // Создаем кликабельную зону для слов "Log In"
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Переход на экран авторизации при клике на текст
                Intent intent = new Intent(Registration_activity.this, SignIn_activity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                // Устанавливаем цвет ссылки из твоей палитры (оранжевый)
                ds.setColor(getResources().getColor(R.color.vidilenievsego));
                ds.setUnderlineText(false); // Убираем подчеркивание
                ds.setFakeBoldText(true);   // Делаем жирным
            }
        };

        // Указываем индексы: "Log In" начинается с 20-го символа
        ss.setSpan(clickableSpan, 20, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginLink.setText(ss);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}