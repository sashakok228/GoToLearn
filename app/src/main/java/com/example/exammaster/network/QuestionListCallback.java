package com.example.exammaster.network;

import java.util.List;

public interface QuestionListCallback {

    void onSuccess(List<QuestionResponse> questions);

    void onError(String errorMessage);
}