package com.example.exammaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class changepassword_activity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmNewPassword;
    private Button btnUpdatePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword_activity);

        etNewPassword = findViewById(R.id.etPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmPassword);
        btnUpdatePassword = findViewById(R.id.btnChangePassword);

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPass = etNewPassword.getText().toString();
                String confirmPass = etConfirmNewPassword.getText().toString();

                if (newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(changepassword_activity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (!newPass.equals(confirmPass)) {
                    etConfirmNewPassword.setError("Passwords do not match");
                    Toast.makeText(changepassword_activity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // Если всё успешно
                    Toast.makeText(changepassword_activity.this, "Password updated successfully!", Toast.LENGTH_LONG).show();

                    // После смены пароля обычно перекидывают на экран логина
                    Intent intent = new Intent(changepassword_activity  .this, SignIn_activity.class);
                    // Очищаем стек активностей, чтобы нельзя было вернуться назад к смене пароля
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}