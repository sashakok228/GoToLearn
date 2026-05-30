package com.example.exammaster.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameApi {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final OfflineCacheManager cacheManager;

    public GameApi(Context context) {
        this.cacheManager = new OfflineCacheManager(context);
    }

    public void getQuestionsBySubject(long subjectId,
                                      String token,
                                      GameQuestionsCallback callback) {

        executor.execute(() -> {
            try {
                String response = getJson(
                        ApiConfig.BASE_URL + "/tickets/subject/" + subjectId,
                        token
                );

                cacheManager.saveTicketsJson(subjectId, response);

                List<GameQuestion> questions = parseQuestionsFromTickets(response);

                mainHandler.post(() -> callback.onSuccess(questions));

            } catch (Exception serverError) {
                String errorMessage = serverError.getMessage();

                if (isUnauthorized(errorMessage)) {
                    mainHandler.post(() -> callback.onError(errorMessage));
                    return;
                }

                String cachedJson = cacheManager.getTicketsJson(subjectId);

                if (cachedJson != null && !cachedJson.trim().isEmpty()) {
                    try {
                        List<GameQuestion> cachedQuestions = parseQuestionsFromTickets(cachedJson);

                        mainHandler.post(() -> callback.onSuccess(cachedQuestions));

                    } catch (Exception cacheError) {
                        mainHandler.post(() -> callback.onError(
                                "Ошибка чтения кэша вопросов: " + cacheError.getMessage()
                        ));
                    }
                } else {
                    mainHandler.post(() -> callback.onError(
                            "Нет интернета и нет сохранённых вопросов"
                    ));
                }
            }
        });
    }

    private List<GameQuestion> parseQuestionsFromTickets(String responseBody) throws Exception {
        List<GameQuestion> result = new ArrayList<>();

        JSONArray ticketsArray = new JSONArray(responseBody);

        for (int i = 0; i < ticketsArray.length(); i++) {
            JSONObject ticketJson = ticketsArray.getJSONObject(i);

            JSONArray questionsArray = ticketJson.optJSONArray("questions");

            if (questionsArray == null) {
                continue;
            }

            for (int j = 0; j < questionsArray.length(); j++) {
                JSONObject questionJson = questionsArray.getJSONObject(j);

                long id = questionJson.optLong("id", -1);
                String questionText = questionJson.optString("questionText", "");
                String correctAnswer = questionJson.optString("correctAnswer", "");
                String wrongAnswer1 = questionJson.optString("wrongAnswer1", "");
                String wrongAnswer2 = questionJson.optString("wrongAnswer2", "");
                String wrongAnswer3 = questionJson.optString("wrongAnswer3", "");

                if (!questionText.trim().isEmpty() && !correctAnswer.trim().isEmpty()) {
                    result.add(new GameQuestion(
                            id,
                            questionText,
                            correctAnswer,
                            wrongAnswer1,
                            wrongAnswer2,
                            wrongAnswer3
                    ));
                }
            }
        }

        return result;
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