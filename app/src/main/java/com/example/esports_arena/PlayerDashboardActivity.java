package com.example.esports_arena;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.data.TeamRepository;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.Team;
import com.example.esports_arena.ui.TeamRosterAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;

public class PlayerDashboardActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYER_ID = "extra_player_id";

    private TextView playerName;
    private TextView playerEmail;
    private TextView playerRole;
    private TextView teamInfo;
    private TextView statsKills;
    private TextView statsDeaths;
    private TextView statsAssists;
    private TextView statsKd;
    private TextView statsWinRate;
    private TextView status;
    private ProgressBar loading;
    private SwitchMaterial availabilitySwitch;
    private TextInputEditText availabilityReasonInput;
    private Button updateAvailabilityButton;
    private RecyclerView teamRosterList;
    private TeamRosterAdapter teamRosterAdapter;
    private Button leaderVoteButton;

    private PlayerRepository playerRepository;
    private TeamRepository teamRepository;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private Player currentPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_dashboard);

        playerRepository = new PlayerRepository();
        teamRepository = new TeamRepository();

        playerName = findViewById(R.id.playerName);
        playerEmail = findViewById(R.id.playerEmail);
        playerRole = findViewById(R.id.playerRole);
        teamInfo = findViewById(R.id.teamInfo);
        statsKills = findViewById(R.id.statsKills);
        statsDeaths = findViewById(R.id.statsDeaths);
        statsAssists = findViewById(R.id.statsAssists);
        statsKd = findViewById(R.id.statsKd);
        statsWinRate = findViewById(R.id.statsWinRate);
        status = findViewById(R.id.dashboardStatus);
        loading = findViewById(R.id.dashboardLoading);
        availabilitySwitch = findViewById(R.id.availabilitySwitch);
        availabilityReasonInput = findViewById(R.id.availabilityReasonInput);
        updateAvailabilityButton = findViewById(R.id.updateAvailabilityButton);
        teamRosterList = findViewById(R.id.teamRosterList);
        leaderVoteButton = findViewById(R.id.leaderVoteButton);

        teamRosterAdapter = new TeamRosterAdapter();
        teamRosterList.setLayoutManager(new LinearLayoutManager(this));
        teamRosterList.setAdapter(teamRosterAdapter);

        int playerId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
        if (playerId == -1) {
            Toast.makeText(this, "Missing player id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPlayer(playerId);

        updateAvailabilityButton.setOnClickListener(v -> updateAvailability());
        leaderVoteButton.setOnClickListener(v -> openLeaderVote());
    }

    private void loadPlayer(int playerId) {
        setLoading(true);
        playerRepository.getById(playerId).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                status.setText("Failed to load player");
                return;
            }

            Player player = task.getResult();
            if (player == null) {
                status.setText("Player not found");
                return;
            }

            currentPlayer = player;
            bind(player);
        });
    }

    private void bind(Player player) {
        playerName.setText(player.getUsername() != null ? player.getUsername() : "Player");
        playerEmail.setText(player.getEmail() != null ? player.getEmail() : "");
        playerRole.setText(player.getRole() != null ? player.getRole() : "");

        if (player.getTeamId() != null) {
            teamInfo.setText("Team: " + player.getTeamId());
            loadTeamAndRoster(player.getTeamId());
            leaderVoteButton.setEnabled(true);
        } else {
            teamInfo.setText("Team: None");
            teamRosterAdapter.setPlayers(null);
            leaderVoteButton.setEnabled(false);
        }

        statsKills.setText("Kills: " + player.getTotalKills());
        statsDeaths.setText("Deaths: " + player.getTotalDeaths());
        statsAssists.setText("Assists: " + player.getTotalAssists());
        statsKd.setText("K/D: " + decimalFormat.format(player.getKdRatio()));
        statsWinRate.setText("Win Rate: " + decimalFormat.format(player.getWinRate()) + "%");

        availabilitySwitch.setChecked(player.isAvailable());
        if (player.getAvailabilityReason() != null) {
            availabilityReasonInput.setText(player.getAvailabilityReason());
        } else {
            availabilityReasonInput.setText("");
        }
    }

    private void setLoading(boolean loadingState) {
        loading.setVisibility(loadingState ? View.VISIBLE : View.GONE);
        updateAvailabilityButton.setEnabled(!loadingState);
    }

    private void loadTeamAndRoster(int teamId) {
        setLoading(true);
        teamRepository.getById(teamId).addOnCompleteListener(teamTask -> {
            if (teamTask.isSuccessful()) {
                Team team = teamTask.getResult();
                if (team != null && team.getName() != null) {
                    String tag = team.getTag() != null ? team.getTag() : "";
                    teamInfo.setText(team.getName() + (tag.isEmpty() ? "" : " (" + tag + ")"));
                }
            } else {
                Log.w("ROSTER", "Team load failed", teamTask.getException());
            }

            playerRepository.getByTeamId(teamId).addOnCompleteListener(playersTask -> {
                setLoading(false);
                if (playersTask.isSuccessful()) {
                    if (playersTask.getResult() != null) {
                        Log.d("ROSTER", "Loaded " + playersTask.getResult().size() + " players for team " + teamId);
                        teamRosterAdapter.setPlayers(playersTask.getResult());
                        if (playersTask.getResult().isEmpty()) {
                            status.setText("No teammates found for team " + teamId);
                        }
                    } else {
                        Log.d("ROSTER", "No roster result for team " + teamId);
                        status.setText("No teammates found for team " + teamId);
                        teamRosterAdapter.setPlayers(null);
                    }
                } else {
                    status.setText("Failed to load team roster");
                    Log.w("ROSTER", "Roster load failed", playersTask.getException());
                }
            });
        });
    }

    private void updateAvailability() {
        if (currentPlayer == null) {
            status.setText("No player loaded");
            return;
        }

        boolean available = availabilitySwitch.isChecked();
        String reason = availabilityReasonInput.getText() != null ? availabilityReasonInput.getText().toString().trim() : "";

        currentPlayer.setAvailable(available);
        currentPlayer.setAvailabilityReason(reason.isEmpty() ? null : reason);

        setLoading(true);
        playerRepository.update(currentPlayer).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                status.setText(available ? "Set to available" : "Set to unavailable");
            } else {
                status.setText("Failed to update availability");
            }
        });
    }

    private void openLeaderVote() {
        if (currentPlayer == null || currentPlayer.getTeamId() == null) {
            status.setText("No team available for voting");
            return;
        }

        Intent intent = new Intent(this, LeaderVoteActivity.class);
        intent.putExtra(LeaderVoteActivity.EXTRA_TEAM_ID, currentPlayer.getTeamId());
        intent.putExtra(LeaderVoteActivity.EXTRA_PLAYER_ID, currentPlayer.getId());
        startActivity(intent);
    }
}
