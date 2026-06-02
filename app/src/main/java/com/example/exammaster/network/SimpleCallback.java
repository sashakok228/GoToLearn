package com.example.exammaster.network;

public interface SimpleCallback {

    void onSuccess(String message);

    void onError(String errorMessage);
}