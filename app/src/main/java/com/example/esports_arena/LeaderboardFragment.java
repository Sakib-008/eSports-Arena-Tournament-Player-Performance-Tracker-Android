package com.example.esports_arena;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.data.TeamRepository;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.Team;
import com.example.esports_arena.ui.LeaderboardAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderboardFragment extends Fragment {

    private static final String ARG_PLAYER_ID = "arg_player_id";

    private RecyclerView leaderboardRecycler;
    private ProgressBar leaderboardLoading;
    private TextView leaderboardStatus;
    private MaterialAutoCompleteTextView leaderboardTypeSelector;
    private MaterialAutoCompleteTextView leaderboardScopeSelector;
    private TextView leaderboardTitle;

    private LeaderboardAdapter leaderboardAdapter;
    private PlayerRepository playerRepository;
    private TeamRepository teamRepository;
    private int playerId;

    private List<Player> allPlayers = new ArrayList<>();
    private List<Team> allTeams = new ArrayList<>();
    private String currentType = "Players"; // "Players" or "Teams"
    private String currentScope = "Overall";

    public static LeaderboardFragment newInstance(int playerId) {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        leaderboardRecycler = view.findViewById(R.id.leaderboardRecycler);
        leaderboardLoading = view.findViewById(R.id.leaderboardLoading);
        leaderboardStatus = view.findViewById(R.id.leaderboardStatus);
        leaderboardTypeSelector = view.findViewById(R.id.leaderboardTypeSelector);
        leaderboardScopeSelector = view.findViewById(R.id.leaderboardScopeSelector);
        leaderboardTitle = view.findViewById(R.id.leaderboardTitle);

        leaderboardAdapter = new LeaderboardAdapter();
        leaderboardRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        leaderboardRecycler.setAdapter(leaderboardAdapter);

        playerRepository = new PlayerRepository();
        teamRepository = new TeamRepository();

        playerId = getArguments() != null ? getArguments().getInt(ARG_PLAYER_ID, -1) : -1;
        if (playerId == -1) {
            leaderboardStatus.setText("Missing player id");
            return;
        }

        setupTypeSelector();
        setupScopeSelector();
        loadAllData();
    }

    private void setupTypeSelector() {
        String[] types = {"Players", "Teams"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, types);
        leaderboardTypeSelector.setAdapter(adapter);
        leaderboardTypeSelector.setText("Players", false);
        leaderboardTypeSelector.setOnItemClickListener((parent, v, position, id) -> {
            currentType = position == 0 ? "Players" : "Teams";
            currentScope = "Overall";
            updateScopeSelector();
            refreshLeaderboard();
        });
    }

    private void setupScopeSelector() {
        String[] scopes = {"Overall"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, scopes);
        leaderboardScopeSelector.setAdapter(adapter);
        leaderboardScopeSelector.setText("Overall", false);
        leaderboardScopeSelector.setOnItemClickListener((parent, v, position, id) -> {
            currentScope = leaderboardScopeSelector.getText().toString();
            refreshLeaderboard();
        });
    }

    private void updateScopeSelector() {
        List<String> scopes = new ArrayList<>();
        scopes.add("Overall");
        
        if ("Players".equals(currentType)) {
            // First try to fetch tournaments directly from database structure
            playerRepository.getTournamentNames().addOnCompleteListener(task -> {
                List<String> finalScopes = new ArrayList<>(scopes);
                
                if (task.isSuccessful() && task.getResult() != null) {
                    List<String> tournaments = task.getResult();
                    android.util.Log.d("Leaderboard", "Got " + tournaments.size() + " tournaments from database");
                    finalScopes.addAll(tournaments);
                } else {
                    // Fallback: collect from player objects
                    android.util.Log.d("Leaderboard", "Failed to fetch tournaments from DB, using player objects");
                    for (Player p : allPlayers) {
                        if (p.getTournamentStats() != null && !p.getTournamentStats().isEmpty()) {
                            finalScopes.addAll(p.getTournamentStats().keySet());
                            android.util.Log.d("Leaderboard", "Player " + p.getUsername() + " has tournaments: " + p.getTournamentStats().keySet());
                        }
                    }
                }
                
                // Remove duplicates
                List<String> distinctScopes = finalScopes.stream().distinct().collect(Collectors.toList());
                android.util.Log.d("Leaderboard", "Final scopes: " + distinctScopes);
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, distinctScopes);
                leaderboardScopeSelector.setAdapter(adapter);
                leaderboardScopeSelector.setText(distinctScopes.get(0), false);
            });
        } else {
            // For teams, just use Overall for now
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, scopes);
            leaderboardScopeSelector.setAdapter(adapter);
            leaderboardScopeSelector.setText(scopes.get(0), false);
        }
    }

    private void loadAllData() {
        setLoading(true);
        playerRepository.getAll().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allPlayers = task.getResult();
                android.util.Log.d("Leaderboard", "Loaded " + allPlayers.size() + " players");
                for (Player p : allPlayers) {
                    if (p.getTournamentStats() != null) {
                        android.util.Log.d("Leaderboard", "Player: " + p.getUsername() + " Tournament count: " + p.getTournamentStats().size());
                    }
                }
                
                // Also fetch tournament names from database structure
                playerRepository.getTournamentNames().addOnCompleteListener(tournaTask -> {
                    if (tournaTask.isSuccessful() && tournaTask.getResult() != null) {
                        List<String> tournaments = tournaTask.getResult();
                        android.util.Log.d("Leaderboard", "Tournaments from DB: " + tournaments);
                    }
                    updateScopeSelector();
                    refreshLeaderboard();
                });
            } else {
                leaderboardStatus.setText("Failed to load data");
                android.util.Log.e("Leaderboard", "Failed to load players");
                setLoading(false);
            }
        });
        
        teamRepository.getAll().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allTeams = task.getResult();
                android.util.Log.d("Leaderboard", "Loaded " + allTeams.size() + " teams");
            }
        });
    }

    private void refreshLeaderboard() {
        if ("Players".equals(currentType)) {
            displayPlayerLeaderboard();
        } else {
            displayTeamLeaderboard();
        }
    }

    private void displayPlayerLeaderboard() {
        setLoading(true);
        leaderboardTitle.setText("Player Leaderboard - " + currentScope);
        
        List<LeaderboardAdapter.LeaderboardEntry> entries = new ArrayList<>();
        
        android.util.Log.d("Leaderboard", "Displaying players for scope: " + currentScope);
        
        for (Player p : allPlayers) {
            int kills, deaths, assists, matches, won;
            
            if ("Overall".equals(currentScope)) {
                kills = p.getTotalKills();
                deaths = p.getTotalDeaths();
                assists = p.getTotalAssists();
                matches = p.getMatchesPlayed();
                won = p.getMatchesWon();
            } else {
                if (p.getTournamentStats() == null) {
                    android.util.Log.d("Leaderboard", "Player " + p.getUsername() + " has no tournament stats");
                    continue;
                }
                if (!p.getTournamentStats().containsKey(currentScope)) {
                    android.util.Log.d("Leaderboard", "Player " + p.getUsername() + " not in tournament " + currentScope);
                    continue;
                }
                com.example.esports_arena.model.TournamentStats ts = p.getTournamentStats().get(currentScope);
                if (ts == null) {
                    android.util.Log.d("Leaderboard", "Tournament stats null for " + p.getUsername() + " in " + currentScope);
                    continue;
                }
                kills = ts.getKills();
                deaths = ts.getDeaths();
                assists = ts.getAssists();
                matches = ts.getMatchesPlayed();
                won = ts.getMatchesWon();
            }
            
            double kd = deaths == 0 ? kills : (double) kills / deaths;
            double wr = matches == 0 ? 0.0 : (double) won / matches * 100.0;
            
            entries.add(new LeaderboardAdapter.LeaderboardEntry(
                    p.getUsername(),
                    "K/D: " + String.format("%.2f", kd),
                    "Matches: " + matches + " | W/L: " + won + "/" + (matches - won),
                    kd
            ));
        }
        
        android.util.Log.d("Leaderboard", "Total entries: " + entries.size());
        
        // Sort by K/D ratio descending
        entries.sort((a, b) -> Double.compare(b.score, a.score));
        
        leaderboardAdapter.setEntries(entries);
        setLoading(false);
    }

    private void displayTeamLeaderboard() {
        setLoading(true);
        leaderboardTitle.setText("Team Leaderboard - " + currentScope);
        
        List<LeaderboardAdapter.LeaderboardEntry> entries = new ArrayList<>();
        
        for (Team t : allTeams) {
            String name = t.getName() != null ? t.getName() : "Team " + t.getId();
            String tag = t.getTag() != null ? " (" + t.getTag() + ")" : "";
            String region = t.getRegion() != null ? t.getRegion() : "Unknown";
            
            entries.add(new LeaderboardAdapter.LeaderboardEntry(
                    name + tag,
                    "Region: " + region,
                    "ID: " + t.getId(),
                    t.getId()
            ));
        }
        
        leaderboardAdapter.setEntries(entries);
        setLoading(false);
    }

    private void setLoading(boolean loading) {
        leaderboardLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        leaderboardRecycler.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }
}
