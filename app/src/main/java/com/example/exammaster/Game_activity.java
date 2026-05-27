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

public class Game_activity extends AppCompatActivity {

    private TextView tvSubjectName;
    private TextView tvQuestionText;
    private LinearLayout containerInput;
    private Button btnCheck;

    private SessionManager sessionManager;
    private GameApi gameApi;

    private String mode = "Definition";
    private String difficulty = "Normal";

    private long subjectId = -1;
    private String subjectName = "Training";

    private final List<GameQuestion> questions = new ArrayList<>();

    private int currentQuestionIndex = 0;
    private int correctAnswersCount = 0;

    private GameQuestion currentQuestion;
    private EditText currentInput;

    private String selectedDefinitionAnswer = null;
    private Button selectedDefinitionButton = null;
    private String definitionEasyTemplate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        containerInput = findViewById(R.id.containerInput);
        btnCheck = findViewById(R.id.btnCheck);

        sessionManager = new SessionManager(this);
        gameApi = new GameApi();

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

        String modeFromIntent = intent.getStringExtra("MODE");
        if (modeFromIntent != null && !modeFromIntent.trim().isEmpty()) {
            mode = modeFromIntent;
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

        selectedDefinitionAnswer = null;
        selectedDefinitionButton = null;
        definitionEasyTemplate = null;
        currentInput = null;

        currentQuestion = questions.get(currentQuestionIndex);

        tvSubjectName.setText(
                "Вопрос " + (currentQuestionIndex + 1) + "/" + questions.size()
        );

        if ("Practice".equalsIgnoreCase(difficulty)) {
            renderPracticeLogic();
            return;
        }

        if ("Definition".equalsIgnoreCase(mode)) {
            renderDefinitionLogic();
        } else {
            renderTestLogic();
        }
    }

    // ------------------------------------------------------------
    // РЕЖИМ DEFINITION
    // Easy   — выбор слова кнопками
    // Normal — ввод короткого ответа
    // Hard   — ввод полного ответа
    // ------------------------------------------------------------

    private void renderDefinitionLogic() {
        if ("Easy".equalsIgnoreCase(difficulty)) {
            renderDefinitionEasy();
        } else if ("Normal".equalsIgnoreCase(difficulty)) {
            renderDefinitionNormal();
        } else if ("Hard".equalsIgnoreCase(difficulty)) {
            renderDefinitionHard();
        } else {
            renderDefinitionNormal();
        }
    }

    private void renderDefinitionEasy() {
        String questionText = currentQuestion.getQuestionText();

        /*
         * Если в вопросе есть "____", работаем как в исходной версии:
         * пользователь нажимает слово, и оно вставляется в пропуск.
         *
         * Пример:
         * "Интеграл — это операция нахождения ____ под графиком."
         *
         * Если пропуска нет, просто показываем вопрос и выбор кнопками.
         */
        definitionEasyTemplate = questionText;

        if (questionText.contains("____")) {
            tvQuestionText.setText(questionText);
        } else {
            tvQuestionText.setText(questionText + "\n\nВыбери правильный ответ:");
        }

        List<String> choices = new ArrayList<>();
        choices.add(currentQuestion.getCorrectAnswer());

        if (!isEmpty(currentQuestion.getWrongAnswer1())) {
            choices.add(currentQuestion.getWrongAnswer1());
        }

        Collections.shuffle(choices);

        for (String choice : choices) {
            addWordChoice(choice);
        }

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Check");
        btnCheck.setOnClickListener(v -> checkDefinitionChoiceAnswer());
    }

    private void renderDefinitionNormal() {
        tvQuestionText.setText(currentQuestion.getQuestionText());

        addInputField("Введите пропущенное слово", false);

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Check");
        btnCheck.setOnClickListener(v -> checkInputAnswer());
    }

    private void renderDefinitionHard() {
        tvQuestionText.setText(
                "Напишите полный ответ:\n\n" + currentQuestion.getQuestionText()
        );

        addInputField("Введите полное определение...", true);

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Check");
        btnCheck.setOnClickListener(v -> checkInputAnswer());
    }

    private void addWordChoice(String word) {
        Button btn = new Button(this);

        btn.setText(word);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.rounded_input);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
        );

        lp.setMargins(0, 0, 0, 30);
        btn.setLayoutParams(lp);

        btn.setOnClickListener(v -> {
            selectedDefinitionAnswer = word;

            if (selectedDefinitionButton != null) {
                selectedDefinitionButton.setBackgroundTintList(null);
            }

            selectedDefinitionButton = btn;
            selectedDefinitionButton.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#90E0EF"))
            );

            if (definitionEasyTemplate != null && definitionEasyTemplate.contains("____")) {
                tvQuestionText.setText(definitionEasyTemplate.replace("____", word));
            }
        });

        containerInput.addView(btn);
    }

    private void addInputField(String hint, boolean multiLine) {
        EditText et = new EditText(this);

        et.setHint(hint);
        et.setGravity(multiLine ? Gravity.TOP : Gravity.CENTER);
        et.setBackgroundResource(R.drawable.rounded_input);
        et.setPadding(40, 40, 40, 40);
        et.setSingleLine(!multiLine);
        et.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                multiLine ? 400 : LinearLayout.LayoutParams.WRAP_CONTENT
        );

        lp.setMargins(0, 0, 0, 40);
        et.setLayoutParams(lp);

        currentInput = et;
        containerInput.addView(et);
    }

    // ------------------------------------------------------------
    // РЕЖИМ TEST
    // Easy   — 2 варианта
    // Normal — 3 варианта
    // Hard   — 4 варианта, потому что в БД пока 3 неправильных ответа
    // ------------------------------------------------------------

    private void renderTestLogic() {
        tvQuestionText.setText(currentQuestion.getQuestionText());

        List<String> answers = new ArrayList<>();

        answers.add(currentQuestion.getCorrectAnswer());

        if (!isEmpty(currentQuestion.getWrongAnswer1())) {
            answers.add(currentQuestion.getWrongAnswer1());
        }

        if (!"Easy".equalsIgnoreCase(difficulty)) {
            if (!isEmpty(currentQuestion.getWrongAnswer2())) {
                answers.add(currentQuestion.getWrongAnswer2());
            }
        }

        if ("Hard".equalsIgnoreCase(difficulty)) {
            if (!isEmpty(currentQuestion.getWrongAnswer3())) {
                answers.add(currentQuestion.getWrongAnswer3());
            }
        }

        Collections.shuffle(answers);

        for (String answer : answers) {
            addAnswerButton(answer);
        }

        btnCheck.setVisibility(View.GONE);
    }

    private void addAnswerButton(String text) {
        Button btn = new Button(this);

        btn.setText(text);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.rounded_input);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150
        );

        lp.setMargins(0, 0, 0, 30);
        btn.setLayoutParams(lp);

        btn.setOnClickListener(v -> checkTestAnswer(btn, text));

        containerInput.addView(btn);
    }

    // ------------------------------------------------------------
    // PRACTICE
    // ------------------------------------------------------------

    private void renderPracticeLogic() {
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
    // ПРОВЕРКА ОТВЕТОВ
    // ------------------------------------------------------------

    private void checkDefinitionChoiceAnswer() {
        if (selectedDefinitionAnswer == null || selectedDefinitionAnswer.trim().isEmpty()) {
            Toast.makeText(this, "Выберите ответ", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCorrect = normalize(selectedDefinitionAnswer)
                .equals(normalize(currentQuestion.getCorrectAnswer()));

        if (isCorrect) {
            correctAnswersCount++;

            if (selectedDefinitionButton != null) {
                selectedDefinitionButton.setBackgroundTintList(
                        ColorStateList.valueOf(Color.GREEN)
                );
            }

            Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            if (selectedDefinitionButton != null) {
                selectedDefinitionButton.setBackgroundTintList(
                        ColorStateList.valueOf(Color.RED)
                );
            }

            Toast.makeText(this, "Неправильно", Toast.LENGTH_SHORT).show();
            highlightCorrectButton();
        }

        disableAllButtons();

        btnCheck.setText("Дальше");
        btnCheck.setOnClickListener(v -> goToNextQuestion());
    }

    private void checkInputAnswer() {
        if (currentInput == null) {
            return;
        }

        String userAnswer = currentInput.getText().toString().trim();

        if (userAnswer.isEmpty()) {
            currentInput.setError("Введите ответ");
            return;
        }

        boolean isCorrect = normalize(userAnswer)
                .equals(normalize(currentQuestion.getCorrectAnswer()));

        if (isCorrect) {
            correctAnswersCount++;
            Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(
                    this,
                    "Неправильно. Правильный ответ: " + currentQuestion.getCorrectAnswer(),
                    Toast.LENGTH_LONG
            ).show();
        }

        currentInput.setEnabled(false);

        btnCheck.setText("Дальше");
        btnCheck.setOnClickListener(v -> goToNextQuestion());
    }

    private void checkTestAnswer(Button selectedButton, String selectedAnswer) {
        boolean isCorrect = normalize(selectedAnswer)
                .equals(normalize(currentQuestion.getCorrectAnswer()));

        if (isCorrect) {
            correctAnswersCount++;
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            Toast.makeText(this, "Верно!", Toast.LENGTH_SHORT).show();
        } else {
            selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            Toast.makeText(this, "Неправильно", Toast.LENGTH_SHORT).show();
            highlightCorrectButton();
        }

        disableAllButtons();

        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setText("Дальше");
        btnCheck.setOnClickListener(v -> goToNextQuestion());
    }

    private void highlightCorrectButton() {
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

    private void disableAllButtons() {
        for (int i = 0; i < containerInput.getChildCount(); i++) {
            View child = containerInput.getChildAt(i);

            if (child instanceof Button) {
                child.setEnabled(false);
            }
        }
    }

    // ------------------------------------------------------------
    // ПЕРЕХОД К СЛЕДУЮЩЕМУ ВОПРОСУ
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
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ------------------------------------------------------------

    private boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
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
}