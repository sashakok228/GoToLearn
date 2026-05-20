package com.example.exammaster; // Проверь название своего пакета

public class Discipline {
    private String title;
    private int currentProgress;
    private int totalQuestions;

    public Discipline(String title, int currentProgress, int totalQuestions) {
        this.title = title;
        this.currentProgress = currentProgress;
        this.totalQuestions = totalQuestions;
    }

    // Эти методы нужны адаптеру, чтобы "достать" текст
    public String getTitle() { return title; }
    public int getCurrentProgress() { return currentProgress; }
    public int getTotalQuestions() { return totalQuestions; }
}