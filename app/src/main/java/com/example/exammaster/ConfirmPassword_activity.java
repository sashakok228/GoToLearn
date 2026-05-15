package com.example.exammaster;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class ConfirmPassword_activity extends AppCompatActivity {

    private EditText code1, code2, code3, code4;
    private TextView tvResend;
    private Button btnChangePassword;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        setContentView(R.layout.confirmpassword_activity);

        // Инициализация всех элементов
        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);
        tvResend = findViewById(R.id.tvResend);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Настройка логики ввода
        setupCodeInputs();

        // Кнопка "Change password"
        btnChangePassword.setOnClickListener(v -> {
            String fullCode = code1.getText().toString() + code2.getText().toString() +
                    code3.getText().toString() + code4.getText().toString();

            if (fullCode.length() < 4) {
                Toast.makeText(this, "Please enter the full code", Toast.LENGTH_SHORT).show();
            } else {
                // Здесь будет переход на экран установки НОВОГО пароля
                Toast.makeText(this, "Code Verified!", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(this, NewPasswordActivity.class);
                // startActivity(intent);
            }
        });

        // Кликабельный текст "Resend" с таймером
        tvResend.setOnClickListener(v -> {
            if (!isTimerRunning) {
                startTimer();
            }
        });
    }

    private void setupCodeInputs() {

        code1.addTextChangedListener(new GenericTextWatcher(code1, code2));
        code2.addTextChangedListener(new GenericTextWatcher(code2, code3));
        code3.addTextChangedListener(new GenericTextWatcher(code3, code4));
        code4.addTextChangedListener(new GenericTextWatcher(code4, null));


        code1.setOnKeyListener(new GenericKeyEvent(code1, null));
        code2.setOnKeyListener(new GenericKeyEvent(code2, code1));
        code3.setOnKeyListener(new GenericKeyEvent(code3, code2));
        code4.setOnKeyListener(new GenericKeyEvent(code4, code3));
    }

    
    public class GenericTextWatcher implements TextWatcher {
        private final View currentView;
        private final View nextView;

        public GenericTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            if (text.length() == 1 && nextView != null) {
                nextView.requestFocus();
            } else if (text.length() == 1 && nextView == null) {
                // Скрываем клавиатуру на последней цифре
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
            }
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    // Класс для движения КУРСОР НАЗАД (Backspace)
    public class GenericKeyEvent implements View.OnKeyListener {
        private final EditText currentView;
        private final EditText previousView;

        public GenericKeyEvent(EditText currentView, EditText previousView) {
            this.currentView = currentView;
            this.previousView = previousView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL
                    && currentView.getText().toString().isEmpty() && previousView != null) {
                previousView.requestFocus();
                return true;
            }
            return false;
        }
    }

    private void startTimer() {
        isTimerRunning = true;
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.5f);

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResend.setText(String.format(Locale.getDefault(), "Resend in %02d", (int) (millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                tvResend.setEnabled(true);
                tvResend.setAlpha(1.0f);
                tvResend.setText("Resend Code");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}