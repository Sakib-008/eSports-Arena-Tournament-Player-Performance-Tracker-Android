package com.example.esports_arena;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

        // Debug probe: log player count to verify Firebase connectivity
        // playerRepository.getAll().addOnCompleteListener(t -> {
        //     if (!t.isSuccessful()) {
        //         Log.e("DB_CHECK", "Error loading players", t.getException());
        //     } else if (t.getResult() != null) {
        //         Log.d("DB_CHECK", "Players count: " + t.getResult().size());
        //         if (!t.getResult().isEmpty()) {
        //             Player first = t.getResult().get(0);
        //             Log.d("DB_CHECK", "First player username: " + first.getUsername());
        //         }
        //     }
        // });
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
                // Fallback: case-insensitive scan to help debugging
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
                        Log.d("LOGIN", "Known usernames sample: " + sampleUsernames(allTask.getResult()));
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

    private String sampleUsernames(java.util.List<Player> players) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Player p : players) {
            if (p.getUsername() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(p.getUsername());
                count++;
                if (count >= 5) break;
            }
        }
        return sb.toString();
    }

    private void setLoading(boolean loadingState) {
        loading.setVisibility(loadingState ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loadingState);
    }

    private void onLoginSuccess(Player player) {
        String message = "Welcome, " + (player.getUsername() != null ? player.getUsername() : "player") + "!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, PlayerDashboardActivity.class);
        intent.putExtra(PlayerDashboardActivity.EXTRA_PLAYER_ID, player.getId());
        startActivity(intent);
    }
}