package com.example.esports_arena.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PlayerMatchStats {
    private int id;
    private int matchId;
    private int playerId;
    private int kills;
    private int deaths;
    private int assists;

    public PlayerMatchStats() {
        // Firebase requires a public no-arg constructor
    }

    public PlayerMatchStats(int matchId, int playerId, int kills, int deaths, int assists) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public double getKdRatio() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }
}
