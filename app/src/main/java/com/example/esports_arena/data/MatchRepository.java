package com.example.esports_arena.data;

import com.example.esports_arena.model.Match;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MatchRepository {
    private static final String MATCHES_NODE = "matches";

    private final DatabaseReference matchesRef;
    private final Executor callbackExecutor;

    public MatchRepository() {
        this.matchesRef = FirebaseService.root().child(MATCHES_NODE);
        this.callbackExecutor = Executors.newSingleThreadExecutor();
    }

    public Task<List<Match>> getAll() {
        return matchesRef.get().continueWith(callbackExecutor, task -> {
            List<Match> matches = new ArrayList<>();
            
            if (!task.isSuccessful()) {
                android.util.Log.e("MatchRepo", "Task failed: " + (task.getException() != null ? task.getException().getMessage() : "unknown"));
                return matches;
            }
            
            if (task.getResult() == null) {
                android.util.Log.d("MatchRepo", "No matches node found in database");
                return matches;
            }

            DataSnapshot snapshot = task.getResult();
            android.util.Log.d("MatchRepo", "Matches snapshot children count: " + snapshot.getChildrenCount());
            
            for (DataSnapshot matchNode : snapshot.getChildren()) {
                try {
                    Match match = matchNode.getValue(Match.class);
                    if (match != null) {
                        android.util.Log.d("MatchRepo", "✓ Loaded Match ID: " + match.getId() + 
                                " | Tournament: " + match.getTournamentId() + 
                                " | Status: " + match.getStatus() + 
                                " | PlayerStats: " + (match.getPlayerStats() != null ? match.getPlayerStats().size() : 0));
                        matches.add(match);
                    } else {
                        android.util.Log.w("MatchRepo", "✗ Match node parsed to null: " + matchNode.getKey());
                    }
                } catch (Exception e) {
                    android.util.Log.e("MatchRepo", "Error parsing match " + matchNode.getKey() + ": " + e.getMessage());
                }
            }
            android.util.Log.d("MatchRepo", "===== Total loaded: " + matches.size() + " matches =====");
            return matches;
        });
    }

    public Task<List<Match>> getByTournamentId(int tournamentId) {
        return getAll().continueWith(callbackExecutor, task -> {
            List<Match> tournamentMatches = new ArrayList<>();
            if (!task.isSuccessful() || task.getResult() == null) {
                return tournamentMatches;
            }

            for (Match match : task.getResult()) {
                if (match.getTournamentId() == tournamentId) {
                    tournamentMatches.add(match);
                }
            }
            android.util.Log.d("MatchRepo", "Found " + tournamentMatches.size() + " matches for tournament " + tournamentId);
            return tournamentMatches;
        });
    }
}
