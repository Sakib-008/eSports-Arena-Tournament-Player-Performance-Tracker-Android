package com.example.esports_arena.model;

import androidx.annotation.Nullable;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class Player {
    private int id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private Integer teamId;
    private String role;
    private String joinDate;
    private int totalKills;
    private int totalDeaths;
    private int totalAssists;
    private int matchesPlayed;
    private int matchesWon;
    private boolean available;
    private String availabilityReason;
    private Map<String, TournamentStats> tournamentStats;

    public Player() {
        // Firebase requires a public no-arg constructor
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Nullable
    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(@Nullable Integer teamId) {
        this.teamId = teamId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void setTotalKills(int totalKills) {
        this.totalKills = totalKills;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public int getTotalAssists() {
        return totalAssists;
    }

    public void setTotalAssists(int totalAssists) {
        this.totalAssists = totalAssists;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }

    public void setMatchesPlayed(int matchesPlayed) {
        this.matchesPlayed = matchesPlayed;
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int matchesWon) {
        this.matchesWon = matchesWon;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAvailabilityReason() {
        return availabilityReason;
    }

    public void setAvailabilityReason(String availabilityReason) {
        this.availabilityReason = availabilityReason;
    }

    @Nullable
    public Map<String, TournamentStats> getTournamentStats() {
        return tournamentStats;
    }

    public void setTournamentStats(@Nullable Map<String, TournamentStats> tournamentStats) {
        this.tournamentStats = tournamentStats;
    }

    public double getKdRatio() {
        return totalDeaths == 0 ? totalKills : (double) totalKills / totalDeaths;
    }

    public double getWinRate() {
        return matchesPlayed == 0 ? 0.0 : (double) matchesWon / matchesPlayed * 100.0;
    }

    public String getAvailabilityStatus() {
        if (available) {
            return "Available";
        }
        return availabilityReason == null || availabilityReason.isBlank()
                ? "Unavailable"
                : "Unavailable: " + availabilityReason;
    }

    @Override
    public String toString() {
        return username != null ? username : "Player" + id;
    }
}
