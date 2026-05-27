package com.example.exammaster.network;

import java.util.List;

public interface GameQuestionsCallback {
    void onSuccess(List<GameQuestion> questions);

    void onError(String errorMessage);
}