package com.example.exammaster.network;

import android.content.Context;
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
    private final OfflineCacheManager cacheManager;

    public SubjectApi(Context context) {
        this.cacheManager = new OfflineCacheManager(context);
    }

    public void getSubjects(String token, SubjectListCallback callback) {
        executor.execute(() -> {
            try {
                String response = getJson(ApiConfig.BASE_URL + "/subjects", token);

                cacheManager.saveSubjectsJson(response);

                List<SubjectResponse> subjects = parseSubjects(response);

                mainHandler.post(() -> callback.onSuccess(subjects));

            } catch (Exception serverError) {
                String errorMessage = serverError.getMessage();

                if (isUnauthorized(errorMessage)) {
                    mainHandler.post(() -> callback.onError(errorMessage));
                    return;
                }

                String cachedJson = cacheManager.getSubjectsJson();

                if (cachedJson != null && !cachedJson.trim().isEmpty()) {
                    try {
                        List<SubjectResponse> cachedSubjects = parseSubjects(cachedJson);

                        mainHandler.post(() -> callback.onSuccess(cachedSubjects));
                    } catch (Exception cacheError) {
                        mainHandler.post(() -> callback.onError(
                                "Ошибка чтения кэша дисциплин: " + cacheError.getMessage()
                        ));
                    }
                } else {
                    mainHandler.post(() -> callback.onError(
                            "Нет интернета и нет сохранённых дисциплин"
                    ));
                }
            }
        });
    }

    public void createSubject(CreateSubjectRequest request,
                              String token,
                              SubjectCallback callback) {

        executor.execute(() -> {
            try {
                String response = postJson(
                        ApiConfig.BASE_URL + "/subjects",
                        buildSubjectJson(request).toString(),
                        token
                );

                SubjectResponse subject = parseSubject(response);

                mainHandler.post(() -> callback.onSuccess(subject));

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

    private List<SubjectResponse> parseSubjects(String json) throws Exception {
        List<SubjectResponse> result = new ArrayList<>();

        JSONArray array = new JSONArray(json);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            result.add(parseSubject(object));
        }

        return result;
    }

    private SubjectResponse parseSubject(String json) throws Exception {
        JSONObject object = new JSONObject(json);
        return parseSubject(object);
    }

    private SubjectResponse parseSubject(JSONObject object) {
        long id = object.optLong("id", -1);
        String name = object.optString("name", "");
        String description = object.optString("description", "");

        return new SubjectResponse(id, name, description);
    }

    private String getJson(String urlString, String bearerToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setRequestProperty("Accept", "application/json");

            if (bearerToken != null && !bearerToken.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }

            int code = connection.getResponseCode();

            InputStream stream = code >= 200 && code < 300
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
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            if (bearerToken != null && !bearerToken.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                writer.write(jsonBody);
                writer.flush();
            }

            int code = connection.getResponseCode();

            InputStream stream = code >= 200 && code < 300
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

    private boolean isUnauthorized(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        return errorMessage.contains("HTTP 401")
                || errorMessage.contains("Unauthorized")
                || errorMessage.contains("Invalid JWT token")
                || errorMessage.contains("User from token not found");
    }
}