package com.example.exammaster.network;

public class AuthResponse {
    private final String token;
    private final long userId;
    private final String username;
    private final String email;

    public AuthResponse(String token, long userId, String username, String email) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}