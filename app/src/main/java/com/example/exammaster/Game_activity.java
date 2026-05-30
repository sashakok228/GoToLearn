package com.example.exammaster;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.GameApi;
import com.example.exammaster.network.GameQuestion;
import com.example.exammaster.network.GameQuestionsCallback;
import com.example.exammaster.network.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Game_activity extends AppCompatActivity {

    private TextView tvSubjectName;
    private TextView tvQuestionText;
    private LinearLayout containerInput;
    private Button btnCheck;

    private SessionManager sessionManager;
    private GameApi gameApi;

    private long subjectId = -1;
    private String subjectName = "Тренировка";
    private String difficulty = "Normal";

    private final List<GameQuestion> questions = new ArrayList<>();
    private final Random random = new Random();

    private int currentQuestionIndex = 0;
    private int correctAnswersCount = 0;

    private GameQuestion currentQuestion;

    private EditText answerInput;
    private String hiddenWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        containerInput = findViewById(R.id.containerInput);
        btnCheck = findViewById(R.id.btnCheck);

        sessionManager = new SessionManager(this);
        gameApi = new GameApi(this);
        readIntentData();
        loadQuestionsFromServer();
    }

    private void readIntentData() {
        Intent intent = getIntent();

        subjectId = intent.getLongExtra("subjectId", -1);

        String subjectNameFromIntent = intent.getStringExtra("subjectName");
        if (subjectNameFromIntent != null && !subjectNameFromIntent.trim().isEmpty()) {
            subjectName = subjectNameFromIntent;
        }

        String difficultyFromIntent = intent.getStringExtra("DIFFICULTY");
        if (difficultyFromIntent != null && !difficultyFromIntent.trim().isEmpty()) {
            difficulty = difficultyFromIntent;
        }
    }

    private void loadQuestionsFromServer() {
        if (subjectId == -1) {
            Toast.makeText(this, "Ошибка: дисциплина не выбрана", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty() || token.trim().equalsIgnoreCase("null")) {
            Toast.makeText(this, "Нет токена. Войдите заново", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Game_activity.this, SignIn_activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        tvSubjectName.setText("Загрузка...");
        tvQuestionText.setText("Загружаем вопросы из базы данных...");
        containerInput.removeAllViews();
        btnCheck.setVisibility(View.GONE);

        gameApi.getQuestionsBySubject(subjectId, token, new GameQuestionsCallback() {
            @Override
            public void onSuccess(List<GameQuestion> loadedQuestions) {
                questions.clear();

                if (loadedQuestions != null) {
                    questions.addAll(loadedQuestions);
                }

                if (questions.isEmpty()) {
                    tvSubjectName.setText(subjectName);
                    tvQuestionText.setText("В этой дисциплине пока нет вопросов.");
                    btnCheck.setVisibility(View.GONE);

                    Toast.makeText(
                            Game_activity.this,
                            "Сначала добавь вопросы в дисциплину",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                Collections.shuffle(questions);

                currentQuestionIndex = 0;
                correctAnswersCount = 0;

                showCurrentQuestion();
            }

            @Override
            public void onError(String errorMessage) {
                tvSubjectName.setText(subjectName);
                tvQuestionText.setText("Ошибка загрузки вопросов");
                btnCheck.setVisibility(View.GONE);

                Toast.makeText(
                        Game_activity.this,
                        "Ошибка загрузки вопросов: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showCurrentQuestion() {
        containerInput.removeAllViews();
        answerInput = null;
        hiddenWord = null;

        currentQuestion = questions.get(currentQuestionIndex);

        tvSubjectName.setText(
                subjectName + " — вопрос " +
                        (currentQuestionIndex + 1) + "/" + questions.size()
        );

        if ("Easy".equalsIgnoreCase(difficulty)) {
            showMissingWordGame();
        } else if ("Normal".equalsIgnoreCase(difficulty)) {
            /*
             * На нормальной сложности иногда пропуск слова,
             * иногда выбор правильного ответа.
             */
            boolean useMissingWord = random.nextBoolean();

            if (useMissingWord) {
                showMissingWordGame();
            } else {
                showChoiceGame();
            }
        } else if ("Hard".equalsIgnoreCase(difficulty)) {
            showChoiceGame();
        } else if ("Practice".equalsIgnoreCase(difficulty)) {
            showPracticeGame();
        } else {
            showChoiceGame();
        }
    }

    // ------------------------------------------------------------
    // EASY / NORMAL: пропущенное слово в правильном ответе
    // ------------------------------------------------------------

    private void showMissingWordGame() {
        String correctAnswer = currentQuestion.getCorrectAnswer();

        if (isEmpty(correctAnswer)) {
            Toast.makeText(this, "У вопроса нет правильного ответа", Toast.LENGTH_LONG).show();
            goToNextQuestion();
            return;
        }

        MaskedAnswer maskedAnswer = makeMaskedAnswer(correctAnswer);

        hiddenWord = maskedAnswer.hiddenWord;

        /*
         * ВАЖНО:
         * Здесь больше НЕ выводим сам вопрос.
         * Показываем только правильный ответ с пропуском.
         */
        tvQuestionText.setText(
                "Заполни пропуск в правильном ответе:\n\n" +
                        maskedAnswer.textWithGap
        );

        answerInput = new EditText(this);
        answerInput.setHint("Введите пропущенное слово");
        answerInput.setSingleLine(true);
        answerInput.setGravity(Gravity.CENTER);
        answerInput.setBackgroundResource(R.drawable.rounded_input);
        answerInput.setPadding(40, 30, 40, 30);
        answerInput.setTextColor(Color.BLACK);
        answerInput.setHintTextColor(Color.GRAY);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        inputParams.setMargins(0, 0, 0, 40);
        answerInput.setLayoutParams(inputParams);

        containerInput.addView(answerInput);

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Проверить");
        btnCheck.setOnClickListener(v -> checkMissingWordAnswer());
    }

    private MaskedAnswer makeMaskedAnswer(String correctAnswer) {
        String[] words = correctAnswer.trim().split("\\s+");

        List<Integer> possibleIndexes = new ArrayList<>();

        for (int i = 0; i < words.length; i++) {
            String cleaned = cleanWord(words[i]);

            if (cleaned.length() >= 3 && !isStopWord(cleaned)) {
                possibleIndexes.add(i);
            }
        }

        int selectedIndex;

        if (!possibleIndexes.isEmpty()) {
            selectedIndex = possibleIndexes.get(random.nextInt(possibleIndexes.size()));
        } else {
            selectedIndex = 0;
        }

        String selectedWord = cleanWord(words[selectedIndex]);

        if (selectedWord.isEmpty()) {
            selectedWord = words[selectedIndex];
        }

        words[selectedIndex] = "____";

        String textWithGap = String.join(" ", words);

        return new MaskedAnswer(textWithGap, selectedWord);
    }

    private void checkMissingWordAnswer() {
        if (answerInput == null) {
            return;
        }

        String userAnswer = answerInput.getText().toString().trim();

        if (userAnswer.isEmpty()) {
            answerInput.setError("Введите пропущенное слово");
            return;
        }

        boolean isCorrect = normalize(userAnswer).equals(normalize(hiddenWord));

        if (isCorrect) {
            correctAnswersCount++;
            Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
            answerInput.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            Toast.makeText(
                    this,
                    "Неправильно. Пропущенное слово: " + hiddenWord,
                    Toast.LENGTH_LONG
            ).show();

            answerInput.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }

        answerInput.setEnabled(false);

        btnCheck.setText("Дальше");
        btnCheck.setOnClickListener(v -> goToNextQuestion());
    }

    // ------------------------------------------------------------
    // NORMAL / HARD: выбор правильного варианта ответа
    // ------------------------------------------------------------

    private void showChoiceGame() {
        tvQuestionText.setText(currentQuestion.getQuestionText());

        List<String> answers = new ArrayList<>();

        answers.add(currentQuestion.getCorrectAnswer());

        if (!isEmpty(currentQuestion.getWrongAnswer1())) {
            answers.add(currentQuestion.getWrongAnswer1());
        }

        if (!isEmpty(currentQuestion.getWrongAnswer2())) {
            answers.add(currentQuestion.getWrongAnswer2());
        }

        if (!isEmpty(currentQuestion.getWrongAnswer3())) {
            answers.add(currentQuestion.getWrongAnswer3());
        }

        Collections.shuffle(answers);

        for (String answer : answers) {
            addAnswerButton(answer);
        }

        btnCheck.setVisibility(View.GONE);
    }

    private void addAnswerButton(String answerText) {
        Button btn = new Button(this);

        btn.setText(answerText);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.rounded_input);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150
        );

        params.setMargins(0, 0, 0, 30);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> checkChoiceAnswer(btn, answerText));

        containerInput.addView(btn);
    }

    private void checkChoiceAnswer(Button selectedButton, String selectedAnswer) {
        boolean isCorrect = normalize(selectedAnswer)
                .equals(normalize(currentQuestion.getCorrectAnswer()));

        if (isCorrect) {
            correctAnswersCount++;
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            Toast.makeText(this, "Неправильно", Toast.LENGTH_SHORT).show();
            highlightCorrectAnswer();
        }

        disableAnswerButtons();

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Дальше");
        btnCheck.setOnClickListener(v -> goToNextQuestion());
    }

    private void highlightCorrectAnswer() {
        for (int i = 0; i < containerInput.getChildCount(); i++) {
            View child = containerInput.getChildAt(i);

            if (child instanceof Button) {
                Button button = (Button) child;

                if (normalize(button.getText().toString())
                        .equals(normalize(currentQuestion.getCorrectAnswer()))) {

                    button.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                }
            }
        }
    }

    private void disableAnswerButtons() {
        for (int i = 0; i < containerInput.getChildCount(); i++) {
            View child = containerInput.getChildAt(i);

            if (child instanceof Button) {
                child.setEnabled(false);
            }
        }
    }

    // ------------------------------------------------------------
    // PRACTICE
    // ------------------------------------------------------------

    private void showPracticeGame() {
        tvQuestionText.setText(
                currentQuestion.getQuestionText()
                        + "\n\nПравильный ответ:\n"
                        + currentQuestion.getCorrectAnswer()
        );

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Дальше");
        btnCheck.setOnClickListener(v -> goToNextQuestion());
    }

    // ------------------------------------------------------------
    // Переходы
    // ------------------------------------------------------------

    private void goToNextQuestion() {
        currentQuestionIndex++;

        if (currentQuestionIndex >= questions.size()) {
            showResult();
            return;
        }

        showCurrentQuestion();
    }

    private void showResult() {
        containerInput.removeAllViews();

        tvSubjectName.setText("Результат");

        tvQuestionText.setText(
                "Ты ответил правильно на "
                        + correctAnswersCount
                        + " из "
                        + questions.size()
                        + " вопросов."
        );

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Завершить");
        btnCheck.setOnClickListener(v -> finish());
    }

    // ------------------------------------------------------------
    // Вспомогательные методы
    // ------------------------------------------------------------

    private boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String cleanWord(String word) {
        if (word == null) {
            return "";
        }

        return word
                .replaceAll("^[^\\p{L}\\p{N}]+", "")
                .replaceAll("[^\\p{L}\\p{N}]+$", "")
                .trim();
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase(Locale.ROOT)
                .replace("ё", "е")
                .replaceAll("[\\p{Punct}]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean isStopWord(String word) {
        String normalized = normalize(word);

        return normalized.equals("это")
                || normalized.equals("как")
                || normalized.equals("для")
                || normalized.equals("или")
                || normalized.equals("при")
                || normalized.equals("над")
                || normalized.equals("под")
                || normalized.equals("что")
                || normalized.equals("где")
                || normalized.equals("она")
                || normalized.equals("оно")
                || normalized.equals("они")
                || normalized.equals("его")
                || normalized.equals("ее")
                || normalized.equals("её")
                || normalized.equals("из")
                || normalized.equals("на")
                || normalized.equals("в")
                || normalized.equals("и")
                || normalized.equals("а")
                || normalized.equals("к")
                || normalized.equals("с")
                || normalized.equals("по")
                || normalized.equals("от");
    }

    private static class MaskedAnswer {
        private final String textWithGap;
        private final String hiddenWord;

        private MaskedAnswer(String textWithGap, String hiddenWord) {
            this.textWithGap = textWithGap;
            this.hiddenWord = hiddenWord;
        }
    }
}