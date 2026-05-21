package com.example.exammaster; // Проверь, чтобы название пакета было твоим

public class Discipline_play {
    private String title;
    private int currentProgress;
    private int totalQuestions;

    public Discipline_play(String title, int currentProgress, int totalQuestions) {
        this.title = title;
        this.currentProgress = currentProgress;
        this.totalQuestions = totalQuestions;
    }

    // Геттеры, чтобы адаптер мог забрать данные
    public String getTitle() { return title; }
    public int getCurrentProgress() { return currentProgress; }
    public int getTotalQuestions() { return totalQuestions; }
}