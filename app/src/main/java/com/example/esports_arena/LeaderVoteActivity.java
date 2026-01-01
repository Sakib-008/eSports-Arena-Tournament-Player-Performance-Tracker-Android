package com.example.esports_arena;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.data.LeaderVoteRepository;
import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.model.LeaderVote;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.ui.LeaderVoteAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderVoteActivity extends AppCompatActivity {

    public static final String EXTRA_TEAM_ID = "extra_team_id";
    public static final String EXTRA_PLAYER_ID = "extra_player_id";

    private RecyclerView candidatesList;
    private ProgressBar loading;
    private TextView resultsText;
    private TextView status;

    private final Map<Integer, Integer> voteCounts = new HashMap<>();
    private LeaderVoteAdapter adapter;

    private PlayerRepository playerRepository;
    private LeaderVoteRepository voteRepository;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "leader_votes";

    private int teamId;
    private int voterId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leader_vote);

        candidatesList = findViewById(R.id.candidatesList);
        loading = findViewById(R.id.voteLoading);
        resultsText = findViewById(R.id.resultsText);
        status = findViewById(R.id.voteStatus);

        teamId = getIntent().getIntExtra(EXTRA_TEAM_ID, -1);
        voterId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
        if (teamId == -1 || voterId == -1) {
            Toast.makeText(this, "Missing team or player", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        playerRepository = new PlayerRepository();
        voteRepository = new LeaderVoteRepository();

        adapter = new LeaderVoteAdapter(voteCounts, this::castVote);
        candidatesList.setLayoutManager(new LinearLayoutManager(this));
        candidatesList.setAdapter(adapter);

        adapter.setVotingEnabled(!hasAlreadyVoted());

        loadCandidates();
        loadResults();
    }

    private void loadCandidates() {
        setLoading(true);
        playerRepository.getByTeamId(teamId).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                status.setText("Failed to load candidates");
                return;
            }
            List<Player> players = task.getResult();
            adapter.setCandidates(players);
        });
    }

    private void loadResults() {
        voteRepository.getResults(teamId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                voteCounts.clear();
                voteCounts.putAll(task.getResult());
                adapter.updateCounts(voteCounts);
                resultsText.setText(formatResults());
            }
        });
    }

    private String formatResults() {
        if (voteCounts.isEmpty()) {
            return "No votes yet";
        }
        StringBuilder sb = new StringBuilder("Current votes:\n");
        for (Map.Entry<Integer, Integer> entry : voteCounts.entrySet()) {
            sb.append("• Candidate ").append(entry.getKey())
                    .append(": ").append(entry.getValue()).append(" votes\n");
        }
        return sb.toString().trim();
    }

    private void castVote(Player candidate) {
        status.setText("");
        if (hasAlreadyVoted()) {
            status.setText("You already voted");
            adapter.setVotingEnabled(false);
            return;
        }
        LeaderVote vote = new LeaderVote(teamId, voterId, candidate.getId(), System.currentTimeMillis());
        setLoading(true);
        voteRepository.submitVote(vote).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                status.setText("Vote cast for " + candidate.getUsername());
                markVoted(candidate.getId());
                adapter.setVotingEnabled(false);
                loadResults();
            } else {
                status.setText("Failed to cast vote");
            }
        });
    }

    private void setLoading(boolean state) {
        loading.setVisibility(state ? View.VISIBLE : View.GONE);
        candidatesList.setEnabled(!state);
    }

    private boolean hasAlreadyVoted() {
        String key = voteKey();
        return prefs.getInt(key, -1) != -1;
    }

    private void markVoted(int candidateId) {
        prefs.edit().putInt(voteKey(), candidateId).apply();
    }

    private String voteKey() {
        return "team_" + teamId + "_voter_" + voterId;
    }
}
