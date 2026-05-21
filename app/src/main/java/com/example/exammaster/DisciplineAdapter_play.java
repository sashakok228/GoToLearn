package com.example.exammaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DisciplineAdapter_play extends RecyclerView.Adapter<DisciplineAdapter_play.ViewHolder> {

    private List<Discipline_play> disciplineList;

    public DisciplineAdapter_play(List<Discipline_play> disciplineList) {
        this.disciplineList = disciplineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ВОТ ЗДЕСЬ МЫ ПОДКЛЮЧАЕМ ТВОЮ КАРТОЧКУ ПО ИМЕНИ ФАЙЛА
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discipline_grid_play, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discipline_play discipline = disciplineList.get(position);

        // Устанавливаем название
        holder.tvSubjectTitle.setText(discipline.getTitle());

        // Устанавливаем текст прогресса (например, "10/30")
        holder.tvProgressRatio.setText(discipline.getCurrentProgress() + "/" + discipline.getTotalQuestions());

        // Настраиваем шкалу прогресса (ProgressBar)
        holder.pb1.setMax(discipline.getTotalQuestions());
        holder.pb1.setProgress(discipline.getCurrentProgress());

        // Логика кнопки "Играть"
        holder.btnPlay1.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Начинаем: " + discipline.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return disciplineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Элементы, которые мы ищем ВНУТРИ карточки (твои ID)
        ImageView ivIcon1, settings;
        TextView tvSubjectTitle, tvProgressRatio;
        ProgressBar pb1;
        ImageButton btnPlay1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Привязываем переменные к ID из файла item_discipline_grid_play.xml
            ivIcon1 = itemView.findViewById(R.id.ivIcon1);
            tvSubjectTitle = itemView.findViewById(R.id.tvSubjectTitle);
            tvProgressRatio = itemView.findViewById(R.id.tvProgressRatio);
            pb1 = itemView.findViewById(R.id.pb1);
            settings = itemView.findViewById(R.id.settings);
            btnPlay1 = itemView.findViewById(R.id.btnPlay1);
        }
    }
}