package com.example.esports_arena.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.R;
import com.example.esports_arena.model.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamRosterAdapter extends RecyclerView.Adapter<TeamRosterAdapter.ViewHolder> {

    private final List<Player> players = new ArrayList<>();

    public void setPlayers(List<Player> newPlayers) {
        players.clear();
        if (newPlayers != null) {
            players.addAll(newPlayers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_player, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Player player = players.get(position);
        holder.name.setText(player.getUsername() != null ? player.getUsername() : "");
        holder.role.setText(player.getRole() != null ? player.getRole() : "");
        holder.availability.setText(player.isAvailable() ? "Available" : "Unavailable");
        holder.availability.setTextColor(player.isAvailable() ? 0xFF2ecc71 : 0xFFe94560);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView role;
        final TextView availability;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.playerRowName);
            role = itemView.findViewById(R.id.playerRowRole);
            availability = itemView.findViewById(R.id.playerRowAvailability);
        }
    }
}
