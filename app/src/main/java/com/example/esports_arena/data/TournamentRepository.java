package com.example.esports_arena.data;

import com.example.esports_arena.model.Tournament;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TournamentRepository {
    private static final String TOURNAMENTS_NODE = "tournaments";

    private final DatabaseReference tournamentsRef;
    private final Executor callbackExecutor;

    public TournamentRepository() {
        this.tournamentsRef = FirebaseService.root().child(TOURNAMENTS_NODE);
        this.callbackExecutor = Executors.newSingleThreadExecutor();
    }

    public Task<List<Tournament>> getAll() {
        return tournamentsRef.get().continueWith(callbackExecutor, task -> {
            List<Tournament> tournaments = new ArrayList<>();
            if (!task.isSuccessful() || task.getResult() == null) {
                android.util.Log.d("TournamentRepo", "No tournaments found");
                return tournaments;
            }

            for (DataSnapshot tournamentNode : task.getResult().getChildren()) {
                try {
                    Tournament tournament = new Tournament();
                    tournament.setId(Integer.parseInt(tournamentNode.getKey()));
                    String name = tournamentNode.child("name").getValue(String.class);
                    tournament.setName(name);
                    tournaments.add(tournament);
                    android.util.Log.d("TournamentRepo", "Found tournament - ID: " + tournament.getId() + ", Name: " + name);
                } catch (Exception e) {
                    android.util.Log.e("TournamentRepo", "Error parsing tournament: " + e.getMessage());
                }
            }
            android.util.Log.d("TournamentRepo", "Loaded " + tournaments.size() + " tournaments");
            return tournaments;
        });
    }
}
