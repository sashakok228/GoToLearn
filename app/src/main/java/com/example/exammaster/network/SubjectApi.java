package com.example.exammaster.network;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectApi {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void createSubject(CreateSubjectRequest request, String token, SubjectCallback callback) {
        executor.execute(() -> {
            try {
                String response = postJson(
                        ApiConfig.BASE_URL + "/subjects",
                        buildSubjectJson(request).toString(),
                        token
                );

                SubjectResponse subjectResponse = parseSubjectResponse(response);
                mainHandler.post(() -> callback.onSuccess(subjectResponse));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getSubjects(String token, SubjectListCallback callback) {
        executor.execute(() -> {
            try {
                String response = getJson(
                        ApiConfig.BASE_URL + "/subjects",
                        token
                );

                List<SubjectResponse> subjects = parseSubjectList(response);
                mainHandler.post(() -> callback.onSuccess(subjects));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private JSONObject buildSubjectJson(CreateSubjectRequest request) throws Exception {
        JSONObject json = new JSONObject();
        json.put("name", request.getName());
        json.put("description", request.getDescription());
        return json;
    }

    private SubjectResponse parseSubjectResponse(String responseBody) throws Exception {
        JSONObject json = new JSONObject(responseBody);

        long id = json.getLong("id");
        String name = json.optString("name", "");
        String description = json.optString("description", "");

        return new SubjectResponse(id, name, description);
    }

    private List<SubjectResponse> parseSubjectList(String responseBody) throws Exception {
        JSONArray array = new JSONArray(responseBody);
        List<SubjectResponse> result = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.getJSONObject(i);

            long id = json.getLong("id");
            String name = json.optString("name", "");
            String description = json.optString("description", "");

            result.add(new SubjectResponse(id, name, description));
        }

        return result;
    }

    private String getJson(String urlString, String bearerToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");

            if (bearerToken != null && !bearerToken.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
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

    private String postJson(String urlString, String jsonBody, String bearerToken) throws Exception {
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
