package com.example.esports_arena.data;

import androidx.annotation.Nullable;

import com.example.esports_arena.model.Player;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
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

    public Task<Void> update(Player player) {
        if (player == null) {
            return Tasks.forResult(null);
        }
        return playersRef.child(String.valueOf(player.getId())).setValue(player);
    }

    @Nullable
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
