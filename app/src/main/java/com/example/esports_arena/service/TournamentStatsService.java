package com.example.esports_arena.service;

import com.example.esports_arena.data.MatchRepository;
import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.model.Match;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.PlayerMatchStats;
import com.example.esports_arena.model.TournamentStats;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
    private Map<Integer, Integer> playerTeamCache = new HashMap<>();

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
            stats.setKills(0);
            stats.setDeaths(0);
            stats.setAssists(0);
            stats.setMatchesPlayed(0);
            stats.setMatchesWon(0);
            return stats;
        }
        
        // First pass: collect all team IDs where player was involved and match result
        Integer playerTeamId = playerTeamCache.get(playerId);
        
        try {
            for (Match match : allMatches) {
                if (match == null) continue;
                
                android.util.Log.d("TournamentStats", "\nChecking Match ID: " + match.getId() + 
                        " | TournamentId: " + match.getTournamentId() + 
                        " | Status: " + match.getStatus() +
                        " | Team1: " + match.getTeam1Id() + " Team2: " + match.getTeam2Id() +
                        " | PlayerStats count: " + (match.getPlayerStats() != null ? match.getPlayerStats().size() : "null"));
                
                // Only count completed matches in this tournament
                if (match.getTournamentId() == tournamentId) {
                    android.util.Log.d("TournamentStats", "  ✓ Tournament matches!");
                    
                    if (match.isCompleted()) {
                        android.util.Log.d("TournamentStats", "  ✓ Match is completed!");
                        
                        // Find player's stats in this match
                        if (match.getPlayerStats() != null && !match.getPlayerStats().isEmpty()) {
                            android.util.Log.d("TournamentStats", "  Searching " + match.getPlayerStats().size() + " player entries...");
                            
                            boolean foundPlayer = false;
                            for (PlayerMatchStats pms : match.getPlayerStats()) {
                                if (pms == null) continue;
                                
                                if (pms.getPlayerId() == playerId) {
                                    android.util.Log.d("TournamentStats", "  ✓✓✓ FOUND PLAYER! K:" + pms.getKills() + " D:" + pms.getDeaths() + " A:" + pms.getAssists());
                                    
                                    kills += pms.getKills();
                                    deaths += pms.getDeaths();
                                    assists += pms.getAssists();
                                    matchesPlayed++;
                                    foundPlayer = true;
                                    
                                    // Determine player's team - check which team roster this player belongs to
                                    // We need to infer from the match structure
                                    // For now, we'll try to get it from the player object once
                                    if (playerTeamId == null) {
                                        try {
                                            // Try to fetch player synchronously (not recommended but quick fix)
                                            android.util.Log.d("TournamentStats", "  Attempting to get player team...");
                                            com.google.android.gms.tasks.Task<Player> playerTask = playerRepository.getById(playerId);
                                            int retries = 0;
                                            while (!playerTask.isComplete() && retries < 10) {
                                                Thread.sleep(100);
                                                retries++;
                                            }
                                            if (playerTask.isSuccessful() && playerTask.getResult() != null) {
                                                Player p = playerTask.getResult();
                                                if (p.getTeamId() != null) {
                                                    playerTeamId = p.getTeamId();
                                                    playerTeamCache.put(playerId, playerTeamId);
                                                    android.util.Log.d("TournamentStats", "  Got player team: " + playerTeamId);
                                                }
                                            }
                                        } catch (Exception e) {
                                            android.util.Log.e("TournamentStats", "  Error getting player team", e);
                                        }
                                    }
                                    
                                    // Check if player's team won
                                    if (match.getWinnerId() != null && playerTeamId != null) {
                                        if (playerTeamId == match.getWinnerId()) {
                                            matchesWon++;
                                            android.util.Log.d("TournamentStats", "    Team WON this match!");
                                        } else {
                                            android.util.Log.d("TournamentStats", "    Team LOST (winner: " + match.getWinnerId() + ", player team: " + playerTeamId + ")");
                                        }
                                    }
                                    break;
                                }
                            }
                            
                            if (!foundPlayer) {
                                android.util.Log.d("TournamentStats", "  Player " + playerId + " not found in this match");
                            }
                        } else {
                            android.util.Log.d("TournamentStats", "  ! playerStats is null or empty");
                        }
                    } else {
                        android.util.Log.d("TournamentStats", "  ✗ Match NOT completed (status: " + match.getStatus() + ")");
                    }
                } else {
                    android.util.Log.d("TournamentStats", "  ✗ Different tournament (want " + tournamentId + ")");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("TournamentStats", "Exception during stats calculation", e);
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
