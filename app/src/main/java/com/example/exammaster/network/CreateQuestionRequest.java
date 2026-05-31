package com.example.exammaster.network;

public class CreateQuestionRequest {

    private final Integer questionNumber;
    private final String questionText;
    private final String correctAnswer;

    public CreateQuestionRequest(Integer questionNumber,
                                 String questionText,
                                 String correctAnswer) {
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}