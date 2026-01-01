package com.example.esports_arena.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.R;
import com.example.esports_arena.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderVoteAdapter extends RecyclerView.Adapter<LeaderVoteAdapter.ViewHolder> {

    public interface OnVoteClick {
        void onVote(Player candidate);
    }

    private final List<Player> candidates = new ArrayList<>();
    private final Map<Integer, Integer> currentCounts;
    private final OnVoteClick onVoteClick;
    private boolean votingEnabled = true;

    public LeaderVoteAdapter(Map<Integer, Integer> currentCounts, OnVoteClick onVoteClick) {
        this.currentCounts = currentCounts;
        this.onVoteClick = onVoteClick;
    }

    public void setCandidates(List<Player> list) {
        candidates.clear();
        if (list != null) {
            candidates.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void updateCounts(Map<Integer, Integer> counts) {
        if (counts != null && currentCounts != counts) {
            currentCounts.clear();
            currentCounts.putAll(counts);
        }
        notifyDataSetChanged();
    }

    public void setVotingEnabled(boolean enabled) {
        this.votingEnabled = enabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vote_player, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = candidates.get(position);
        holder.name.setText(player.getUsername() != null ? player.getUsername() : "");
        holder.role.setText(player.getRole() != null ? player.getRole() : "");
        Integer votes = currentCounts.get(player.getId());
        int voteCount = votes != null ? votes : 0;
        holder.availability.setText(voteCount + (voteCount == 1 ? " vote" : " votes"));
        holder.voteButton.setOnClickListener(v -> onVoteClick.onVote(player));
        holder.voteButton.setVisibility(View.VISIBLE);
        holder.voteButton.setEnabled(votingEnabled);
    }

    @Override
    public int getItemCount() {
        return candidates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView role;
        final TextView availability;
        final Button voteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.playerRowName);
            role = itemView.findViewById(R.id.playerRowRole);
            availability = itemView.findViewById(R.id.playerRowAvailability);
            voteButton = itemView.findViewById(R.id.playerRowVote);
        }
    }
}
