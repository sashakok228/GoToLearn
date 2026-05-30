package com.example.exammaster;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.exammaster.network.SessionManager;

public class AuthRedirector {

    public static boolean isUnauthorizedError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        return errorMessage.contains("HTTP 401")
                || errorMessage.contains("Unauthorized")
                || errorMessage.contains("Invalid JWT token")
                || errorMessage.contains("JWT token")
                || errorMessage.contains("User from token not found");
    }

    public static void logoutToSignIn(Activity activity) {
        SessionManager sessionManager = new SessionManager(activity);
        sessionManager.clear();

        Toast.makeText(
                activity,
                "Сессия истекла. Войдите заново",
                Toast.LENGTH_LONG
        ).show();

        Intent intent = new Intent(activity, SignIn_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}