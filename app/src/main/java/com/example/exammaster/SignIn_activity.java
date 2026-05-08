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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.Registration_activity;

public class SignIn_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        Button btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Кнопка входа
        btnSignIn.setOnClickListener(v -> {
            Toast.makeText(this, "Signing In...", Toast.LENGTH_SHORT).show();
        });

        // Забыли пароль
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Reset Password Screen", Toast.LENGTH_SHORT).show();
        });

        // Настройка ссылки "Register"
        setupRegisterLink(tvRegisterLink);
    }

    private void setupRegisterLink(TextView textView) {
        String text = "Don't have an account? Register";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // ПЕРЕХОД НА РЕГИСТРАЦИЮ
                Intent intent = new Intent(SignIn_activity.this, Registration_activity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.vidilenievsego)); // Оранжевый
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        // Индексы для слова "Register" (символы с 23 до конца строки)
        ss.setSpan(clickableSpan, 23, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}