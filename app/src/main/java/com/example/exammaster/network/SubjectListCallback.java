package com.example.exammaster.network;

import java.util.List;

public interface SubjectListCallback {
    void onSuccess(List<SubjectResponse> subjects);
    void onError(String errorMessage);
}