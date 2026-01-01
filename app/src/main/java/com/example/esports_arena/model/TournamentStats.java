package com.example.esports_arena.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class TournamentStats {
    private int kills;
    private int deaths;
    private int assists;
    private int matchesPlayed;
    private int matchesWon;

    public TournamentStats() {
        // Firebase requires a public no-arg constructor
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
}
