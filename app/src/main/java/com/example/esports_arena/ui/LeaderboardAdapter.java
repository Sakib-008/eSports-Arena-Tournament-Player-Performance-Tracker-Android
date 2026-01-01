package com.example.esports_arena.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    public static class LeaderboardEntry {
        public final String title;
        public final String subtitle1;
        public final String subtitle2;
        public final double score;

        public LeaderboardEntry(String title, String subtitle1, String subtitle2, double score) {
            this.title = title;
            this.subtitle1 = subtitle1;
            this.subtitle2 = subtitle2;
            this.score = score;
        }
    }

    private final List<LeaderboardEntry> entries = new ArrayList<>();

    public void setEntries(List<LeaderboardEntry> newEntries) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.rank.setText((position + 1) + ".");
        holder.title.setText(entry.title);
        holder.subtitle1.setText(entry.subtitle1);
        holder.subtitle2.setText(entry.subtitle2);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView rank;
        final TextView title;
        final TextView subtitle1;
        final TextView subtitle2;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.leaderboardRank);
            title = itemView.findViewById(R.id.leaderboardTitle);
            subtitle1 = itemView.findViewById(R.id.leaderboardSubtitle1);
            subtitle2 = itemView.findViewById(R.id.leaderboardSubtitle2);
        }
    }
}
