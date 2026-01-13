package com.example.esports_arena.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Match {
    public enum MatchStatus {
        SCHEDULED, LIVE, COMPLETED, POSTPONED, CANCELLED
    }

    private int id;
    private int tournamentId;
    private int team1Id;
    private int team2Id;
    private int team1Score;
    private int team2Score;
    private String scheduledTime;      // Changed from long to String (ISO format in Firebase)
    private String actualStartTime;    // Changed from long to String (ISO format in Firebase)
    private String actualEndTime;      // Changed from long to String (ISO format in Firebase)
    private MatchStatus status;
    private String round;
    private Integer winnerId;
    private List<PlayerMatchStats> playerStats;
    private boolean completed; // Firebase also stores this boolean flag

    public Match() {
        this.playerStats = new ArrayList<>();
        this.status = MatchStatus.SCHEDULED;
    }

    public Match(int tournamentId, int team1Id, int team2Id, long scheduledTime, String round) {
        this();
        this.tournamentId = tournamentId;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        setScheduledTimeFromLong(scheduledTime);
        this.round = round;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(int tournamentId) {
        this.tournamentId = tournamentId;
    }

    public int getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(int team1Id) {
        this.team1Id = team1Id;
    }

    public int getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(int team2Id) {
        this.team2Id = team2Id;
    }

    public int getTeam1Score() {
        return team1Score;
    }

    public void setTeam1Score(int team1Score) {
        this.team1Score = team1Score;
    }

    public int getTeam2Score() {
        return team2Score;
    }

    public void setTeam2Score(int team2Score) {
        this.team2Score = team2Score;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    // Convenience method to get as long
    public long getScheduledTimeAsLong() {
        if (scheduledTime == null || scheduledTime.isEmpty()) return 0;
        try {
            return Long.parseLong(scheduledTime);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // Convenience method to set from long
    public void setScheduledTimeFromLong(long time) {
        this.scheduledTime = String.valueOf(time);
    }

    public String getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(String actualStartTime) {
        this.actualStartTime = actualStartTime;
    }
    
    // Convenience method to get as long
    public long getActualStartTimeAsLong() {
        if (actualStartTime == null || actualStartTime.isEmpty()) return 0;
        try {
            return Long.parseLong(actualStartTime);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // Convenience method to set from long
    public void setActualStartTimeFromLong(long time) {
        this.actualStartTime = String.valueOf(time);
    }

    public String getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(String actualEndTime) {
        this.actualEndTime = actualEndTime;
    }
    
    // Convenience method to get as long
    public long getActualEndTimeAsLong() {
        if (actualEndTime == null || actualEndTime.isEmpty()) return 0;
        try {
            return Long.parseLong(actualEndTime);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // Convenience method to set from long
    public void setActualEndTimeFromLong(long time) {
        this.actualEndTime = String.valueOf(time);
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public List<PlayerMatchStats> getPlayerStats() {
        return playerStats;
    }

    public void setPlayerStats(List<PlayerMatchStats> playerStats) {
        this.playerStats = playerStats;
    }

    public boolean isCompleted() {
        // Check both the enum status and the boolean flag
        return (status == MatchStatus.COMPLETED) || completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && status != MatchStatus.COMPLETED) {
            this.status = MatchStatus.COMPLETED;
        }
    }

    public void addPlayerStats(PlayerMatchStats stats) {
        playerStats.add(stats);
    }
}
