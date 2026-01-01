package com.example.esports_arena.data;

import androidx.annotation.Nullable;

import com.example.esports_arena.model.Player;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayerRepository {
    private final DatabaseReference playersRef;
    private final Executor callbackExecutor;

    public PlayerRepository() {
        this.playersRef = FirebaseService.playersRef();
        this.callbackExecutor = Executors.newSingleThreadExecutor();
    }

    public Task<Player> getById(int id) {
        return playersRef.child(String.valueOf(id)).get().continueWith(callbackExecutor, task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return null;
            }
            DataSnapshot snapshot = task.getResult();
            return snapshot.getValue(Player.class);
        });
    }

    public Task<Player> getByUsername(String username) {
        return playersRef.orderByChild("username").equalTo(username).limitToFirst(1).get()
                .continueWith(callbackExecutor, task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return null;
                    }
                    for (DataSnapshot child : task.getResult().getChildren()) {
                        return child.getValue(Player.class);
                    }
                    return null;
                });
    }

    public Task<List<Player>> getAll() {
        return playersRef.get().continueWith(callbackExecutor, task -> {
            List<Player> players = new ArrayList<>();
            if (!task.isSuccessful() || task.getResult() == null) {
                return players;
            }
            for (DataSnapshot child : task.getResult().getChildren()) {
                Player player = child.getValue(Player.class);
                if (player != null) {
                    players.add(player);
                }
            }
            return players;
        });
    }

    public Task<List<Player>> getByTeamId(int teamId) {
        // Primary: numeric match on teamId
        return playersRef.orderByChild("teamId").equalTo(teamId).get()
                .continueWithTask(callbackExecutor, task -> {
                    List<Player> players = collectPlayers(task);
                    if (!players.isEmpty()) {
                        return Tasks.forResult(players);
                    }
                    // Fallback: if DB stored teamId as string, do a manual filter
                    return playersRef.get().continueWith(callbackExecutor, allTask -> {
                        List<Player> fallback = new ArrayList<>();
                        if (!allTask.isSuccessful() || allTask.getResult() == null) {
                            return fallback;
                        }
                        for (DataSnapshot child : allTask.getResult().getChildren()) {
                            Player p = child.getValue(Player.class);
                            if (p != null && p.getTeamId() != null && p.getTeamId() == teamId) {
                                fallback.add(p);
                                continue;
                            }
                            Object rawTeamId = child.child("teamId").getValue();
                            if (p != null && rawTeamId instanceof String) {
                                try {
                                    if (Integer.parseInt((String) rawTeamId) == teamId) {
                                        fallback.add(p);
                                    }
                                } catch (NumberFormatException ignore) {
                                    // ignore invalid string
                                }
                            }
                        }
                        return fallback;
                    });
                });
    }

    private List<Player> collectPlayers(Task<DataSnapshot> task) {
        List<Player> players = new ArrayList<>();
        if (!task.isSuccessful() || task.getResult() == null) {
            return players;
        }
        for (DataSnapshot child : task.getResult().getChildren()) {
            Player player = child.getValue(Player.class);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public Task<Void> create(Player player) {
        if (player == null) {
            return Tasks.forResult(null);
        }
        return playersRef.child(String.valueOf(player.getId())).setValue(player);
    }

    public Task<Void> update(Player player) {
        if (player == null) {
            return Tasks.forResult(null);
        }
        return playersRef.child(String.valueOf(player.getId())).setValue(player);
    }

    public Task<List<String>> getTournamentNames() {
        return com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("tournaments")
                .get()
                .continueWith(callbackExecutor, task -> {
                    List<String> tournaments = new ArrayList<>();
                    android.util.Log.d("PlayerRepo", "getTournamentNames() called - checking tournaments node");
                    
                    if (!task.isSuccessful()) {
                        android.util.Log.e("PlayerRepo", "Task failed: " + (task.getException() != null ? task.getException().getMessage() : "unknown"));
                        return tournaments;
                    }
                    
                    if (task.getResult() == null) {
                        android.util.Log.d("PlayerRepo", "Tournaments node doesn't exist");
                        return tournaments;
                    }
                    
                    for (DataSnapshot tournamentNode : task.getResult().getChildren()) {
                        String name = tournamentNode.child("name").getValue(String.class);
                        String id = tournamentNode.getKey();
                        android.util.Log.d("PlayerRepo", "Tournament found - ID: " + id + ", Name: " + name);
                        
                        if (name != null && !tournaments.contains(name)) {
                            tournaments.add(name);
                        }
                    }
                    android.util.Log.d("PlayerRepo", "Total tournaments collected: " + tournaments.size() + " -> " + tournaments);
                    return tournaments;
                });
    }

    public Task<Map<String, Object>> getTournamentStatsForPlayer(int playerId, String tournamentName) {
        return playersRef.child(String.valueOf(playerId)).child("tournamentStats").child(tournamentName).get()
                .continueWith(callbackExecutor, task -> {
                    Map<String, Object> result = new java.util.HashMap<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return result;
                    }
                    DataSnapshot snapshot = task.getResult();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        result.put(child.getKey(), child.getValue());
                    }
                    return result;
                });
    }
}
