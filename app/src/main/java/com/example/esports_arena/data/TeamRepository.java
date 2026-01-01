package com.example.esports_arena.data;

import com.example.esports_arena.model.Team;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TeamRepository {
    private static final String TEAMS_NODE = "teams";

    private final DatabaseReference teamsRef;
    private final Executor callbackExecutor;

    public TeamRepository() {
        this.teamsRef = FirebaseService.root().child(TEAMS_NODE);
        this.callbackExecutor = Executors.newSingleThreadExecutor();
    }

    public Task<Team> getById(int id) {
        return teamsRef.child(String.valueOf(id)).get().continueWith(callbackExecutor, task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return null;
            }
            DataSnapshot snapshot = task.getResult();
            return snapshot.getValue(Team.class);
        });
    }
}
