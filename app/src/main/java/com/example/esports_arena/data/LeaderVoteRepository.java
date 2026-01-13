package com.example.esports_arena.data;

import com.example.esports_arena.model.LeaderVote;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LeaderVoteRepository {
    private static final String VOTES_NODE = "leader_votes";

    private final DatabaseReference votesRef;
    private final Executor callbackExecutor;

    public LeaderVoteRepository() {
        this.votesRef = FirebaseService.root().child(VOTES_NODE);
        this.callbackExecutor = Executors.newSingleThreadExecutor();
    }

    public Task<Void> submitVote(LeaderVote vote) {
        return votesRef.child(String.valueOf(vote.getTeamId())).push().setValue(vote);
    }

    public Task<Map<Integer, Integer>> getResults(int teamId) {
        return votesRef.child(String.valueOf(teamId)).get().continueWith(callbackExecutor, task -> {
            Map<Integer, Integer> counts = new HashMap<>();
            if (!task.isSuccessful() || task.getResult() == null) {
                return counts;
            }
            for (DataSnapshot child : task.getResult().getChildren()) {
                LeaderVote vote = child.getValue(LeaderVote.class);
                if (vote != null) {
                    int candidate = vote.getCandidateId();
                    counts.put(candidate, counts.getOrDefault(candidate, 0) + 1);
                }
            }
            return counts;
        });
    }

    public Task<LeaderVote> getVoteByVoter(int teamId, int voterId) {
        return votesRef.child(String.valueOf(teamId)).get().continueWith(callbackExecutor, task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return null;
            }
            for (DataSnapshot child : task.getResult().getChildren()) {
                LeaderVote vote = child.getValue(LeaderVote.class);
                if (vote != null && vote.getVoterId() == voterId) {
                    return vote;
                }
            }
            return null;
        });
    }
}
