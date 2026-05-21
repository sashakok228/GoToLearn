package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Prepare_training_activity extends AppCompatActivity {

    private TextView btnEasy, btnNormal, btnHard, btnPractice;
    private Button btnStart;

    // Переменные для хранения выбора
    private TextView selectedButton = null;
    private String selectedDifficulty = "Normal"; // По умолчанию
    private String currentMode = "Definition";   // Режим (Definition или Test)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prepare_training_activity);

        // 1. Инициализация кнопок сложности
        btnEasy = findViewById(R.id.btnEasy);
        btnNormal = findViewById(R.id.btnNormal);
        btnHard = findViewById(R.id.btnHard);
        btnPractice = findViewById(R.id.btnPractice);
        btnStart = findViewById(R.id.btnStart);

        // Получаем режим (Определение или Тест) из предыдущего экрана
        // Если ничего не пришло, по умолчанию будет "Definition"
        if (getIntent().hasExtra("MODE")) {
            currentMode = getIntent().getStringExtra("MODE");
        }

        // 2. Создаем один слушатель для всех кнопок сложности
        View.OnClickListener difficultyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelection((TextView) v);
            }
        };

        btnEasy.setOnClickListener(difficultyListener);
        btnNormal.setOnClickListener(difficultyListener);
        btnHard.setOnClickListener(difficultyListener);
        btnPractice.setOnClickListener(difficultyListener);

        // Устанавливаем выбор по умолчанию на Normal
        updateSelection(btnNormal);

        // 3. Логика кнопки START
        btnStart.setOnClickListener(v -> {
            if (selectedDifficulty != null) {
                // Переходим на экран самой игры
                Intent intent = new Intent(Prepare_training_activity.this, Game_activity.class);

                // Передаем режим и выбранную сложность
                intent.putExtra("MODE", currentMode);
                intent.putExtra("DIFFICULTY", selectedDifficulty);

                startActivity(intent);
            } else {
                Toast.makeText(this, "Пожалуйста, выберите сложность", Toast.LENGTH_SHORT).show();
            }
        });

        // Кнопки нижней панели (если они есть в этом XML)
        setupNavigation();
    }

    private void updateSelection(TextView newSelected) {
        // Снимаем выделение с предыдущей кнопки
        if (selectedButton != null) {
            selectedButton.setSelected(false);
        }

        // Активируем новую кнопку
        newSelected.setSelected(true);
        selectedButton = newSelected;

        // Сохраняем текст выбранной сложности (Easy, Normal и т.д.)
        selectedDifficulty = newSelected.getText().toString();
    }

    private void setupNavigation() {
        if (findViewById(R.id.navHome) != null) {
            findViewById(R.id.navHome).setOnClickListener(v -> {
                startActivity(new Intent(this, Home_page_activity.class));
                finish();
            });
        }
    }
}