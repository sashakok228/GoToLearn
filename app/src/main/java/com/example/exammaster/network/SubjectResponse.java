package com.example.exammaster.network;

public class SubjectResponse {

    private final long id;
    private final String name;
    private final String description;
    private final String imageUrl;
    private final int questionCount;

    public SubjectResponse(long id,
                           String name,
                           String description) {
        this(id, name, description, "", 0);
    }

    public SubjectResponse(long id,
                           String name,
                           String description,
                           String imageUrl,
                           int questionCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.questionCount = questionCount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getQuestionCount() {
        return questionCount;
    }
}