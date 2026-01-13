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
    private long scheduledTime;
    private long actualStartTime;
    private long actualEndTime;
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
        this.scheduledTime = scheduledTime;
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

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public long getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(long actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public long getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(long actualEndTime) {
        this.actualEndTime = actualEndTime;
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
