package com.example.exammaster.network;

public interface UserProfileCallback {

    void onSuccess(UserProfileResponse profile);

    void onError(String errorMessage);
}