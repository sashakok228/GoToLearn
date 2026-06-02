package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.UserProfileApi;
import com.example.exammaster.network.UserProfileCallback;
import com.example.exammaster.network.UserProfileResponse;

public class ConfirmEmailChange_activity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText etCode;

    private SessionManager sessionManager;
    private UserProfileApi userProfileApi;

    private String newEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_email_change_activity);

        tvTitle = findViewById(R.id.tvTitle);
        etCode = findViewById(R.id.etCode);

        sessionManager = new SessionManager(this);
        userProfileApi = new UserProfileApi(this);

        readIntentData();
        setupScreen();
        setupButtons();
    }

    private void readIntentData() {
        String emailFromIntent = getIntent().getStringExtra("newEmail");

        if (emailFromIntent != null) {
            newEmail = emailFromIntent;
        }
    }

    private void setupScreen() {
        tvTitle.setText("Код отправлен на:\n" + newEmail);
    }

    private void setupButtons() {
        View btnConfirm = findViewById(R.id.btnConfirm);

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> confirmEmailChange());
        }
    }

    private void confirmEmailChange() {
        String code = etCode.getText().toString().trim();

        if (code.isEmpty()) {
            etCode.setError("Введите код");
            etCode.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty()) {
            AuthRedirector.logoutToSignIn(this);
            return;
        }

        userProfileApi.confirmEmailChange(newEmail, code, token, new UserProfileCallback() {
            @Override
            public void onSuccess(UserProfileResponse profile) {
                sessionManager.saveProfile(profile);

                Toast.makeText(
                        ConfirmEmailChange_activity.this,
                        "Почта изменена",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(ConfirmEmailChange_activity.this, Profile_activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(
                        ConfirmEmailChange_activity.this,
                        "Ошибка подтверждения: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}