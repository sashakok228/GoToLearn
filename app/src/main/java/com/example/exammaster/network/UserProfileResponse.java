package com.example.exammaster.network;

public class UserProfileResponse {

    private final long userId;
    private final String username;
    private final String email;
    private final String avatarUrl;

    public UserProfileResponse(long userId,
                               String username,
                               String email,
                               String avatarUrl) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }
}