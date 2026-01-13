package com.example.esports_arena;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.data.MatchRepository;
import com.example.esports_arena.data.TournamentRepository;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.TournamentStats;
import com.example.esports_arena.model.Match;
import com.example.esports_arena.model.Tournament;
import com.example.esports_arena.service.TournamentStatsService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerStatsFragment extends Fragment {

    private static final String ARG_PLAYER_ID = "arg_player_id";

    private BarChart kdaBarChart;
    private PieChart winPieChart;
    private TextView statsStatus;
    private TextView statsKills;
    private TextView statsDeaths;
    private TextView statsAssists;
    private TextView statsKd;
    private TextView statsWinRate;
    private TextView statsMatches;
    private TextView statsMatchesWon;
    private MaterialAutoCompleteTextView statsTournamentSelector;

    private PlayerRepository playerRepository;
    private MatchRepository matchRepository;
    private TournamentRepository tournamentRepository;
    private TournamentStatsService tournamentStatsService;
    private int playerId;
    private Player cachedPlayer;
    private List<Match> allMatches;
    private List<Tournament> allTournaments;

    public static PlayerStatsFragment newInstance(int playerId) {
        PlayerStatsFragment fragment = new PlayerStatsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        kdaBarChart = view.findViewById(R.id.kdaBarChart);
        winPieChart = view.findViewById(R.id.winPieChart);
        statsStatus = view.findViewById(R.id.statsStatus);
        statsKills = view.findViewById(R.id.statsKills);
        statsDeaths = view.findViewById(R.id.statsDeaths);
        statsAssists = view.findViewById(R.id.statsAssists);
        statsKd = view.findViewById(R.id.statsKd);
        statsWinRate = view.findViewById(R.id.statsWinRate);
        statsMatches = view.findViewById(R.id.statsMatches);
        statsMatchesWon = view.findViewById(R.id.statsMatchesWon);
        statsTournamentSelector = view.findViewById(R.id.statsTournamentSelector);

        playerRepository = new PlayerRepository();
        matchRepository = new MatchRepository();
        tournamentRepository = new TournamentRepository();
        tournamentStatsService = new TournamentStatsService();

        playerId = getArguments() != null ? getArguments().getInt(ARG_PLAYER_ID, -1) : -1;
        if (playerId == -1) {
            statsStatus.setText("Missing player id");
            return;
        }

        setupTournamentSelector();
        loadPlayer(playerId);
    }

    private void setupTournamentSelector() {
        // Initial adapter - will be updated later
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new String[]{"Overall"});
        statsTournamentSelector.setAdapter(adapter);
        statsTournamentSelector.setText("Overall", false);
        
        // Make it non-editable so it acts like a spinner/dropdown
        statsTournamentSelector.setKeyListener(null);
        statsTournamentSelector.setInputType(0);
        statsTournamentSelector.setFocusable(false);
        
        android.util.Log.d("PlayerStats", "Tournament selector configured as non-editable dropdown");
        // Listener is set in updateTournamentSelectorListener()
    }
    
    private void updateTournamentSelectorListener() {
        // Clear any existing listeners first
        statsTournamentSelector.setOnItemClickListener(null);
        
        // Set the item click listener - triggered when item is selected from dropdown
        statsTournamentSelector.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            android.util.Log.d("PlayerStats", "***** onItemClickListener - item clicked at position: " + position + ", selected: '" + selected + "' *****");
            
            // Explicitly set the text to the selected item
            statsTournamentSelector.setText(selected, false);
            
            // Render the new selection
            renderCurrentScope();
        });
        
        // Also add a click listener to ensure the dropdown opens
        statsTournamentSelector.setOnClickListener(v -> {
            android.util.Log.d("PlayerStats", "onClickListener triggered - showing dropdown");
            statsTournamentSelector.showDropDown();
        });
        
        android.util.Log.d("PlayerStats", "===== Tournament selector listeners re-attached =====");
    }

    private void loadPlayer(int playerId) {
        statsStatus.setText("");
        android.util.Log.d("PlayerStats", "========== loadPlayer(" + playerId + ") START ==========");
        
        playerRepository.getById(playerId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                statsStatus.setText("Failed to load player stats");
                android.util.Log.e("PlayerStats", "Player task failed", task.getException());
                return;
            }
            Player player = task.getResult();
            if (player == null) {
                statsStatus.setText("Player not found");
                android.util.Log.e("PlayerStats", "Player is null");
                return;
            }
            
            android.util.Log.d("PlayerStats", "✓ Player loaded: " + player.getUsername());
            cachedPlayer = player;
            
            // Load all matches for tournament stats calculation
            android.util.Log.d("PlayerStats", "➡️ Starting to load matches...");
            matchRepository.getAll().addOnCompleteListener(matchTask -> {
                if (matchTask.isSuccessful()) {
                    if (matchTask.getResult() != null) {
                        allMatches = matchTask.getResult();
                        android.util.Log.d("PlayerStats", "✅ Loaded " + allMatches.size() + " matches");
                    } else {
                        android.util.Log.e("PlayerStats", "❌ Match task result is null");
                        allMatches = new ArrayList<>();
                    }
                } else {
                    android.util.Log.e("PlayerStats", "❌ Match task failed: " + 
                            (matchTask.getException() != null ? matchTask.getException().getMessage() : "unknown error"));
                    allMatches = new ArrayList<>();
                }
                
                // Also load all tournaments for tournament ID lookup
                android.util.Log.d("PlayerStats", "➡️ Starting to load tournaments...");
                tournamentRepository.getAll().addOnCompleteListener(tournamentTask -> {
                    if (tournamentTask.isSuccessful() && tournamentTask.getResult() != null) {
                        allTournaments = tournamentTask.getResult();
                        android.util.Log.d("PlayerStats", "✅ Loaded " + allTournaments.size() + " tournaments");
                        for (Tournament t : allTournaments) {
                            android.util.Log.d("PlayerStats", "  Tournament: ID=" + t.getId() + ", Name='" + t.getName() + "'");
                        }
                    } else {
                        android.util.Log.e("PlayerStats", "❌ Failed to load tournaments", tournamentTask.getException());
                        allTournaments = new ArrayList<>();
                    }
                    
                    android.util.Log.d("PlayerStats", "========== loadPlayer() END - Populating selector ==========");
                    // Now populate the tournament selector with all data loaded
                    try {
                        populateTournamentSelector(player);
                    } catch (Exception e) {
                        android.util.Log.e("PlayerStats", "❌ Exception in populateTournamentSelector", e);
                        statsStatus.setText("Error loading tournament data");
                    }
                });
            });
        });
    }

    private void populateTournamentSelector(Player player) {
        List<String> scopes = new ArrayList<>();
        scopes.add("Overall");
        
        android.util.Log.d("PlayerStats", "\n╔═══════════════════════════════════════════════════════╗");
        android.util.Log.d("PlayerStats", "║  populateTournamentSelector() called                 ║");
        android.util.Log.d("PlayerStats", "╚═══════════════════════════════════════════════════════╝");
        
        // First try to fetch tournaments directly from database
        playerRepository.getTournamentNames().addOnCompleteListener(task -> {
            android.util.Log.d("PlayerStats", "[getTournamentNames] Task completed - Success: " + task.isSuccessful());
            if (!task.isSuccessful() && task.getException() != null) {
                android.util.Log.e("PlayerStats", "[getTournamentNames] Exception:", task.getException());
            }
            List<String> finalScopes = new ArrayList<>(scopes);
            
            if (task.isSuccessful() && task.getResult() != null) {
                List<String> tournaments = task.getResult();
                android.util.Log.d("PlayerStats", "Got " + tournaments.size() + " tournaments from database: " + tournaments);
                finalScopes.addAll(tournaments);
            } else {
                // Fallback: use player object
                android.util.Log.d("PlayerStats", "Falling back to player object for tournaments");
                Map<String, TournamentStats> tournamentStats = player.getTournamentStats();
                if (tournamentStats != null && !tournamentStats.isEmpty()) {
                    finalScopes.addAll(tournamentStats.keySet());
                    android.util.Log.d("PlayerStats", "Found tournaments from player: " + tournamentStats.keySet());
                } else {
                    android.util.Log.d("PlayerStats", "No tournament stats in player object");
                }
            }
            
            // Remove duplicates
            List<String> distinctScopes = new ArrayList<>();
            for (String scope : finalScopes) {
                if (!distinctScopes.contains(scope)) {
                    distinctScopes.add(scope);
                }
            }
            
            android.util.Log.d("PlayerStats", "Final scopes to display: " + distinctScopes);
            
            // IMPORTANT: Set listeners BEFORE setting adapter
            statsTournamentSelector.setOnItemClickListener((parent, view, position, id) -> {
                String selected = (String) parent.getItemAtPosition(position);
                android.util.Log.d("PlayerStats", "\n╔═══════════════════════════════════════════════════════╗");
                android.util.Log.d("PlayerStats", "║  TOURNAMENT SELECTED: " + selected);
                android.util.Log.d("PlayerStats", "║  Position: " + position);
                android.util.Log.d("PlayerStats", "╚═══════════════════════════════════════════════════════╝");
                statsTournamentSelector.setText(selected, false);
                android.util.Log.d("PlayerStats", "[OnItemClick] About to call renderCurrentScope()...");
                try {
                    renderCurrentScope();
                    android.util.Log.d("PlayerStats", "[OnItemClick] ✓ renderCurrentScope() completed successfully");
                } catch (Exception e) {
                    android.util.Log.e("PlayerStats", "[OnItemClick] ✗ EXCEPTION in renderCurrentScope():", e);
                    statsStatus.setText("Error: " + e.getMessage());
                }
            });
            
            statsTournamentSelector.setOnClickListener(v -> {
                android.util.Log.d("PlayerStats", "@@@@@ OnClickListener fired - showing dropdown @@@@@");
                statsTournamentSelector.showDropDown();
            });
            
            // NOW set the adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, distinctScopes);
            statsTournamentSelector.setAdapter(adapter);
            
            // Set the current selection
            String current = statsTournamentSelector.getText() != null ? statsTournamentSelector.getText().toString() : "";
            if (!distinctScopes.contains(current)) {
                statsTournamentSelector.setText(distinctScopes.get(0), false);
            }
            
            // Make sure the dropdown will show on click
            statsTournamentSelector.setThreshold(1);
            
            android.util.Log.d("PlayerStats", "Tournament selector fully configured with " + distinctScopes.size() + " options");
            
            // Now render after tournament selector is populated
            renderCurrentScope();
        });
    }

    private void renderText(ScopedStats stats) {
        statsKills.setText("Kills: " + stats.kills);
        statsDeaths.setText("Deaths: " + stats.deaths);
        statsAssists.setText("Assists: " + stats.assists);
        statsKd.setText("K/D: " + stats.kdRatio);
        statsWinRate.setText("Win Rate: " + String.format("%.2f%%", stats.winRate));
        statsMatches.setText("Matches Played: " + stats.matchesPlayed);
        statsMatchesWon.setText("Matches Won: " + stats.matchesWon);
    }

    private void renderCharts(ScopedStats stats) {
        // Bar: kills / deaths / assists
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, stats.kills));
        entries.add(new BarEntry(1, stats.deaths));
        entries.add(new BarEntry(2, stats.assists));
        BarDataSet dataSet = new BarDataSet(entries, "K/D/A");
        dataSet.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#F44336"), Color.parseColor("#FFC107")});
        dataSet.setValueTextSize(12f);
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        kdaBarChart.setData(barData);
        kdaBarChart.getDescription().setEnabled(false);
        XAxis xAxis = kdaBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Kills", "Deaths", "Assists"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        kdaBarChart.getAxisLeft().setAxisMinimum(0f);
        kdaBarChart.getAxisRight().setEnabled(false);
        kdaBarChart.getLegend().setEnabled(false);
        kdaBarChart.invalidate();

        // Pie: wins vs losses
        int wins = stats.matchesWon;
        int played = stats.matchesPlayed;
        int losses = Math.max(0, played - wins);
        if (wins == 0 && losses == 0) {
            statsStatus.setText("No matches played yet");
            winPieChart.clear();
        } else {
            List<PieEntry> pieEntries = new ArrayList<>();
            pieEntries.add(new PieEntry(wins, "Wins"));
            pieEntries.add(new PieEntry(losses, "Losses"));
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
            pieDataSet.setColors(new int[]{Color.parseColor("#4CAF50"), Color.parseColor("#F44336")});
            pieDataSet.setValueTextSize(12f);
            PieData pieData = new PieData(pieDataSet);
            winPieChart.setData(pieData);
            winPieChart.setUsePercentValues(true);
            Description desc = new Description();
            desc.setText("");
            winPieChart.setDescription(desc);
            Legend legend = winPieChart.getLegend();
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            winPieChart.invalidate();
        }
    }

    private void renderCurrentScope() {
        android.util.Log.d("PlayerStats", "\n╔═══════════════════════════════════════════════════════╗");
        android.util.Log.d("PlayerStats", "║  renderCurrentScope() START                          ║");
        android.util.Log.d("PlayerStats", "╚═══════════════════════════════════════════════════════╝");
        
        if (cachedPlayer == null) {
            android.util.Log.e("PlayerStats", "[renderCurrentScope] ✗ cachedPlayer is null!");
            return;
        }
        
        android.util.Log.d("PlayerStats", "[renderCurrentScope] Player: " + cachedPlayer.getUsername());
        android.util.Log.d("PlayerStats", "[renderCurrentScope] Current selection: " + (statsTournamentSelector.getText() != null ? statsTournamentSelector.getText().toString() : "null"));
        
        try {
            android.util.Log.d("PlayerStats", "[renderCurrentScope] → Calling scopedStatsForCurrentSelection...");
            ScopedStats scopedStats = scopedStatsForCurrentSelection(cachedPlayer);
            android.util.Log.d("PlayerStats", "[renderCurrentScope] ✓ Got scoped stats: K=" + scopedStats.kills + " D=" + scopedStats.deaths + " A=" + scopedStats.assists);
            
            android.util.Log.d("PlayerStats", "[renderCurrentScope] → Calling renderText...");
            renderText(scopedStats);
            android.util.Log.d("PlayerStats", "[renderCurrentScope] ✓ renderText completed");
            
            android.util.Log.d("PlayerStats", "[renderCurrentScope] → Calling renderCharts...");
            renderCharts(scopedStats);
            android.util.Log.d("PlayerStats", "[renderCurrentScope] ✓ renderCharts completed");
            
            android.util.Log.d("PlayerStats", "[renderCurrentScope] ✓✓✓ ALL RENDERING COMPLETE ✓✓✓");
        } catch (Exception e) {
            android.util.Log.e("PlayerStats", "[renderCurrentScope] ✗✗✗ EXCEPTION:", e);
            android.util.Log.e("PlayerStats", "[renderCurrentScope] Exception type: " + e.getClass().getName());
            android.util.Log.e("PlayerStats", "[renderCurrentScope] Exception message: " + e.getMessage());
            e.printStackTrace();
            statsStatus.setText("Error rendering stats: " + e.getMessage());
        }
    }

    private ScopedStats scopedStatsForCurrentSelection(Player player) {
        String selection = statsTournamentSelector.getText() != null ? statsTournamentSelector.getText().toString() : "Overall";
        android.util.Log.d("PlayerStats", "\n>>>>> scopedStatsForCurrentSelection() START");
        android.util.Log.d("PlayerStats", "Selection: '" + selection + "'");
        android.util.Log.d("PlayerStats", "AllMatches: " + (allMatches != null ? allMatches.size() : "null"));
        android.util.Log.d("PlayerStats", "AllTournaments: " + (allTournaments != null ? allTournaments.size() : "null"));
        
        try {
            if (!"Overall".equalsIgnoreCase(selection) && allMatches != null && !allMatches.isEmpty()) {
                // Find tournament ID from tournament name
                int tournamentId = findTournamentIdByName(selection);
                if (tournamentId != -1) {
                    android.util.Log.d("PlayerStats", "Tournament ID found: " + tournamentId);
                    TournamentStats tournamentStats = tournamentStatsService.getPlayerTournamentStats(player.getId(), tournamentId, allMatches);
                    if (tournamentStats != null) {
                        android.util.Log.d("PlayerStats", "Returned stats: K=" + tournamentStats.getKills() + " D=" + tournamentStats.getDeaths() + " A=" + tournamentStats.getAssists());
                        return ScopedStats.fromTournament(tournamentStats);
                    } else {
                        android.util.Log.w("PlayerStats", "Tournament stats returned null");
                    }
                } else {
                    android.util.Log.w("PlayerStats", "Tournament ID NOT found for: '" + selection + "'");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("PlayerStats", "❌ Exception getting tournament stats", e);
        }
        
        android.util.Log.d("PlayerStats", "Using overall stats");
        android.util.Log.d("PlayerStats", ">>>>> scopedStatsForCurrentSelection() END\n");
        return ScopedStats.fromPlayer(player);
    }
    
    private int findTournamentIdByName(String tournamentName) {
        android.util.Log.d("PlayerStats", "\n╔═══════════════════════════════════════════════════════╗");
        android.util.Log.d("PlayerStats", "║  findTournamentIdByName(" + tournamentName + ")");
        android.util.Log.d("PlayerStats", "╚═══════════════════════════════════════════════════════╝");
        
        if (allTournaments == null) {
            android.util.Log.e("PlayerStats", "[findTournament] ✗✗✗ allTournaments is NULL!");
            return -1;
        }
        
        if (allTournaments.isEmpty()) {
            android.util.Log.e("PlayerStats", "[findTournament] ✗✗✗ allTournaments is EMPTY!");
            return -1;
        }
        
        android.util.Log.d("PlayerStats", "[findTournament] Searching through " + allTournaments.size() + " tournaments:");
        
        // Search through all tournaments to find matching name
        for (int i = 0; i < allTournaments.size(); i++) {
            Tournament tournament = allTournaments.get(i);
            if (tournament == null) {
                android.util.Log.w("PlayerStats", "[findTournament]   [" + i + "] NULL tournament object");
                continue;
            }
            
            String tName = tournament.getName();
            int tId = tournament.getId();
            android.util.Log.d("PlayerStats", "[findTournament]   [" + i + "] ID=" + tId + ", Name='" + tName + "'");
            
            if (tName != null && tName.equals(tournamentName)) {
                android.util.Log.d("PlayerStats", "[findTournament] ✓✓✓ MATCH FOUND! Returning ID: " + tId);
                return tId;
            }
        }
        
        android.util.Log.w("PlayerStats", "[findTournament] ✗ No tournament found matching: '" + tournamentName + "'");
        return -1;
    }

    private static class ScopedStats {
        final int kills;
        final int deaths;
        final int assists;
        final int matchesPlayed;
        final int matchesWon;
        final double kdRatio;
        final double winRate;

        private ScopedStats(int kills, int deaths, int assists, int matchesPlayed, int matchesWon) {
            this.kills = kills;
            this.deaths = deaths;
            this.assists = assists;
            this.matchesPlayed = matchesPlayed;
            this.matchesWon = matchesWon;
            this.kdRatio = deaths == 0 ? kills : (double) kills / deaths;
            this.winRate = matchesPlayed == 0 ? 0.0 : (double) matchesWon / matchesPlayed * 100.0;
        }

        static ScopedStats fromPlayer(Player player) {
            return new ScopedStats(
                    player.getTotalKills(),
                    player.getTotalDeaths(),
                    player.getTotalAssists(),
                    player.getMatchesPlayed(),
                    player.getMatchesWon()
            );
        }

        static ScopedStats fromTournament(TournamentStats stats) {
            return new ScopedStats(
                    stats.getKills(),
                    stats.getDeaths(),
                    stats.getAssists(),
                    stats.getMatchesPlayed(),
                    stats.getMatchesWon()
            );
        }
    }
}
