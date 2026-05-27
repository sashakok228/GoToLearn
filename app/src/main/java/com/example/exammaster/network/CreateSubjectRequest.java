package com.example.exammaster.network;

public class CreateSubjectRequest {
    private final String name;
    private final String description;

    public CreateSubjectRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}