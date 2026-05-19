package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.AuthApi;
import com.example.exammaster.network.AuthCallback;
import com.example.exammaster.network.AuthResponse;
import com.example.exammaster.network.RegisterRequest;
import com.example.exammaster.network.SessionManager;

public class Registration_activity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    private AuthApi authApi;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.registration_activity);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        authApi = new AuthApi();
        sessionManager = new SessionManager(this);

        btnRegister.setOnClickListener(v -> registerUser());

        setupLoginLink();
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            etConfirmPassword.setError("Passwords do not match!");
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        RegisterRequest request = new RegisterRequest(name, email, pass);

        authApi.register(request, new AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                btnRegister.setEnabled(true);

                sessionManager.saveAuth(response);

                Toast.makeText(Registration_activity.this,
                        "Registration successful!",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Registration_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                btnRegister.setEnabled(true);

                Toast.makeText(Registration_activity.this,
                        "Registration error: " + errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupLoginLink() {
        String text = "Already Registered? Log In";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(Registration_activity.this, SignIn_activity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.vidilenievsego));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        ss.setSpan(clickableSpan, 20, 26, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginLink.setText(ss);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}