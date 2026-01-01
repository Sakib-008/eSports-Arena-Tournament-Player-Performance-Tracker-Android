package com.example.esports_arena;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.data.TeamRepository;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.Team;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class PlayerProfileActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYER_ID = "extra_player_id";

    private TextView profileUsername;
    private TextView profileRealName;
    private TextView profileRole;
    private TextView profileEmail;
    private TextView profileTeam;
    private SwitchMaterial profileAvailabilitySwitch;
    private TextInputEditText profileAvailabilityReasonInput;
    private Button profileUpdateAvailabilityButton;
    private ProgressBar profileLoading;
    private TextView profileStatus;

    private PlayerRepository playerRepository;
    private TeamRepository teamRepository;
    private Player currentPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_profile);

        profileUsername = findViewById(R.id.profileUsername);
        profileRealName = findViewById(R.id.profileRealName);
        profileRole = findViewById(R.id.profileRole);
        profileEmail = findViewById(R.id.profileEmail);
        profileTeam = findViewById(R.id.profileTeam);
        profileAvailabilitySwitch = findViewById(R.id.profileAvailabilitySwitch);
        profileAvailabilityReasonInput = findViewById(R.id.profileAvailabilityReasonInput);
        profileUpdateAvailabilityButton = findViewById(R.id.profileUpdateAvailabilityButton);
        profileLoading = findViewById(R.id.profileLoading);
        profileStatus = findViewById(R.id.profileStatus);

        playerRepository = new PlayerRepository();
        teamRepository = new TeamRepository();

        int playerId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
        if (playerId == -1) {
            Toast.makeText(this, "Missing player id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPlayer(playerId);

        profileUpdateAvailabilityButton.setOnClickListener(v -> updateAvailability());
    }

    private void loadPlayer(int playerId) {
        setLoading(true);
        playerRepository.getById(playerId).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                profileStatus.setText("Failed to load player");
                return;
            }

            Player player = task.getResult();
            if (player == null) {
                profileStatus.setText("Player not found");
                return;
            }

            currentPlayer = player;
            bind(player);
        });
    }

    private void bind(Player player) {
        profileUsername.setText(player.getUsername() != null ? player.getUsername() : "Player");
        profileRealName.setText(player.getRealName() != null ? player.getRealName() : "");
        profileRole.setText(player.getRole() != null ? player.getRole() : "");
        profileEmail.setText(player.getEmail() != null ? player.getEmail() : "");

        if (player.getTeamId() != null) {
            profileTeam.setText("Team: " + player.getTeamId());
            loadTeam(player.getTeamId());
        } else {
            profileTeam.setText("Team: None");
        }

        profileAvailabilitySwitch.setChecked(player.isAvailable());
        if (player.getAvailabilityReason() != null) {
            profileAvailabilityReasonInput.setText(player.getAvailabilityReason());
        } else {
            profileAvailabilityReasonInput.setText("");
        }
    }

    private void loadTeam(int teamId) {
        teamRepository.getById(teamId).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return;
            }
            Team team = task.getResult();
            String tag = team.getTag() != null ? team.getTag() : "";
            String name = team.getName() != null ? team.getName() : ("Team " + teamId);
            profileTeam.setText(tag.isEmpty() ? name : name + " (" + tag + ")");
        });
    }

    private void updateAvailability() {
        if (currentPlayer == null) {
            profileStatus.setText("No player loaded");
            return;
        }
        boolean available = profileAvailabilitySwitch.isChecked();
        String reason = profileAvailabilityReasonInput.getText() != null
                ? profileAvailabilityReasonInput.getText().toString().trim()
                : "";

        currentPlayer.setAvailable(available);
        currentPlayer.setAvailabilityReason(reason.isEmpty() ? null : reason);

        setLoading(true);
        playerRepository.update(currentPlayer).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                profileStatus.setText(available ? "Set to available" : "Set to unavailable");
            } else {
                profileStatus.setText("Failed to update availability");
            }
        });
    }

    private void setLoading(boolean loading) {
        profileLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        profileUpdateAvailabilityButton.setEnabled(!loading);
        profileAvailabilitySwitch.setEnabled(!loading);
        profileAvailabilityReasonInput.setEnabled(!loading);
    }
}
