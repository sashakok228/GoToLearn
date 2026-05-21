package com.example.exammaster;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game_activity extends AppCompatActivity {

    // ИСПОЛЬЗУЕМ ПРАВИЛЬНЫЙ ID: tvSubjectName
    private TextView tvSubjectName, tvQuestionText;
    private LinearLayout containerInput;
    private Button btnCheck;

    private String mode; // "Definition" или "Test"
    private String difficulty; // "Easy", "Normal", "Hard"

    // Данные для примера
    private String correctAnswer = "живых";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        // Привязываем к правильному ID из хедера
        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        containerInput = findViewById(R.id.containerInput);
        btnCheck = findViewById(R.id.btnCheck);

        // Получаем данные из предыдущего экрана
        mode = getIntent().getStringExtra("MODE");
        difficulty = getIntent().getStringExtra("DIFFICULTY");

        // Устанавливаем текст в заголовок (например, "Вопрос 1/10")
        tvSubjectName.setText("Вопрос 1/10");

        setupGameLevel();
    }

    private void setupGameLevel() {
        containerInput.removeAllViews();

        if ("Definition".equals(mode)) {
            renderDefinitionLogic();
        } else {
            renderTestLogic();
        }
    }

    // --- ЛОГИКА 1: ОПРЕДЕЛЕНИЯ (Слова пропадают) ---
    private void renderDefinitionLogic() {
        if ("Easy".equals(difficulty)) {
            tvQuestionText.setText("Биология — это наука о ____ существах.");
            // Слова отображаются снизу как кнопки
            addWordChoice("живых");
            addWordChoice("неживых");
        }
        else if ("Normal".equals(difficulty)) {
            tvQuestionText.setText("Биология — это наука о ____ существах.");
            // Нужно дописать слово самому
            addInputField("Введите пропущенное слово", false);
        }
        else if ("Hard".equals(difficulty)) {
            tvQuestionText.setText("Напишите полное определение Биологии:");
            // Нужно написать всё определение полностью
            addInputField("Введите полное определение...", true);
        }

        btnCheck.setOnClickListener(v -> {
            Toast.makeText(this, "Проверка ответа...", Toast.LENGTH_SHORT).show();
        });
    }

    // --- ЛОГИКА 2: ТЕСТЫ (Выбор варианта) ---
    private void renderTestLogic() {
        tvQuestionText.setText("Какое свойство характерно для всех живых организмов?");

        List<String> answers = new ArrayList<>();
        answers.add("Обмен веществ"); // Правильный
        answers.add("Способность к полёту"); // Ложный

        if (!"Easy".equals(difficulty)) {
            answers.add("Наличие металлов"); // +1 ложный для Normal
        }
        if ("Hard".equals(difficulty)) {
            answers.add("Абсолютная неподвижность"); // + еще ложные для Hard
            answers.add("Питание только солнечным светом");
        }

        Collections.shuffle(answers); // Перемешиваем кнопки

        for (String answer : answers) {
            addAnswerButton(answer);
        }

        // В режиме теста кнопка "Check" не нужна, проверяем сразу по нажатию на вариант
        btnCheck.setVisibility(View.GONE);
    }

    // --- МЕТОДЫ ОТРИСОВКИ ---

    private void addWordChoice(String word) {
        Button btn = new Button(this);
        btn.setText(word);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.rounded_input);
        btn.setOnClickListener(v -> {
            String current = tvQuestionText.getText().toString();
            tvQuestionText.setText(current.replace("____", word));
        });
        containerInput.addView(btn);
    }

    private void addInputField(String hint, boolean multiLine) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setGravity(multiLine ? Gravity.TOP : Gravity.CENTER);
        et.setBackgroundResource(R.drawable.rounded_input);
        et.setPadding(40, 40, 40, 40);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                multiLine ? 400 : LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 40);
        et.setLayoutParams(lp);

        containerInput.addView(et);
    }

    private void addAnswerButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.rounded_input);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 150);
        lp.setMargins(0, 0, 0, 30);
        btn.setLayoutParams(lp);

        btn.setOnClickListener(v -> {
            if (text.equals("Обмен веществ")) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.GREEN));
                Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
                Toast.makeText(this, "Неправильно", Toast.LENGTH_SHORT).show();
            }
        });
        containerInput.addView(btn);
    }
}