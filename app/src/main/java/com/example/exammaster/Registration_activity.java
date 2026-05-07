package com.example.exammaster;
import android.content.Intent;
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
import android.os.Bundle;

public class Registration_activity extends AppCompatActivity {

    private EditText etPassword, etConfirmPassword, etName, etEmail;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        // 1. Логика кнопки регистрации
        btnRegister.setOnClickListener(v -> {
            String pass = etPassword.getText().toString();
            String confirmPass = etConfirmPassword.getText().toString();

            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            } else if (pass.equals(confirmPass)) {
                // Здесь логика регистрации (например, API запрос)
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Делаем "Log In" кликабельным
        setupLoginLink();
    }

    private void setupLoginLink() {
        String text = "Already Registered? Log In";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Переход на экран логина
                // Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                // startActivity(intent);
                Toast.makeText(Registration_activity.this, "Go to Login Screen", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.vidilenievsego)); // Оранжевый цвет
                ds.setUnderlineText(false); // Убираем подчеркивание
                ds.setFakeBoldText(true);
            }
        };

        // Указываем индексы для слова "Log In" (в строке это символы с 20 по 26)
        ss.setSpan(clickableSpan, 20, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginLink.setText(ss);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
}
}