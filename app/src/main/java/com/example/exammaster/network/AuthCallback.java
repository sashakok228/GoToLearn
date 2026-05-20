package com.example.exammaster.network;

public interface AuthCallback {
    void onSuccess(AuthResponse response);
    void onError(String errorMessage);
}