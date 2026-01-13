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
import com.example.esports_arena.data.MatchRepository;
import com.example.esports_arena.data.TournamentRepository;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.Team;
import com.example.esports_arena.model.Match;
import com.example.esports_arena.model.Tournament;
import com.example.esports_arena.model.TournamentStats;
import com.example.esports_arena.service.TournamentStatsService;
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
    private MatchRepository matchRepository;
    private TournamentRepository tournamentRepository;
    private TournamentStatsService tournamentStatsService;
    private int playerId;

    private List<Player> allPlayers = new ArrayList<>();
    private List<Team> allTeams = new ArrayList<>();
    private List<Match> allMatches = new ArrayList<>();
    private List<Tournament> allTournaments = new ArrayList<>();
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
        matchRepository = new MatchRepository();
        tournamentRepository = new TournamentRepository();
        tournamentStatsService = new TournamentStatsService();

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
        
        // Make it non-editable
        leaderboardTypeSelector.setKeyListener(null);
        leaderboardTypeSelector.setInputType(0);
        leaderboardTypeSelector.setFocusable(false);
        
        leaderboardTypeSelector.setOnItemClickListener((parent, v, position, id) -> {
            currentType = (String) parent.getItemAtPosition(position);
            android.util.Log.d("Leaderboard", "Type selected: " + currentType);
            leaderboardTypeSelector.setText(currentType, false);
            currentScope = "Overall";
            updateScopeSelector();
            refreshLeaderboard();
        });
        
        leaderboardTypeSelector.setOnClickListener(v -> {
            leaderboardTypeSelector.showDropDown();
        });
    }

    private void setupScopeSelector() {
        String[] scopes = {"Overall"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, scopes);
        leaderboardScopeSelector.setAdapter(adapter);
        leaderboardScopeSelector.setText("Overall", false);
        
        // Make it non-editable
        leaderboardScopeSelector.setKeyListener(null);
        leaderboardScopeSelector.setInputType(0);
        leaderboardScopeSelector.setFocusable(false);
        
        leaderboardScopeSelector.setOnItemClickListener((parent, v, position, id) -> {
            currentScope = (String) parent.getItemAtPosition(position);
            android.util.Log.d("Leaderboard", "Scope selected: " + currentScope);
            leaderboardScopeSelector.setText(currentScope, false);
            refreshLeaderboard();
        });
        
        leaderboardScopeSelector.setOnClickListener(v -> {
            leaderboardScopeSelector.showDropDown();
        });
    }

    private void updateScopeSelector() {
        List<String> scopes = new ArrayList<>();
        scopes.add("Overall");
        
        // Both Players and Teams can have tournament scopes
        if ("Players".equals(currentType)) {
            // Fetch tournaments directly from database
            playerRepository.getTournamentNames().addOnCompleteListener(task -> {
                List<String> finalScopes = new ArrayList<>(scopes);
                
                if (task.isSuccessful() && task.getResult() != null) {
                    List<String> tournaments = task.getResult();
                    android.util.Log.d("Leaderboard", "Got " + tournaments.size() + " tournaments from database");
                    finalScopes.addAll(tournaments);
                }
                
                // Remove duplicates
                List<String> distinctScopes = finalScopes.stream().distinct().collect(Collectors.toList());
                android.util.Log.d("Leaderboard", "Final scopes: " + distinctScopes);
                
                // IMPORTANT: Set listeners BEFORE setting adapter
                leaderboardScopeSelector.setOnItemClickListener((parent, v, position, id) -> {
                    String selected = (String) parent.getItemAtPosition(position);
                    android.util.Log.d("Leaderboard", "$$$$$ Scope item clicked - position: " + position + ", selected: '" + selected + "' $$$$$");
                    leaderboardScopeSelector.setText(selected, false);
                    currentScope = selected;
                    refreshLeaderboard();
                });
                
                leaderboardScopeSelector.setOnClickListener(v -> {
                    android.util.Log.d("Leaderboard", "@@@@@ Scope selector clicked - showing dropdown @@@@@");
                    leaderboardScopeSelector.showDropDown();
                });
                
                // NOW set the adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, distinctScopes);
                leaderboardScopeSelector.setAdapter(adapter);
                leaderboardScopeSelector.setText(distinctScopes.get(0), false);
                leaderboardScopeSelector.setThreshold(1);
            });
        } else {
            // For teams, also load tournament scopes
            playerRepository.getTournamentNames().addOnCompleteListener(task -> {
                List<String> finalScopes = new ArrayList<>(scopes);
                
                if (task.isSuccessful() && task.getResult() != null) {
                    List<String> tournaments = task.getResult();
                    android.util.Log.d("Leaderboard", "Got " + tournaments.size() + " tournaments for teams");
                    finalScopes.addAll(tournaments);
                }
                
                // Remove duplicates
                List<String> distinctScopes = finalScopes.stream().distinct().collect(Collectors.toList());
                
                // Set listeners BEFORE setting adapter
                leaderboardScopeSelector.setOnItemClickListener((parent, v, position, id) -> {
                    String selected = (String) parent.getItemAtPosition(position);
                    android.util.Log.d("Leaderboard", "Team scope selected: " + selected);
                    leaderboardScopeSelector.setText(selected, false);
                    currentScope = selected;
                    refreshLeaderboard();
                });
                
                leaderboardScopeSelector.setOnClickListener(v -> {
                    leaderboardScopeSelector.showDropDown();
                });
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, distinctScopes);
                leaderboardScopeSelector.setAdapter(adapter);
                leaderboardScopeSelector.setText(distinctScopes.get(0), false);
                leaderboardScopeSelector.setThreshold(1);
            });
        }
    }

    private void loadAllData() {
        setLoading(true);
        
        // Load players
        playerRepository.getAll().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allPlayers = task.getResult();
                android.util.Log.d("Leaderboard", "Loaded " + allPlayers.size() + " players");
                
                // Load matches
                matchRepository.getAll().addOnCompleteListener(matchTask -> {
                    if (matchTask.isSuccessful() && matchTask.getResult() != null) {
                        allMatches = matchTask.getResult();
                        android.util.Log.d("Leaderboard", "Loaded " + allMatches.size() + " matches");
                    } else {
                        android.util.Log.e("Leaderboard", "Failed to load matches");
                        allMatches = new ArrayList<>();
                    }
                    
                    // Load tournaments
                    tournamentRepository.getAll().addOnCompleteListener(tournamentTask -> {
                        if (tournamentTask.isSuccessful() && tournamentTask.getResult() != null) {
                            allTournaments = tournamentTask.getResult();
                            android.util.Log.d("Leaderboard", "Loaded " + allTournaments.size() + " tournaments");
                        } else {
                            android.util.Log.e("Leaderboard", "Failed to load tournaments");
                            allTournaments = new ArrayList<>();
                        }
                        
                        updateScopeSelector();
                        refreshLeaderboard();
                    });
                });
            } else {
                leaderboardStatus.setText("Failed to load data");
                android.util.Log.e("Leaderboard", "Failed to load players");
                setLoading(false);
            }
        });
        
        // Load teams
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
        
        android.util.Log.d("Leaderboard", "===== Displaying players for scope: " + currentScope + " =====");
        
        if ("Overall".equals(currentScope)) {
            // Use overall stats from Player model
            for (Player p : allPlayers) {
                int kills = p.getTotalKills();
                int deaths = p.getTotalDeaths();
                int assists = p.getTotalAssists();
                int matches = p.getMatchesPlayed();
                int won = p.getMatchesWon();
                
                double kd = deaths == 0 ? kills : (double) kills / deaths;
                double wr = matches == 0 ? 0.0 : (double) won / matches * 100.0;
                
                entries.add(new LeaderboardAdapter.LeaderboardEntry(
                        p.getUsername(),
                        "K/D: " + String.format("%.2f", kd),
                        "Matches: " + matches + " | W/L: " + won + "/" + (matches - won),
                        kd
                ));
            }
        } else {
            // Calculate tournament-specific stats from matches
            int tournamentId = findTournamentIdByName(currentScope);
            
            if (tournamentId == -1) {
                android.util.Log.e("Leaderboard", "Tournament not found: " + currentScope);
                leaderboardStatus.setText("Tournament not found");
                setLoading(false);
                return;
            }
            
            android.util.Log.d("Leaderboard", "Calculating stats for tournament ID: " + tournamentId);
            
            for (Player p : allPlayers) {
                TournamentStats ts = tournamentStatsService.getPlayerTournamentStats(p.getId(), tournamentId, allMatches);
                
                if (ts.getMatchesPlayed() == 0) {
                    android.util.Log.d("Leaderboard", "Player " + p.getUsername() + " has 0 matches in tournament");
                    continue; // Skip players who didn't play in this tournament
                }
                
                int kills = ts.getKills();
                int deaths = ts.getDeaths();
                int assists = ts.getAssists();
                int matches = ts.getMatchesPlayed();
                int won = ts.getMatchesWon();
                
                double kd = deaths == 0 ? kills : (double) kills / deaths;
                double wr = matches == 0 ? 0.0 : (double) won / matches * 100.0;
                
                android.util.Log.d("Leaderboard", "Player " + p.getUsername() + ": K=" + kills + " D=" + deaths + " M=" + matches);
                
                entries.add(new LeaderboardAdapter.LeaderboardEntry(
                        p.getUsername(),
                        "K/D: " + String.format("%.2f", kd),
                        "Matches: " + matches + " | W/L: " + won + "/" + (matches - won),
                        kd
                ));
            }
        }
        
        android.util.Log.d("Leaderboard", "Total entries: " + entries.size());
        
        // Sort by K/D ratio descending
        entries.sort((a, b) -> Double.compare(b.score, a.score));
        
        leaderboardAdapter.setEntries(entries);
        setLoading(false);
    }
    
    private int findTournamentIdByName(String tournamentName) {
        if (allTournaments == null || allTournaments.isEmpty()) {
            android.util.Log.e("Leaderboard", "Tournaments list is null or empty");
            return -1;
        }
        
        for (Tournament tournament : allTournaments) {
            if (tournament.getName() != null && tournament.getName().equals(tournamentName)) {
                android.util.Log.d("Leaderboard", "Found tournament ID: " + tournament.getId() + " for name: " + tournamentName);
                return tournament.getId();
            }
        }
        
        android.util.Log.w("Leaderboard", "Tournament not found: " + tournamentName);
        return -1;
    }

    private void displayTeamLeaderboard() {
        setLoading(true);
        leaderboardTitle.setText("Team Leaderboard - " + currentScope);
        
        List<LeaderboardAdapter.LeaderboardEntry> entries = new ArrayList<>();
        
        android.util.Log.d("Leaderboard", "===== Displaying teams for scope: " + currentScope + " =====");
        
        if ("Overall".equals(currentScope)) {
            // Show all teams with basic info
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
        } else {
            // Calculate tournament-specific stats for teams
            int tournamentId = findTournamentIdByName(currentScope);
            
            if (tournamentId == -1) {
                android.util.Log.e("Leaderboard", "Tournament not found: " + currentScope);
                leaderboardStatus.setText("Tournament not found");
                setLoading(false);
                return;
            }
            
            android.util.Log.d("Leaderboard", "Calculating team stats for tournament ID: " + tournamentId);
            
            for (Team t : allTeams) {
                // Calculate team stats from matches
                int matchesPlayed = 0;
                int matchesWon = 0;
                int totalKills = 0;
                int totalDeaths = 0;
                
                for (Match m : allMatches) {
                    if (m == null) continue;
                    
                    android.util.Log.d("Leaderboard", "  Checking match " + m.getId() + ": tournamentId=" + m.getTournamentId() + ", team1=" + m.getTeam1Id() + ", team2=" + m.getTeam2Id() + ", winnerId=" + m.getWinnerId());
                    
                    if (m.getTournamentId() != tournamentId) {
                        continue;
                    }
                    
                    if (m.getTeam1Id() == t.getId() || m.getTeam2Id() == t.getId()) {
                        matchesPlayed++;
                        android.util.Log.d("Leaderboard", "    Team " + t.getName() + " played this match");
                        
                        // Check if team won - SAFE null check
                        Integer winnerId = m.getWinnerId();
                        if (winnerId != null && winnerId == t.getId()) {
                            matchesWon++;
                            android.util.Log.d("Leaderboard", "    Team " + t.getName() + " WON this match");
                        }
                        
                        // Add team's kills and deaths
                        if (m.getTeam1Id() == t.getId()) {
                            totalKills += m.getTeam1Score();
                            totalDeaths += m.getTeam2Score();
                        } else {
                            totalKills += m.getTeam2Score();
                            totalDeaths += m.getTeam1Score();
                        }
                    }
                }
                
                if (matchesPlayed == 0) {
                    android.util.Log.d("Leaderboard", "Team " + t.getName() + " has 0 matches in tournament");
                    continue; // Skip teams that didn't play in this tournament
                }
                
                double winRate = matchesPlayed == 0 ? 0.0 : (double) matchesWon / matchesPlayed * 100.0;
                double kd = totalDeaths == 0 ? totalKills : (double) totalKills / totalDeaths;
                
                String name = t.getName() != null ? t.getName() : "Team " + t.getId();
                String tag = t.getTag() != null ? " (" + t.getTag() + ")" : "";
                
                android.util.Log.d("Leaderboard", "Team " + name + ": Matches=" + matchesPlayed + " Won=" + matchesWon + " K/D=" + String.format("%.2f", kd));
                
                entries.add(new LeaderboardAdapter.LeaderboardEntry(
                        name + tag,
                        "Win Rate: " + String.format("%.1f%%", winRate) + " | K/D: " + String.format("%.2f", kd),
                        "Matches: " + matchesPlayed + " | W/L: " + matchesWon + "/" + (matchesPlayed - matchesWon),
                        winRate // Sort by win rate
                ));
            }
        }
        
        android.util.Log.d("Leaderboard", "Total team entries: " + entries.size());
        
        // Sort by score (win rate for tournament scope, ID for overall) descending
        entries.sort((a, b) -> Double.compare(b.score, a.score));
        
        leaderboardAdapter.setEntries(entries);
        setLoading(false);
    }

    private void setLoading(boolean loading) {
        leaderboardLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        leaderboardRecycler.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }
}
