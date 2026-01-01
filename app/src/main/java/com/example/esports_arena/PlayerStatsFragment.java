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
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.TournamentStats;
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
    private int playerId;
    private Player cachedPlayer;

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

        playerId = getArguments() != null ? getArguments().getInt(ARG_PLAYER_ID, -1) : -1;
        if (playerId == -1) {
            statsStatus.setText("Missing player id");
            return;
        }

        setupTournamentSelector();
        loadPlayer(playerId);
    }

    private void setupTournamentSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new String[]{"Overall"});
        statsTournamentSelector.setAdapter(adapter);
        statsTournamentSelector.setText("Overall", false);
        statsTournamentSelector.setOnItemClickListener((parent, view, position, id) -> renderCurrentScope());
    }

    private void loadPlayer(int playerId) {
        statsStatus.setText("");
        playerRepository.getById(playerId).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                statsStatus.setText("Failed to load player stats");
                return;
            }
            Player player = task.getResult();
            if (player == null) {
                statsStatus.setText("Player not found");
                return;
            }
            
            android.util.Log.d("PlayerStats", "Player loaded: " + player.getUsername());
            if (player.getTournamentStats() != null) {
                android.util.Log.d("PlayerStats", "Tournament stats: " + player.getTournamentStats().size() + " tournaments");
                android.util.Log.d("PlayerStats", "Tournament names: " + player.getTournamentStats().keySet());
            } else {
                android.util.Log.d("PlayerStats", "Tournament stats is NULL");
            }
            
            cachedPlayer = player;
            populateTournamentSelector(player); // This is async, renderCurrentScope is called inside it
        });
    }

    private void populateTournamentSelector(Player player) {
        List<String> scopes = new ArrayList<>();
        scopes.add("Overall");
        
        android.util.Log.d("PlayerStats", "populateTournamentSelector() called");
        
        // First try to fetch tournaments directly from database
        playerRepository.getTournamentNames().addOnCompleteListener(task -> {
            android.util.Log.d("PlayerStats", "getTournamentNames task completed - Success: " + task.isSuccessful());
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, distinctScopes);
            statsTournamentSelector.setAdapter(adapter);
            String current = statsTournamentSelector.getText() != null ? statsTournamentSelector.getText().toString() : "";
            if (!distinctScopes.contains(current)) {
                statsTournamentSelector.setText(distinctScopes.get(0), false);
            }
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
        if (cachedPlayer == null) {
            return;
        }
        ScopedStats scopedStats = scopedStatsForCurrentSelection(cachedPlayer);
        renderText(scopedStats);
        renderCharts(scopedStats);
    }

    private ScopedStats scopedStatsForCurrentSelection(Player player) {
        String selection = statsTournamentSelector.getText() != null ? statsTournamentSelector.getText().toString() : "Overall";
        android.util.Log.d("PlayerStats", "Current selection: " + selection);
        if (!"Overall".equalsIgnoreCase(selection)) {
            Map<String, TournamentStats> byTournament = player.getTournamentStats();
            if (byTournament != null) {
                android.util.Log.d("PlayerStats", "Tournament map keys: " + byTournament.keySet());
                TournamentStats scoped = byTournament.get(selection);
                if (scoped != null) {
                    android.util.Log.d("PlayerStats", "Found tournament stats: " + selection);
                    return ScopedStats.fromTournament(scoped);
                } else {
                    android.util.Log.d("PlayerStats", "Tournament not found in map: " + selection);
                }
            } else {
                android.util.Log.d("PlayerStats", "Tournament map is null");
            }
        }
        return ScopedStats.fromPlayer(player);
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
