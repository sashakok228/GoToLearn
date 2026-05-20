package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class Questions_activity extends AppCompatActivity {

    private RecyclerView rvDisciplines;
    private DisciplineAdapter adapter;
    private List<Discipline> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_activity);

        // 1. Находим RecyclerView (корзину)
        rvDisciplines = findViewById(R.id.rvDisciplines);

        // 2. Создаем данные для списка
        list = new ArrayList<>();
        list.add(new Discipline("Физика", 15, 20));
        list.add(new Discipline("Биология", 10, 30));
        list.add(new Discipline("Бурмалда", 6, 30));
        list.add(new Discipline("Химия", 5, 15));
        list.add(new Discipline("Математика", 22, 40));
        list.add(new Discipline("История", 8, 10));

        // 3. Настраиваем сетку в 2 колонки (GridLayoutManager)
        // Если карточек станет много, скролл появится автоматически!
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvDisciplines.setLayoutManager(layoutManager);

        // 4. Подключаем адаптер
        adapter = new DisciplineAdapter(list);
        rvDisciplines.setAdapter(adapter);

        // Логика кнопки "Создать дисциплину"
        Button btnCreate = findViewById(R.id.btnCreateQuestion);
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(Questions_activity.this, Disciplines_create_activity.class);
            startActivity(intent);
        });

        // Кнопки нижней панели
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, Home_page_activity.class));
            finish();
        });
    }
}