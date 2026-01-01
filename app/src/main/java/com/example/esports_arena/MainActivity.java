package com.example.esports_arena;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.model.Player;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

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

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        statusText = findViewById(R.id.statusText);
        loading = findViewById(R.id.loading);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        statusText.setText("");
        String username = usernameInput.getText() != null ? usernameInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (TextUtils.isEmpty(username)) {
            statusText.setText("Username required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            statusText.setText("Password required");
            return;
        }

        setLoading(true);
        playerRepository.getByUsername(username).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                statusText.setText("Network error. Try again.");
                return;
            }

            Player player = task.getResult();
            if (player == null) {
                statusText.setText("User not found");
                return;
            }

            if (!password.equals(player.getPassword())) {
                statusText.setText("Invalid credentials");
                return;
            }

            onLoginSuccess(player);
        });
    }

    private void setLoading(boolean loadingState) {
        loading.setVisibility(loadingState ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loadingState);
    }

    private void onLoginSuccess(Player player) {
        String message = "Welcome, " + (player.getUsername() != null ? player.getUsername() : "player") + "!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Next step: navigate to player dashboard once implemented.
    }
}