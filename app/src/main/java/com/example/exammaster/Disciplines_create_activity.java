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
public class Disciplines_create_activity extends AppCompatActivity {

    private EditText etDisciplineName;
    private ImageView ivDisciplineAvatar;
    private TextView tvSelectAvatar;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disciplines_create_activity);

        etDisciplineName = findViewById(R.id.etDisciplineName);
        ivDisciplineAvatar = findViewById(R.id.ivDisciplineAvatar);
        tvSelectAvatar = findViewById(R.id.tvSelectAvatar);
        btnCreate = findViewById(R.id.btnCreate);

        // Клик по тексту или картинке для выбора аватара
        View.OnClickListener selectAvatarListener = v -> {
            // Здесь в будущем можно открыть галерею или список иконок
            Toast.makeText(this, "Открыть выбор иконок", Toast.LENGTH_SHORT).show();
        };

        tvSelectAvatar.setOnClickListener(selectAvatarListener);
        ivDisciplineAvatar.setOnClickListener(selectAvatarListener);

        // Кнопка создания
        btnCreate.setOnClickListener(v -> {
            String name = etDisciplineName.getText().toString().trim();
            if (name.isEmpty()) {
                etDisciplineName.setError("Введите название");
            } else {
                Toast.makeText(this, "Дисциплина '" + name + "' создана!", Toast.LENGTH_SHORT).show();
                finish(); // Возвращаемся назад к списку
            }
        });
    }
}