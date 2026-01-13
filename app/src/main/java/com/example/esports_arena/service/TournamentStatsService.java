package com.example.esports_arena.service;

import com.example.esports_arena.data.MatchRepository;
import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.model.Match;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.PlayerMatchStats;
import com.example.esports_arena.model.TournamentStats;

import java.util.List;

/**
 * Service to calculate tournament-based statistics for players.
 * This mirrors the desktop app's TournamentStatsService.
 *
 * Calculates player stats by:
 * 1. Getting all matches for a tournament
 * 2. Finding player entries in those matches' playerStats
 * 3. Aggregating kills/deaths/assists/wins
 */
public class TournamentStatsService {
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public TournamentStatsService() {
        this.matchRepository = new MatchRepository();
        this.playerRepository = new PlayerRepository();
    }

    /**
     * Calculate tournament-specific stats for a player
     * Returns aggregated kills/deaths/assists/matches from completed matches in tournament
     */
    public TournamentStats getPlayerTournamentStats(int playerId, int tournamentId, List<Match> allMatches) {
        TournamentStats stats = new TournamentStats();
        
        int kills = 0, deaths = 0, assists = 0, matchesPlayed = 0, matchesWon = 0;
        
        android.util.Log.d("TournamentStats", "\n========== getPlayerTournamentStats START ==========");
        android.util.Log.d("TournamentStats", "Player ID: " + playerId + " | Tournament ID: " + tournamentId);
        android.util.Log.d("TournamentStats", "Total matches in list: " + (allMatches != null ? allMatches.size() : "null"));
        
        if (allMatches == null || allMatches.isEmpty()) {
            android.util.Log.w("TournamentStats", "No matches provided!");
            return stats;
        }
        
        for (Match match : allMatches) {
            android.util.Log.d("TournamentStats", "\nChecking Match ID: " + match.getId() + 
                    " | TournamentId: " + match.getTournamentId() + 
                    " | Status: " + match.getStatus() +
                    " | PlayerStats count: " + (match.getPlayerStats() != null ? match.getPlayerStats().size() : "null"));
            
            // Only count completed matches in this tournament
            if (match.getTournamentId() == tournamentId) {
                android.util.Log.d("TournamentStats", "  ✓ Tournament matches!");
                
                if (match.isCompleted()) {
                    android.util.Log.d("TournamentStats", "  ✓ Match is completed!");
                    
                    // Find player's stats in this match
                    if (match.getPlayerStats() != null) {
                        android.util.Log.d("TournamentStats", "  Searching " + match.getPlayerStats().size() + " player entries...");
                        
                        for (PlayerMatchStats pms : match.getPlayerStats()) {
                            if (pms.getPlayerId() == playerId) {
                                android.util.Log.d("TournamentStats", "  ✓✓✓ FOUND PLAYER! K:" + pms.getKills() + " D:" + pms.getDeaths() + " A:" + pms.getAssists());
                                
                                kills += pms.getKills();
                                deaths += pms.getDeaths();
                                assists += pms.getAssists();
                                matchesPlayed++;
                                
                                // Check if player's team won
                                if (match.getWinnerId() != null) {
                                    Player player = null;
                                    try {
                                        player = playerRepository.getById(playerId).getResult();
                                        if (player != null && player.getTeamId() != null && player.getTeamId().equals(match.getWinnerId())) {
                                            matchesWon++;
                                            android.util.Log.d("TournamentStats", "    Team WON this match!");
                                        }
                                    } catch (Exception e) {
                                        android.util.Log.e("TournamentStats", "    Error checking team: " + e.getMessage());
                                    }
                                }
                                break;
                            }
                        }
                    } else {
                        android.util.Log.d("TournamentStats", "  ! playerStats is null");
                    }
                } else {
                    android.util.Log.d("TournamentStats", "  ✗ Match NOT completed (status: " + match.getStatus() + ")");
                }
            } else {
                android.util.Log.d("TournamentStats", "  ✗ Different tournament (want " + tournamentId + ")");
            }
        }
        
        stats.setKills(kills);
        stats.setDeaths(deaths);
        stats.setAssists(assists);
        stats.setMatchesPlayed(matchesPlayed);
        stats.setMatchesWon(matchesWon);
        
        android.util.Log.d("TournamentStats", "\nFINAL STATS: K:" + kills + " D:" + deaths + " A:" + assists + 
                " MP:" + matchesPlayed + " MW:" + matchesWon);
        android.util.Log.d("TournamentStats", "========== getPlayerTournamentStats END ==========\n");
        
        return stats;
    }
}
