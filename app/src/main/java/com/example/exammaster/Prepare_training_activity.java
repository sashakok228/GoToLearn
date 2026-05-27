package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Prepare_training_activity extends AppCompatActivity {

    private TextView btnEasy;
    private TextView btnNormal;
    private TextView btnHard;
    private TextView btnPractice;
    private Button btnStart;
    private TextView tvGreeting;

    private TextView selectedButton = null;

    private String selectedDifficulty = "Normal";
    private String currentMode = "Test";

    private long subjectId = -1;
    private String subjectName = "Тренировка";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prepare_training_activity);

        tvGreeting = findViewById(R.id.tvGreeting);

        btnEasy = findViewById(R.id.btnEasy);
        btnNormal = findViewById(R.id.btnNormal);
        btnHard = findViewById(R.id.btnHard);
        btnPractice = findViewById(R.id.btnPractice);
        btnStart = findViewById(R.id.btnStart);

        readIntentData();
        setupTitle();
        setupDifficultyButtons();
        setupStartButton();
        setupNavigation();
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
            currentMode = modeFromIntent;
        }
    }

    private void setupTitle() {
        if (tvGreeting != null) {
            tvGreeting.setText(subjectName);
        }
    }

    private void setupDifficultyButtons() {
        View.OnClickListener difficultyListener = v -> updateSelection((TextView) v);

        btnEasy.setOnClickListener(difficultyListener);
        btnNormal.setOnClickListener(difficultyListener);
        btnHard.setOnClickListener(difficultyListener);
        btnPractice.setOnClickListener(difficultyListener);

        updateSelection(btnNormal);
    }

    private void setupStartButton() {
        btnStart.setOnClickListener(v -> {
            if (subjectId == -1) {
                Toast.makeText(
                        Prepare_training_activity.this,
                        "Ошибка: дисциплина не выбрана",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            Intent intent = new Intent(Prepare_training_activity.this, Game_activity.class);

            intent.putExtra("subjectId", subjectId);
            intent.putExtra("subjectName", subjectName);
            intent.putExtra("MODE", currentMode);
            intent.putExtra("DIFFICULTY", selectedDifficulty);

            startActivity(intent);
        });
    }

    private void updateSelection(TextView newSelected) {
        if (selectedButton != null) {
            selectedButton.setSelected(false);
        }

        newSelected.setSelected(true);
        selectedButton = newSelected;

        selectedDifficulty = newSelected.getText().toString();
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(Prepare_training_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);
        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(Prepare_training_activity.this, Disciplines_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(Prepare_training_activity.this, Profile_activity.class);
                startActivity(intent);
            });
        }
    }
}