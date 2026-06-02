package com.example.exammaster.network;

public interface PasswordCheckCallback {

    void onSuccess(boolean valid);

    void onError(String errorMessage);
}