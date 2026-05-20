package com.example.exammaster;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DisciplineAdapter extends RecyclerView.Adapter<DisciplineAdapter.ViewHolder> {

    private List<Discipline> disciplineList;

    public DisciplineAdapter(List<Discipline> disciplineList) {
        this.disciplineList = disciplineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Указываем ту самую "формочку" карточки, которую мы создали в XML
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disciplene_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Discipline discipline = disciplineList.get(position);

        // Вставляем данные в текстовые поля
        holder.tvTitle.setText(discipline.getTitle());
        holder.tvProgress.setText(discipline.getCurrentProgress() + "/" + discipline.getTotalQuestions());

        // Логика кнопки настроек на карточке
        holder.btnSettings.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Настройки: " + discipline.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return disciplineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvProgress;
        ImageButton btnSettings;

        public ViewHolder(View itemView) {
            super(itemView);
            // Находим элементы внутри карточки
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            btnSettings = itemView.findViewById(R.id.btnSettings);
        }
    }
}