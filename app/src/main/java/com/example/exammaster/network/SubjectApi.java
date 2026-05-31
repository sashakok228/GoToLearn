package com.example.exammaster.network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectApi {

    private final ExecutorService executor;
    private final Handler mainHandler;
    private final OfflineCacheManager cacheManager;
    private final Context appContext;

    public SubjectApi() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cacheManager = null;
        this.appContext = null;
    }

    public SubjectApi(Context context) {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cacheManager = new OfflineCacheManager(context);
        this.appContext = context.getApplicationContext();
    }

    public void getSubjects(String token, SubjectListCallback callback) {
        executor.execute(() -> {
            try {
                String response = getJson(
                        ApiConfig.BASE_URL + "/subjects",
                        token
                );

                if (cacheManager != null) {
                    cacheManager.saveSubjectsJson(response);
                }

                List<SubjectResponse> subjects = parseSubjects(response);

                mainHandler.post(() -> callback.onSuccess(subjects));

            } catch (Exception serverError) {
                String errorMessage = serverError.getMessage();

                if (isUnauthorized(errorMessage)) {
                    mainHandler.post(() -> callback.onError(errorMessage));
                    return;
                }

                if (cacheManager != null) {
                    String cachedJson = cacheManager.getSubjectsJson();

                    if (cachedJson != null && !cachedJson.trim().isEmpty()) {
                        try {
                            List<SubjectResponse> cachedSubjects = parseSubjects(cachedJson);
                            mainHandler.post(() -> callback.onSuccess(cachedSubjects));
                            return;
                        } catch (Exception cacheError) {
                            mainHandler.post(() -> callback.onError(
                                    "Ошибка чтения кэша дисциплин: " + cacheError.getMessage()
                            ));
                            return;
                        }
                    }
                }

                mainHandler.post(() -> callback.onError(errorMessage));
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

                SubjectResponse subjectResponse = parseSubject(response);

                clearCacheIfEnabled();

                mainHandler.post(() -> callback.onSuccess(subjectResponse));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void updateSubject(long subjectId,
                              CreateSubjectRequest request,
                              String token,
                              SubjectCallback callback) {

        executor.execute(() -> {
            try {
                String response = putJson(
                        ApiConfig.BASE_URL + "/subjects/" + subjectId,
                        buildSubjectJson(request).toString(),
                        token
                );

                SubjectResponse subjectResponse = parseSubject(response);

                clearCacheIfEnabled();

                mainHandler.post(() -> callback.onSuccess(subjectResponse));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void uploadSubjectImage(long subjectId,
                                   Uri imageUri,
                                   String token,
                                   SubjectCallback callback) {

        executor.execute(() -> {
            try {
                if (appContext == null) {
                    throw new RuntimeException("SubjectApi должен быть создан через new SubjectApi(context)");
                }

                if (imageUri == null) {
                    throw new RuntimeException("Image uri is null");
                }

                String response = uploadMultipartImage(
                        ApiConfig.BASE_URL + "/subjects/" + subjectId + "/image",
                        imageUri,
                        token
                );

                SubjectResponse subjectResponse = parseSubject(response);

                clearCacheIfEnabled();

                mainHandler.post(() -> callback.onSuccess(subjectResponse));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void deleteSubject(long subjectId,
                              String token,
                              SubjectCallback callback) {

        executor.execute(() -> {
            try {
                deleteJson(
                        ApiConfig.BASE_URL + "/subjects/" + subjectId,
                        token
                );

                clearCacheIfEnabled();

                SubjectResponse emptyResponse = new SubjectResponse(
                        subjectId,
                        "",
                        "",
                        "",
                        0
                );

                mainHandler.post(() -> callback.onSuccess(emptyResponse));

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
        String imageUrl = object.optString("imageUrl", "");
        int questionCount = object.optInt("questionCount", 0);

        return new SubjectResponse(
                id,
                name,
                description,
                imageUrl,
                questionCount
        );
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

            addAuthorizationHeader(connection, bearerToken);

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

            addAuthorizationHeader(connection, bearerToken);
            writeRequestBody(connection, jsonBody);

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

    private String putJson(String urlString, String jsonBody, String bearerToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PUT");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            addAuthorizationHeader(connection, bearerToken);
            writeRequestBody(connection, jsonBody);

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

    private void deleteJson(String urlString, String bearerToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            connection.setRequestProperty("Accept", "application/json");

            addAuthorizationHeader(connection, bearerToken);

            int code = connection.getResponseCode();
            String response = readResponse(connection, code);

            if (code < 200 || code >= 300) {
                throw new RuntimeException("HTTP " + code + ": " + response);
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String uploadMultipartImage(String urlString,
                                        Uri imageUri,
                                        String bearerToken) throws Exception {

        String boundary = "----GoToLearnBoundary" + System.currentTimeMillis();
        String lineEnd = "\r\n";

        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            addAuthorizationHeader(connection, bearerToken);

            String fileName = getFileName(imageUri);

            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "subject_image.jpg";
            }

            try (OutputStream outputStream = connection.getOutputStream();
                 InputStream inputStream = appContext.getContentResolver().openInputStream(imageUri)) {

                if (inputStream == null) {
                    throw new RuntimeException("Cannot open image stream");
                }

                String header =
                        "--" + boundary + lineEnd +
                                "Content-Disposition: form-data; name=\"image\"; filename=\"" + fileName + "\"" + lineEnd +
                                "Content-Type: image/*" + lineEnd +
                                lineEnd;

                outputStream.write(header.getBytes(StandardCharsets.UTF_8));

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.write(lineEnd.getBytes(StandardCharsets.UTF_8));

                String footer = "--" + boundary + "--" + lineEnd;
                outputStream.write(footer.getBytes(StandardCharsets.UTF_8));

                outputStream.flush();
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

    private String getFileName(Uri uri) {
        String result = null;

        if (appContext == null || uri == null) {
            return null;
        }

        try (Cursor cursor = appContext.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {
        }

        return result;
    }

    private void addAuthorizationHeader(HttpURLConnection connection, String bearerToken) {
        if (bearerToken != null
                && !bearerToken.trim().isEmpty()
                && !bearerToken.trim().equalsIgnoreCase("null")) {

            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        }
    }

    private void writeRequestBody(HttpURLConnection connection, String jsonBody) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {

            writer.write(jsonBody);
            writer.flush();
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

    private boolean isUnauthorized(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        return errorMessage.contains("HTTP 401")
                || errorMessage.contains("Unauthorized")
                || errorMessage.contains("Invalid JWT token")
                || errorMessage.contains("JWT token")
                || errorMessage.contains("User from token not found");
    }

    private void clearCacheIfEnabled() {
        if (cacheManager != null) {
            cacheManager.clearAllCache();
        }
    }
}