package com.example.exammaster;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Home_page_activity extends AppCompatActivity {

    private TextView tvGreeting, tvStreakText, tvStreakNumber;
    private ProgressBar pbDiscipline1, pbDiscipline2;
    private ImageButton btnPlay1, btnPlay2;


    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page_activity); // Убедись, что XML называется так

        // 1. Инициализация элементов хедера (приветствие и стрик)
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStreakText = findViewById(R.id.tvStreakText);
        tvStreakNumber = findViewById(R.id.tvStreakNumber);

        // 2. Инициализация карточек дисциплин (прогресс и кнопки)
        // Если у тебя две разные карточки, ID у них должны отличаться (например pb1 и pb2)
        pbDiscipline1 = findViewById(R.id.pbDiscipline1);
        btnPlay1 = findViewById(R.id.btnPlay1);

        // 3. Загрузка данных пользователя
        loadUserData();

        // 4. Настройка прогресса (для примера)
        setupRecentDisciplines();

        // 5. Настройка нижней навигации
        setupBottomNavigation();
    }

    private void loadUserData() {
        // Достаем имя, сохраненное при регистрации
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sharedPref.getString("userName", "Alex");
        int streak = sharedPref.getInt("userStreak", 8);

        // Устанавливаем данные на экран
        tvGreeting.setText("Hi, " + name + "!");
        tvStreakText.setText(streak + " Дней стрик");
        tvStreakNumber.setText(String.valueOf(streak));
    }

    private void setupRecentDisciplines() {
        // Устанавливаем прогресс для первой карточки (например, 10 из 30)
        if (pbDiscipline1 != null) {
            pbDiscipline1.setMax(30);
            pbDiscipline1.setProgress(10);
        }

        // Логика кнопки Play в карточке
        if (btnPlay1 != null) {
            btnPlay1.setOnClickListener(v -> {
                Intent intent = new Intent(Home_page_activity.this, Prepare_training_activity.class);
                intent.putExtra("MODE", "Definition"); // Передаем режим обучения
                startActivity(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        // Кнопка ПРОФИЛЬ (центральная иконка)
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            // startActivity(intent);
        });

        // Кнопка ДИСЦИПЛИНЫ (правая иконка книги)
        findViewById(R.id.navLibrary).setOnClickListener(v -> {
            Intent intent = new Intent(Home_page_activity.this, Disciplines_activity.class);
            startActivity(intent);
        });

        // Иконка ДОМ (текущий экран) — обычно ничего не делает или скроллит вверх
        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Мы уже здесь
        });
    }
}