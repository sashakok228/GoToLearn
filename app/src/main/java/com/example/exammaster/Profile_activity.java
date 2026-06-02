package com.example.exammaster;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.exammaster.network.PasswordCheckCallback;
import com.example.exammaster.network.ServerImageLoader;
import com.example.exammaster.network.SessionManager;
import com.example.exammaster.network.SimpleCallback;
import com.example.exammaster.network.UserProfileApi;
import com.example.exammaster.network.UserProfileCallback;
import com.example.exammaster.network.UserProfileResponse;

public class Profile_activity extends AppCompatActivity {

    private static final int REQUEST_PICK_AVATAR = 3001;

    private ImageView ivProfileIcon;
    private TextView tvGreeting;

    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;

    private SessionManager sessionManager;
    private UserProfileApi userProfileApi;

    private Uri selectedAvatarUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        sessionManager = new SessionManager(this);
        userProfileApi = new UserProfileApi(this);

        ivProfileIcon = findViewById(R.id.ivProfileIcon);
        tvGreeting = findViewById(R.id.tvGreeting);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        loadUserDataFromSession();
        loadUserDataFromServer();

        setupButtons();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadUserDataFromSession();
        loadUserDataFromServer();
    }

    private void loadUserDataFromSession() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        String avatarUrl = sessionManager.getAvatarUrl();

        if (username == null || username.trim().isEmpty()) {
            username = makeUsernameFromEmail(email);
        }

        if (username == null || username.trim().isEmpty()) {
            username = "User";
        }

        if (email == null || email.trim().isEmpty()) {
            email = "";
        }

        tvGreeting.setText("Привет, " + username + "!");

        etUsername.setText(username);
        etEmail.setText(email);

        etUsername.setEnabled(true);
        etUsername.setFocusable(true);
        etUsername.setFocusableInTouchMode(true);
        etUsername.setCursorVisible(true);

        etEmail.setEnabled(true);
        etEmail.setFocusable(true);
        etEmail.setFocusableInTouchMode(true);
        etEmail.setCursorVisible(true);

        ServerImageLoader.load(
                ivProfileIcon,
                avatarUrl,
                R.drawable.ic_profile
        );
    }

    private void loadUserDataFromServer() {
        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            return;
        }

        userProfileApi.getMyProfile(token, new UserProfileCallback() {
            @Override
            public void onSuccess(UserProfileResponse profile) {
                sessionManager.saveProfile(profile);
                loadUserDataFromSession();
            }

            @Override
            public void onError(String errorMessage) {
                if (AuthRedirector.isUnauthorizedError(errorMessage)) {
                    AuthRedirector.logoutToSignIn(Profile_activity.this);
                }
            }
        });
    }

    private void setupButtons() {
        if (ivProfileIcon != null) {
            ivProfileIcon.setOnClickListener(v -> openAvatarPicker());
        }

        View btnChangeUsername = findViewById(R.id.btnChangeUsername);

        if (btnChangeUsername != null) {
            btnChangeUsername.setOnClickListener(v -> updateUsername());
        }

        View btnChangeEmail = findViewById(R.id.btnChangeEmail);

        if (btnChangeEmail != null) {
            btnChangeEmail.setOnClickListener(v -> startEmailChange());
        }

        View btnChangePassword = findViewById(R.id.btnChangePassword);

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> checkOldPasswordAndOpenChangeScreen());
        }

        View btnLogout = findViewById(R.id.btnLogout);

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void updateUsername() {
        String newUsername = etUsername.getText().toString().trim();

        if (newUsername.isEmpty()) {
            etUsername.setError("Введите имя");
            etUsername.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            AuthRedirector.logoutToSignIn(this);
            return;
        }

        userProfileApi.updateUsername(newUsername, token, new UserProfileCallback() {
            @Override
            public void onSuccess(UserProfileResponse profile) {
                sessionManager.saveProfile(profile);
                loadUserDataFromSession();

                Toast.makeText(
                        Profile_activity.this,
                        "Имя изменено",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(
                        Profile_activity.this,
                        "Ошибка смены имени: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void startEmailChange() {
        String newEmail = etEmail.getText().toString().trim();

        if (newEmail.isEmpty()) {
            etEmail.setError("Введите новую почту");
            etEmail.requestFocus();
            return;
        }

        if (!newEmail.contains("@")) {
            etEmail.setError("Некорректная почта");
            etEmail.requestFocus();
            return;
        }

        String currentEmail = sessionManager.getEmail();

        if (currentEmail != null && newEmail.equalsIgnoreCase(currentEmail)) {
            etEmail.setError("Это уже ваша текущая почта");
            etEmail.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            AuthRedirector.logoutToSignIn(this);
            return;
        }

        userProfileApi.startEmailChange(newEmail, token, new SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(
                        Profile_activity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                Intent intent = new Intent(Profile_activity.this, ConfirmEmailChange_activity.class);
                intent.putExtra("newEmail", newEmail);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(
                        Profile_activity.this,
                        "Ошибка смены почты: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void checkOldPasswordAndOpenChangeScreen() {
        String oldPassword = etPassword.getText().toString().trim();

        if (oldPassword.isEmpty()) {
            etPassword.setError("Введите старый пароль");
            etPassword.requestFocus();
            return;
        }

        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            AuthRedirector.logoutToSignIn(this);
            return;
        }

        Toast.makeText(this, "Проверяем пароль...", Toast.LENGTH_SHORT).show();

        userProfileApi.checkOldPassword(oldPassword, token, new PasswordCheckCallback() {
            @Override
            public void onSuccess(boolean valid) {
                if (!valid) {
                    etPassword.setError("Старый пароль неверный");
                    etPassword.requestFocus();

                    Toast.makeText(
                            Profile_activity.this,
                            "Старый пароль неверный",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                Intent intent = new Intent(Profile_activity.this, ChangePassword_activity.class);
                intent.putExtra("oldPassword", oldPassword);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(
                        Profile_activity.this,
                        "Ошибка проверки пароля: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из аккаунта?")
                .setMessage("Вы вернётесь на экран авторизации.")
                .setPositiveButton("Выйти", (dialog, which) -> logout())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void logout() {
        sessionManager.clear();

        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Profile_activity.this, SignIn_activity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openAvatarPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, REQUEST_PICK_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_PICK_AVATAR) {
            return;
        }

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        Uri imageUri = data.getData();

        if (imageUri == null) {
            return;
        }

        selectedAvatarUri = imageUri;

        try {
            getContentResolver().takePersistableUriPermission(
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (Exception ignored) {
        }

        ivProfileIcon.setImageURI(selectedAvatarUri);

        uploadAvatar();
    }

    private void uploadAvatar() {
        String token = sessionManager.getToken();

        if (isBadToken(token)) {
            AuthRedirector.logoutToSignIn(this);
            return;
        }

        if (selectedAvatarUri == null) {
            return;
        }

        Toast.makeText(this, "Загружаем аватарку...", Toast.LENGTH_SHORT).show();

        userProfileApi.uploadAvatar(selectedAvatarUri, token, new UserProfileCallback() {
            @Override
            public void onSuccess(UserProfileResponse profile) {
                sessionManager.saveProfile(profile);

                ServerImageLoader.load(
                        ivProfileIcon,
                        profile.getAvatarUrl(),
                        R.drawable.ic_profile
                );

                Toast.makeText(
                        Profile_activity.this,
                        "Аватарка обновлена",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(
                        Profile_activity.this,
                        "Ошибка загрузки аватарки: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String makeUsernameFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }

        String cleanEmail = email.trim();
        int atIndex = cleanEmail.indexOf("@");

        if (atIndex > 0) {
            return cleanEmail.substring(0, atIndex);
        }

        return cleanEmail;
    }

    private boolean isBadToken(String token) {
        return token == null
                || token.trim().isEmpty()
                || token.trim().equalsIgnoreCase("null");
    }

    private void setupNavigation() {
        View navHome = findViewById(R.id.navHome);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(Profile_activity.this, Home_page_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navLibrary = findViewById(R.id.navLibrary);

        if (navLibrary != null) {
            navLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(Profile_activity.this, Disciplines_activity.class);
                startActivity(intent);
                finish();
            });
        }

        View navProfile = findViewById(R.id.navProfile);

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                // Уже на профиле
            });
        }
    }
}