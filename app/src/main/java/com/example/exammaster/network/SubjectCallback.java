package com.example.exammaster.network;

public interface SubjectCallback {
    void onSuccess(SubjectResponse response);
    void onError(String errorMessage);
}