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

public class PasswordRecoveryApi {

    private final ExecutorService executor;
    private final Handler mainHandler;

    public PasswordRecoveryApi() {
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void sendNewPassword(String email, SimpleCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("email", email);

                String response = postJson(
                        ApiConfig.BASE_URL + "/auth/forgot-password",
                        json.toString()
                );

                String message = "Новый пароль отправлен на почту";

                try {
                    JSONObject object = new JSONObject(response);
                    message = object.optString("message", message);
                } catch (Exception ignored) {
                }

                String finalMessage = message;

                mainHandler.post(() -> callback.onSuccess(finalMessage));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private String postJson(String urlString, String jsonBody) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {

                writer.write(jsonBody);
                writer.flush();
            }

            int code = connection.getResponseCode();
            String response = readResponse(connection, code);

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

    private String readResponse(HttpURLConnection connection, int code) throws IOException {
        InputStream stream = code >= 200 && code < 300
                ? connection.getInputStream()
                : connection.getErrorStream();

        return readStream(stream);
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }

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