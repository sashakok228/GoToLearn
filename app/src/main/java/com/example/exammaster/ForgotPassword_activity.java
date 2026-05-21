package com.example.exammaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPassword_activity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnChangePassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Проверь, чтобы название XML-файла совпадало с твоим
        setContentView(R.layout.forgotpassword_activity);

        // 1. Инициализация элементов
        etEmail = findViewById(R.id.etEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // 2. Логика кнопки
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    // Если поле пустое
                    etEmail.setError("Введите ваш Email");
                    Toast.makeText(ForgotPassword_activity.this, "Пожалуйста, введите почту", Toast.LENGTH_SHORT).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // Если введен не Email (нет собачки или точки)
                    etEmail.setError("Введите корректный Email");
                } else {
                    // Если всё введено верно
                    Toast.makeText(ForgotPassword_activity.this, "Код отправлен на " + email, Toast.LENGTH_SHORT).show();

                    // ПЕРЕХОД на следующий экран (где 4 ячейки кода)
                    // Убедись, что файл называется именно confirmpassword_activity
                    Intent intent = new Intent(ForgotPassword_activity.this, ConfirmPassword_activity.class);
                    startActivity(intent);
                }
            }
        });
    }
}