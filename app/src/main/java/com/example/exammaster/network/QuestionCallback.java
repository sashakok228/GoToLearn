package com.example.exammaster.network;

public interface QuestionCallback {

    void onSuccess(QuestionResponse question);

    void onError(String errorMessage);
}