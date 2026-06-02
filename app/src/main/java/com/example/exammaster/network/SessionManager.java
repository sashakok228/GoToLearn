package com.example.exammaster.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "auth_session";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_AVATAR_URL = "avatar_url";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuth(AuthResponse response) {
        if (response == null || !isValidToken(response.getToken())) {
            clear();
            return;
        }

        prefs.edit()
                .putString(KEY_TOKEN, response.getToken())
                .putLong(KEY_USER_ID, response.getUserId())
                .putString(KEY_USERNAME, response.getUsername())
                .putString(KEY_EMAIL, response.getEmail())
                .putString(KEY_AVATAR_URL, "")
                .apply();
    }

    public void saveProfile(UserProfileResponse profile) {
        if (profile == null) {
            return;
        }

        prefs.edit()
                .putLong(KEY_USER_ID, profile.getUserId())
                .putString(KEY_USERNAME, profile.getUsername())
                .putString(KEY_EMAIL, profile.getEmail())
                .putString(KEY_AVATAR_URL, profile.getAvatarUrl())
                .apply();
    }

    public void saveAvatarUrl(String avatarUrl) {
        prefs.edit()
                .putString(KEY_AVATAR_URL, avatarUrl == null ? "" : avatarUrl)
                .apply();
    }

    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        return isValidToken(token) ? token : null;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, "");
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    private boolean isValidToken(String token) {
        return token != null
                && !token.trim().isEmpty()
                && !"null".equalsIgnoreCase(token.trim());
    }
}