package com.example.esports_arena;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.model.Player;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private TextView statusText;
    private ProgressBar loading;
    private Button loginButton;

    private PlayerRepository playerRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        playerRepository = new PlayerRepository();

        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        statusText = findViewById(R.id.statusText);
        loading = findViewById(R.id.loading);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        statusText.setText("");
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        String username = usernameInput.getText() != null ? usernameInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError("Username required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password required");
            return;
        }

        setLoading(true);
        hideKeyboard();
        playerRepository.getByUsername(username).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                statusText.setText("Network error. Try again.");
                return;
            }

            Player player = task.getResult();
            if (player == null) {
                setLoading(true);
                playerRepository.getAll().addOnCompleteListener(allTask -> {
                    setLoading(false);
                    if (!allTask.isSuccessful() || allTask.getResult() == null) {
                        statusText.setText("User not found");
                        return;
                    }
                    Player match = null;
                    for (Player p : allTask.getResult()) {
                        if (p.getUsername() != null && p.getUsername().equalsIgnoreCase(username)) {
                            match = p;
                            break;
                        }
                    }
                    if (match == null) {
                        statusText.setText("User not found (check exact username)");
                        return;
                    }
                    verifyPasswordAndLogin(match, password);
                });
                return;
            }

            verifyPasswordAndLogin(player, password);
        });
    }

    private void verifyPasswordAndLogin(Player player, String password) {
        if (!password.equals(player.getPassword())) {
            statusText.setText("Invalid credentials");
            return;
        }
        onLoginSuccess(player);
    }

    private void setLoading(boolean loadingState) {
        loading.setVisibility(loadingState ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loadingState);
        usernameInput.setEnabled(!loadingState);
        passwordInput.setEnabled(!loadingState);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void onLoginSuccess(Player player) {
        String message = "Welcome, " + (player.getUsername() != null ? player.getUsername() : "player") + "!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PlayerProfileActivity.class);
        intent.putExtra(PlayerProfileActivity.EXTRA_PLAYER_ID, player.getId());
        startActivity(intent);
    }
}
