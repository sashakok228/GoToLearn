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

public class QuestionApi {

    private final ExecutorService executor;
    private final Handler mainHandler;

    public QuestionApi() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ------------------------------------------------------------
    // ПОЛУЧИТЬ ВСЕ ВОПРОСЫ ВЫБРАННОЙ ДИСЦИПЛИНЫ
    // GET /questions/subject/{subjectId}
    // ------------------------------------------------------------

    public void getQuestionsBySubject(long subjectId,
                                      String token,
                                      QuestionListCallback callback) {

        executor.execute(() -> {
            try {
                String response = getJson(
                        ApiConfig.BASE_URL + "/questions/subject/" + subjectId,
                        token
                );

                List<QuestionResponse> questions = parseQuestions(response);

                mainHandler.post(() -> callback.onSuccess(questions));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // ПОЛУЧИТЬ ОДИН ВОПРОС ПО ID
    // GET /questions/{id}
    // ------------------------------------------------------------

    public void getQuestionById(long questionId,
                                String token,
                                QuestionCallback callback) {

        executor.execute(() -> {
            try {
                String response = getJson(
                        ApiConfig.BASE_URL + "/questions/" + questionId,
                        token
                );

                QuestionResponse question = parseQuestion(response);

                mainHandler.post(() -> callback.onSuccess(question));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // ОБНОВИТЬ ВОПРОС
    // PUT /questions/{id}
    // ------------------------------------------------------------

    public void updateQuestion(long questionId,
                               CreateQuestionRequest request,
                               String token,
                               QuestionCallback callback) {

        executor.execute(() -> {
            try {
                String response = putJson(
                        ApiConfig.BASE_URL + "/questions/" + questionId,
                        buildQuestionJson(request).toString(),
                        token
                );

                QuestionResponse question = parseQuestion(response);

                mainHandler.post(() -> callback.onSuccess(question));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // УДАЛИТЬ ВОПРОС
    // DELETE /questions/{id}
    // ------------------------------------------------------------

    public void deleteQuestion(long questionId,
                               String token,
                               QuestionCallback callback) {

        executor.execute(() -> {
            try {
                deleteJson(
                        ApiConfig.BASE_URL + "/questions/" + questionId,
                        token
                );

                QuestionResponse deletedQuestion = new QuestionResponse(
                        questionId,
                        null,
                        "",
                        "",
                        "",
                        "",
                        ""
                );

                mainHandler.post(() -> callback.onSuccess(deletedQuestion));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------
    // JSON ДЛЯ ОБНОВЛЕНИЯ ВОПРОСА
    // ------------------------------------------------------------

    private JSONObject buildQuestionJson(CreateQuestionRequest request) throws Exception {
        JSONObject json = new JSONObject();

        if (request.getQuestionNumber() != null) {
            json.put("questionNumber", request.getQuestionNumber());
        }

        json.put("questionText", request.getQuestionText());
        json.put("correctAnswer", request.getCorrectAnswer());

        return json;
    }

    // ------------------------------------------------------------
    // ПАРСИНГ СПИСКА ВОПРОСОВ
    // ------------------------------------------------------------

    private List<QuestionResponse> parseQuestions(String json) throws Exception {
        List<QuestionResponse> result = new ArrayList<>();

        JSONArray array = new JSONArray(json);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            QuestionResponse question = parseQuestion(object);
            result.add(question);
        }

        return result;
    }

    // ------------------------------------------------------------
    // ПАРСИНГ ОДНОГО ВОПРОСА
    // ------------------------------------------------------------

    private QuestionResponse parseQuestion(String json) throws Exception {
        JSONObject object = new JSONObject(json);
        return parseQuestion(object);
    }

    private QuestionResponse parseQuestion(JSONObject object) {
        long id = object.optLong("id", -1);

        Integer questionNumber = null;
        if (!object.isNull("questionNumber")) {
            questionNumber = object.optInt("questionNumber");
        }

        String questionText = object.optString("questionText", "");
        String correctAnswer = object.optString("correctAnswer", "");

        String wrongAnswer1 = object.optString("wrongAnswer1", "");
        String wrongAnswer2 = object.optString("wrongAnswer2", "");
        String wrongAnswer3 = object.optString("wrongAnswer3", "");

        return new QuestionResponse(
                id,
                questionNumber,
                questionText,
                correctAnswer,
                wrongAnswer1,
                wrongAnswer2,
                wrongAnswer3
        );
    }

    // ------------------------------------------------------------
    // GET
    // ------------------------------------------------------------

    private String getJson(String urlString, String bearerToken) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

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
    // PUT
    // ------------------------------------------------------------

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

    // ------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------

    private void deleteJson(String urlString,
                            String bearerToken) throws Exception {

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

    // ------------------------------------------------------------
    // ОБЩИЕ HTTP-МЕТОДЫ
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