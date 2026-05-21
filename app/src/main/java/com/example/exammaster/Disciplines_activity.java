package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Disciplines_activity extends AppCompatActivity {

    private RecyclerView rvDisciplines;
    private DisciplineAdapter_play adapter; // Твоё новое название адаптера
    private List<Discipline_play> disciplineList; // Твоё новое название класса данных

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Проверь, что твой основной XML называется именно так
        setContentView(R.layout.disciplines_activity);

        // 1. Находим RecyclerView (список) на экране
        rvDisciplines = findViewById(R.id.rvDisciplines);

        // 2. Устанавливаем вертикальный менеджер (карточки друг под другом)
        rvDisciplines.setLayoutManager(new LinearLayoutManager(this));

        // 3. Создаем список и наполняем его данными (используем Discipline_play)
        disciplineList = new ArrayList<>();
        disciplineList.add(new Discipline_play("Физика", 10, 30));
        disciplineList.add(new Discipline_play("Биология", 20, 30));
        disciplineList.add(new Discipline_play("Химия", 5, 25));
        disciplineList.add(new Discipline_play("Математика", 15, 40));

        // 4. Инициализируем адаптер с припиской _play
        adapter = new DisciplineAdapter_play(disciplineList);

        // 5. Подключаем адаптер к списку
        rvDisciplines.setAdapter(adapter);

        // 6. Логика кнопки "Создать дисциплину"


        Button btnCreate = findViewById(R.id.btnCreateDiscipline);
        if (btnCreate != null) {
            btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Переход на экран создания дисциплины
                    Intent intent = new Intent(Disciplines_activity.this, Disciplines_create_activity.class);
                    startActivity(intent);
                }
            });
        }

        // 7. Кнопки нижней панели навигации
        setupNavigation();
    }

    private void setupNavigation() {
        // Кнопка ДОМОЙ
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(Disciplines_activity.this, Home_page_activity.class));
                finish();
            });
        }

        // Кнопка ПРОФИЛЬ
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // Здесь будет переход в профиль:
                // startActivity(new Intent(Disciplines_activity.this, Profile_activity.class));
            });
        }
    }
}