package com.example.esports_arena.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.R;
import com.example.esports_arena.model.Match;
import com.example.esports_arena.model.Team;
import com.example.esports_arena.model.Tournament;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpcomingMatchesAdapter extends RecyclerView.Adapter<UpcomingMatchesAdapter.ViewHolder> {

    private final List<MatchDisplayData> matches = new ArrayList<>();
    private final Map<Integer, Team> teamMap;
    private final Map<Integer, Tournament> tournamentMap;

    public UpcomingMatchesAdapter(Map<Integer, Team> teamMap, Map<Integer, Tournament> tournamentMap) {
        this.teamMap = teamMap;
        this.tournamentMap = tournamentMap;
    }

    public void setMatches(List<Match> matchesList) {
        this.matches.clear();
        for (Match m : matchesList) {
            this.matches.add(new MatchDisplayData(m));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MatchDisplayData matchData = matches.get(position);
        Match match = matchData.match;

        // Get team names
        String team1Name = teamMap.containsKey(match.getTeam1Id()) 
            ? teamMap.get(match.getTeam1Id()).getName() 
            : "Team " + match.getTeam1Id();
        String team2Name = teamMap.containsKey(match.getTeam2Id()) 
            ? teamMap.get(match.getTeam2Id()).getName() 
            : "Team " + match.getTeam2Id();

        holder.teamA.setText(team1Name);
        holder.teamB.setText(team2Name);
        
        // Get tournament name
        String tournamentName = "TBD";
        if (match.getTournamentId() > 0 && tournamentMap.containsKey(match.getTournamentId())) {
            tournamentName = tournamentMap.get(match.getTournamentId()).getName();
        } else if (match.getTournamentId() > 0) {
            tournamentName = "Tournament " + match.getTournamentId();
        }
        holder.tournament.setText("Tournament: " + tournamentName);
        holder.round.setText("Round: " + (match.getRound() != null ? match.getRound() : "TBD"));
        
        // Format scheduled time
        String scheduledTimeStr = "Scheduled: TBD";
        try {
            long scheduledTimeMs = match.getScheduledTimeAsLong();
            if (scheduledTimeMs > 0) {
                Date date = new Date(scheduledTimeMs);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                scheduledTimeStr = "Scheduled: " + sdf.format(date);
            }
        } catch (Exception e) {
            android.util.Log.e("UpcomingMatches", "Error formatting date", e);
        }
        holder.scheduledTime.setText(scheduledTimeStr);
        
        // Status
        String status = match.getStatus() != null ? match.getStatus() : "SCHEDULED";
        holder.status.setText("Status: " + status);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView teamA;
        TextView teamB;
        TextView tournament;
        TextView round;
        TextView scheduledTime;
        TextView status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            teamA = itemView.findViewById(R.id.matchTeamA);
            teamB = itemView.findViewById(R.id.matchTeamB);
            tournament = itemView.findViewById(R.id.matchTournament);
            round = itemView.findViewById(R.id.matchRound);
            scheduledTime = itemView.findViewById(R.id.matchScheduledTime);
            status = itemView.findViewById(R.id.matchStatus);
        }
    }

    private static class MatchDisplayData {
        Match match;

        MatchDisplayData(Match match) {
            this.match = match;
        }
    }
}
