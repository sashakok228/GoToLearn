package com.example.exammaster.network;

public interface TicketCallback {
    void onSuccess();

    void onError(String errorMessage);
}