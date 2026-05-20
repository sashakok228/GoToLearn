package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class Prepare_training_activity extends AppCompatActivity {

    private TextView btnEasy, btnNormal, btnHard, btnPractice;
    private TextView selectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prepare_training_activity);

        btnEasy = findViewById(R.id.btnEasy);
        btnNormal = findViewById(R.id.btnNormal);
        btnHard = findViewById(R.id.btnHard);
        btnPractice = findViewById(R.id.btnPractice);

        View.OnClickListener listener = v -> selectDifficulty((TextView) v);

        btnEasy.setOnClickListener(listener);
        btnNormal.setOnClickListener(listener);
        btnHard.setOnClickListener(listener);
        btnPractice.setOnClickListener(listener);

        // По умолчанию выбираем Normal (как в дизайне)
        selectDifficulty(btnNormal);

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            if (selectedButton != null) {
                // Переход к вопросам
                Toast.makeText(this, "Starting " + selectedButton.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectDifficulty(TextView button) {
        // Сбрасываем предыдущую кнопку
        if (selectedButton != null) {
            selectedButton.setSelected(false);
        }
        // Активируем новую
        button.setSelected(true);
        selectedButton = button;
    }
}