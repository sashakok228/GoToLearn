package com.example.exammaster;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Create_question_activity extends AppCompatActivity {

    private EditText etQuestion, etAnswer;
    private TextView tvDisciplineName;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_question_activity);

        etQuestion = findViewById(R.id.etQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        tvDisciplineName = findViewById(R.id.tvTitle);
        btnCreate = findViewById(R.id.btnCreateQuestion);

        // В будущем сюда можно передавать название дисциплины через Intent
        // String discipline = getIntent().getStringExtra("discipline_name");
        // tvDisciplineName.setText(discipline);

        btnCreate.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            String answer = etAnswer.getText().toString().trim();

            if (question.isEmpty() || answer.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            } else {
                // Логика сохранения вопроса в базу
                Toast.makeText(this, "Вопрос создан!", Toast.LENGTH_SHORT).show();
                finish(); // Возвращаемся к списку вопросов
            }
        });
    }
}