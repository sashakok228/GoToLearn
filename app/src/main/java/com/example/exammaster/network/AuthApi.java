package com.example.exammaster.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthApi {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void register(RegisterRequest request, AuthCallback callback) {
        executor.execute(() -> {
            try {
                String response = postJson(
                        ApiConfig.BASE_URL + "/auth/register",
                        buildRegisterJson(request).toString(),
                        null
                );
                AuthResponse authResponse = parseAuthResponse(response);
                mainHandler.post(() -> callback.onSuccess(authResponse));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void login(LoginRequest request, AuthCallback callback) {
        executor.execute(() -> {
            try {
                String response = postJson(
                        ApiConfig.BASE_URL + "/auth/login",
                        buildLoginJson(request).toString(),
                        null
                );
                AuthResponse authResponse = parseAuthResponse(response);
                mainHandler.post(() -> callback.onSuccess(authResponse));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private JSONObject buildRegisterJson(RegisterRequest request) throws Exception {
        JSONObject json = new JSONObject();
        json.put("username", request.getUsername());
        json.put("email", request.getEmail());
        json.put("password", request.getPassword());
        return json;
    }

    private JSONObject buildLoginJson(LoginRequest request) throws Exception {
        JSONObject json = new JSONObject();
        json.put("email", request.getEmail());
        json.put("password", request.getPassword());
        return json;
    }

    private AuthResponse parseAuthResponse(String responseBody) throws Exception {
        JSONObject json = new JSONObject(responseBody);

        String token = json.getString("token");
        long userId = json.getLong("userId");
        String username = json.getString("username");
        String email = json.getString("email");

        return new AuthResponse(token, userId, username, email);
    }

    public String postJson(String urlString, String jsonBody, String bearerToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            if (bearerToken != null && !bearerToken.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(jsonBody);
                writer.flush();
            }

            int code = connection.getResponseCode();
            InputStream stream = (code >= 200 && code < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String response = readStream(stream);

            if (code < 200 || code >= 300) {
                throw new RuntimeException("HTTP " + code + ": " + response);
            }

            return response;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}