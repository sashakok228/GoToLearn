package com.example.exammaster.network;

public class GameQuestion {

    private final long id;
    private final String questionText;
    private final String correctAnswer;
    private final String wrongAnswer1;
    private final String wrongAnswer2;
    private final String wrongAnswer3;

    public GameQuestion(long id,
                        String questionText,
                        String correctAnswer,
                        String wrongAnswer1,
                        String wrongAnswer2,
                        String wrongAnswer3) {
        this.id = id;
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.wrongAnswer1 = wrongAnswer1;
        this.wrongAnswer2 = wrongAnswer2;
        this.wrongAnswer3 = wrongAnswer3;
    }

    public long getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getWrongAnswer1() {
        return wrongAnswer1;
    }

    public String getWrongAnswer2() {
        return wrongAnswer2;
    }

    public String getWrongAnswer3() {
        return wrongAnswer3;
    }
}