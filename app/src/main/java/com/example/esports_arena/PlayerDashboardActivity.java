package com.example.esports_arena;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.model.Player;

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

    private PlayerRepository playerRepository;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_dashboard);

        playerRepository = new PlayerRepository();

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

        int playerId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
        if (playerId == -1) {
            Toast.makeText(this, "Missing player id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPlayer(playerId);
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

            bind(player);
        });
    }

    private void bind(Player player) {
        playerName.setText(player.getUsername() != null ? player.getUsername() : "Player");
        playerEmail.setText(player.getEmail() != null ? player.getEmail() : "");
        playerRole.setText(player.getRole() != null ? player.getRole() : "");

        if (player.getTeamId() != null) {
            teamInfo.setText("Team: " + player.getTeamId());
        } else {
            teamInfo.setText("Team: None");
        }

        statsKills.setText("Kills: " + player.getTotalKills());
        statsDeaths.setText("Deaths: " + player.getTotalDeaths());
        statsAssists.setText("Assists: " + player.getTotalAssists());
        statsKd.setText("K/D: " + decimalFormat.format(player.getKdRatio()));
        statsWinRate.setText("Win Rate: " + decimalFormat.format(player.getWinRate()) + "%");
    }

    private void setLoading(boolean loadingState) {
        loading.setVisibility(loadingState ? View.VISIBLE : View.GONE);
    }
}
