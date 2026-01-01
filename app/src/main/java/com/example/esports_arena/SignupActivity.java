package com.example.esports_arena;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.model.Player;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Signup screen that creates a new Player in Firebase.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText realNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private MaterialAutoCompleteTextView roleInput;
    private TextView statusText;
    private ProgressBar loading;
    private PlayerRepository playerRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        usernameInput = findViewById(R.id.signupUsername);
        realNameInput = findViewById(R.id.signupRealName);
        emailInput = findViewById(R.id.signupEmail);
        roleInput = findViewById(R.id.signupRole);
        passwordInput = findViewById(R.id.signupPassword);
        confirmInput = findViewById(R.id.signupConfirm);
        statusText = findViewById(R.id.signupStatus);
        loading = findViewById(R.id.signupLoading);
        Button submit = findViewById(R.id.signupSubmit);

        playerRepository = new PlayerRepository();

        String[] roles = new String[] {"IGL", "Rifler", "Support", "Entry Fragger", "Sniper", "Lurker"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roles);
        roleInput.setAdapter(roleAdapter);

        submit.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        statusText.setText("");

        String username = safeText(usernameInput);
        String realName = safeText(realNameInput);
        String email = safeText(emailInput);
        String password = safeText(passwordInput);
        String confirm = safeText(confirmInput);
        String role = safeText(roleInput);

        if (TextUtils.isEmpty(username)) {
            statusText.setText("Username required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            statusText.setText("Email required");
            return;
        }
        if (TextUtils.isEmpty(role)) {
            statusText.setText("Role required");
            return;
        }
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            statusText.setText("Password required");
            return;
        }
        if (!password.equals(confirm)) {
            statusText.setText("Passwords do not match");
            return;
        }

        loading.setVisibility(View.VISIBLE);

        // Generate a basic numeric id to align with existing player ids.
        int playerId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        Player newPlayer = new Player();
        newPlayer.setId(playerId);
        newPlayer.setUsername(username);
        newPlayer.setRealName(realName.isEmpty() ? null : realName);
        newPlayer.setEmail(email.trim().toLowerCase());
        newPlayer.setPassword(password);
        newPlayer.setRole(role);
        newPlayer.setTeamId(null);
        newPlayer.setAvailable(true);
        newPlayer.setAvailabilityReason(null);
        newPlayer.setTotalKills(0);
        newPlayer.setTotalDeaths(0);
        newPlayer.setTotalAssists(0);
        newPlayer.setMatchesPlayed(0);
        newPlayer.setMatchesWon(0);
        newPlayer.setJoinDate(null);

        playerRepository.create(newPlayer).addOnCompleteListener(task -> {
            loading.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Account created. Please log in.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                statusText.setText("Failed to create account. Try again.");
            }
        });
    }

    private String safeText(TextView input) {
        if (input == null || input.getText() == null) {
            return "";
        }
        return input.getText().toString().trim();
    }
}
