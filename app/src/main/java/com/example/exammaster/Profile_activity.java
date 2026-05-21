package com.example.exammaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Profile_activity extends AppCompatActivity {

    private TextView tvGreeting;
    private EditText etName, etEmail, etOldPassword;
    private Button btnChangeData, btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Проверь, чтобы название XML-файла было именно таким
        setContentView(R.layout.profile_activity);

        // 1. Инициализация элементов
        tvGreeting = findViewById(R.id.tvGreeting);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etOldPassword = findViewById(R.id.etPassword); // Поле для старого пароля
        btnChangeData = findViewById(R.id.btnChangeDannie);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // 2. Загрузка текущих данных из памяти
        loadUserData();

        // 3. Логика кнопки "Сменить данные"
        btnChangeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewData();
            }
        });

        // 4. Логика кнопки "Сменить пароль"
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = etOldPassword.getText().toString();
                if (oldPass.isEmpty()) {
                    etOldPassword.setError("Введите старый пароль");
                } else {
                    // Переходим на экран создания НОВОГО пароля (который мы делали раньше)
                    Intent intent = new Intent(Profile_activity.this, ChangePassword_activity.class);
                    startActivity(intent);
                }
            }
        });

        // 5. Настройка нижней навигации
        setupNavigation();
    }

    private void loadUserData() {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sharedPref.getString("userName", "Alex");
        String email = sharedPref.getString("userEmail", "example@mail.com");

        tvGreeting.setText("Hi, " + name + "!");
        etName.setText(name);
        etEmail.setText(email);
    }

    private void saveNewData() {
        String newName = etName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Поля не могут быть пустыми", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем обновленные данные в память
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userName", newName);
        editor.putString("userEmail", newEmail);
        editor.apply();

        tvGreeting.setText("Hi, " + newName + "!");
        Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();
    }

    private void setupNavigation() {
        // Кнопка ДОМОЙ
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, Home_page_activity.class));
            finish();
        });

        // Кнопка БИБЛИОТЕКА (Дисциплины)
        findViewById(R.id.navLibrary).setOnClickListener(v -> {
            startActivity(new Intent(this, Disciplines_activity.class));
            finish();
        });
    }
}