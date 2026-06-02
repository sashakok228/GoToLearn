package com.example.exammaster.network;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserProfileApi {

    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Context appContext;

    public UserProfileApi(Context context) {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.appContext = context.getApplicationContext();
    }

    // ------------------------------------------------------------
    // ПОЛУЧИТЬ ПРОФИЛЬ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ
    // GET /profile/me
    // ------------------------------------------------------------

    public void getMyProfile(String token, UserProfileCallback callback) {
        executor.execute(() -> {
            try {
                String response = getJson(
                        ApiConfig.BASE_URL + "/profile/me",
                        token
                );

                UserProfileResponse profile = parseProfile(response);

                mainHandler.post(() -> callback.onSuccess(profile));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // ПРОВЕРИТЬ СТАРЫЙ ПАРОЛЬ
    // POST /profile/me/check-password
    // ------------------------------------------------------------

    public void checkOldPassword(String oldPassword,
                                 String token,
                                 PasswordCheckCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("oldPassword", oldPassword);

                String response = postJson(
                        ApiConfig.BASE_URL + "/profile/me/check-password",
                        json.toString(),
                        token
                );

                JSONObject object = new JSONObject(response);
                boolean valid = object.optBoolean("valid", false);

                mainHandler.post(() -> callback.onSuccess(valid));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // СМЕНИТЬ ПАРОЛЬ
    // POST /profile/me/change-password
    // ------------------------------------------------------------

    public void changePassword(String oldPassword,
                               String newPassword,
                               String token,
                               SimpleCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("oldPassword", oldPassword);
                json.put("newPassword", newPassword);

                String response = postJson(
                        ApiConfig.BASE_URL + "/profile/me/change-password",
                        json.toString(),
                        token
                );

                String serverMessage = "Пароль успешно изменён";

                try {
                    JSONObject object = new JSONObject(response);
                    serverMessage = object.optString("message", serverMessage);
                } catch (Exception ignored) {
                    // Если сервер вернул не JSON, оставляем стандартное сообщение.
                }

                /*
                 * ВАЖНО:
                 * Для использования внутри lambda нужна final-переменная.
                 */
                String finalMessage = serverMessage;

                mainHandler.post(() -> callback.onSuccess(finalMessage));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // ЗАГРУЗИТЬ АВАТАРКУ
    // POST /profile/me/avatar
    // ------------------------------------------------------------

    public void uploadAvatar(Uri imageUri,
                             String token,
                             UserProfileCallback callback) {
        executor.execute(() -> {
            try {
                if (imageUri == null) {
                    throw new RuntimeException("Image uri is null");
                }

                String response = uploadMultipartImage(
                        ApiConfig.BASE_URL + "/profile/me/avatar",
                        imageUri,
                        token
                );

                UserProfileResponse profile = parseProfile(response);

                mainHandler.post(() -> callback.onSuccess(profile));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // ПАРСИНГ ПРОФИЛЯ
    // ------------------------------------------------------------

    private UserProfileResponse parseProfile(String json) throws Exception {
        JSONObject object = new JSONObject(json);

        long userId = object.optLong("userId", -1);
        String username = object.optString("username", "");
        String email = object.optString("email", "");
        String avatarUrl = object.optString("avatarUrl", "");

        return new UserProfileResponse(
                userId,
                username,
                email,
                avatarUrl
        );
    }

    // ------------------------------------------------------------
    // GET JSON
    // ------------------------------------------------------------

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

    // ------------------------------------------------------------
    // POST JSON
    // ------------------------------------------------------------

    private String postJson(String urlString,
                            String jsonBody,
                            String bearerToken) throws Exception {
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

    // ------------------------------------------------------------
    // POST MULTIPART IMAGE
    // ------------------------------------------------------------

    private String uploadMultipartImage(String urlString,
                                        Uri imageUri,
                                        String bearerToken) throws Exception {
        String boundary = "----GoToLearnAvatarBoundary" + System.currentTimeMillis();
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
            connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=" + boundary
            );

            addAuthorizationHeader(connection, bearerToken);

            String fileName = getFileName(imageUri);

            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "avatar.jpg";
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

    // ------------------------------------------------------------
    // ОБЩИЕ МЕТОДЫ
    // ------------------------------------------------------------

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

    private String getFileName(Uri uri) {
        String result = null;

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
    public void updateUsername(String username,
                               String token,
                               UserProfileCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("username", username);

                String response = putJson(
                        ApiConfig.BASE_URL + "/profile/me/username",
                        json.toString(),
                        token
                );

                UserProfileResponse profile = parseProfile(response);

                mainHandler.post(() -> callback.onSuccess(profile));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void startEmailChange(String newEmail,
                                 String token,
                                 SimpleCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("newEmail", newEmail);

                String response = postJson(
                        ApiConfig.BASE_URL + "/profile/me/email/start",
                        json.toString(),
                        token
                );

                String serverMessage = "Код отправлен";

                try {
                    JSONObject object = new JSONObject(response);
                    serverMessage = object.optString("message", serverMessage);
                } catch (Exception ignored) {
                }

                String finalMessage = serverMessage;

                mainHandler.post(() -> callback.onSuccess(finalMessage));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void confirmEmailChange(String newEmail,
                                   String code,
                                   String token,
                                   UserProfileCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("newEmail", newEmail);
                json.put("code", code);

                String response = postJson(
                        ApiConfig.BASE_URL + "/profile/me/email/confirm",
                        json.toString(),
                        token
                );

                UserProfileResponse profile = parseProfile(response);

                mainHandler.post(() -> callback.onSuccess(profile));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    private String putJson(String urlString,
                           String jsonBody,
                           String bearerToken) throws Exception {
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
}