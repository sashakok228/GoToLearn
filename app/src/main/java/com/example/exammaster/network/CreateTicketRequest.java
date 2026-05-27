package com.example.exammaster.network;

import java.util.List;

public class CreateTicketRequest {

    private final long subjectId;
    private final int ticketNumber;
    private final String title;
    private final String content;
    private final List<CreateQuestionRequest> questions;

    public CreateTicketRequest(long subjectId,
                               int ticketNumber,
                               String title,
                               String content,
                               List<CreateQuestionRequest> questions) {
        this.subjectId = subjectId;
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.content = content;
        this.questions = questions;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public List<CreateQuestionRequest> getQuestions() {
        return questions;
    }
}