package com.example.esports_arena.model;

import com.google.firebase.database.IgnoreExtraProperties;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private String status;              // Changed to String for Firebase compatibility
    private String round;
    private Integer winnerId;
    private List<PlayerMatchStats> playerStats;
    private boolean completed; // Firebase also stores this boolean flag

    public Match() {
        this.playerStats = new ArrayList<>();
        this.status = "SCHEDULED";
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
        
        // Try parsing as a number (milliseconds since epoch)
        try {
            return Long.parseLong(scheduledTime);
        } catch (NumberFormatException e) {
            // Not a number, try ISO date format
        }
        
        // Try parsing as ISO date format (e.g., "2026-01-15T14:00:00")
        try {
            // Remove milliseconds and timezone if present
            String dateStr = scheduledTime;
            if (dateStr.contains(".")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("."));
            }
            if (dateStr.contains("Z")) {
                dateStr = dateStr.replace("Z", "");
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (Exception e) {
            android.util.Log.w("Match", "Failed to parse scheduledTime: " + scheduledTime + " Error: " + e.getMessage());
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
        
        // Try parsing as a number (milliseconds since epoch)
        try {
            return Long.parseLong(actualStartTime);
        } catch (NumberFormatException e) {
            // Not a number, try ISO date format
        }
        
        // Try parsing as ISO date format
        try {
            String dateStr = actualStartTime;
            if (dateStr.contains(".")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("."));
            }
            if (dateStr.contains("Z")) {
                dateStr = dateStr.replace("Z", "");
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (Exception e) {
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
        
        // Try parsing as a number (milliseconds since epoch)
        try {
            return Long.parseLong(actualEndTime);
        } catch (NumberFormatException e) {
            // Not a number, try ISO date format
        }
        
        // Try parsing as ISO date format
        try {
            String dateStr = actualEndTime;
            if (dateStr.contains(".")) {
                dateStr = dateStr.substring(0, dateStr.indexOf("."));
            }
            if (dateStr.contains("Z")) {
                dateStr = dateStr.replace("Z", "");
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Convenience method to set from long
    public void setActualEndTimeFromLong(long time) {
        this.actualEndTime = String.valueOf(time);
    }

    public String getStatus() {
        return status != null ? status : "SCHEDULED";
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public MatchStatus getStatusEnum() {
        if (status == null || status.isEmpty()) {
            return MatchStatus.SCHEDULED;
        }
        try {
            return MatchStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return MatchStatus.SCHEDULED;
        }
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
        // Check both the string status and the boolean flag
        return (status != null && status.equals("COMPLETED")) || completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && (status == null || !status.equals("COMPLETED"))) {
            this.status = "COMPLETED";
        }
    }

    public void addPlayerStats(PlayerMatchStats stats) {
        playerStats.add(stats);
    }
}
