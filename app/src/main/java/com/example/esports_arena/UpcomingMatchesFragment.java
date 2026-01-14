package com.example.esports_arena;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esports_arena.data.MatchRepository;
import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.data.TeamRepository;
import com.example.esports_arena.data.TournamentRepository;
import com.example.esports_arena.model.Match;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.Team;
import com.example.esports_arena.model.Tournament;
import com.example.esports_arena.ui.UpcomingMatchesAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UpcomingMatchesFragment extends Fragment {

    private static final String ARG_PLAYER_ID = "arg_player_id";

    private RecyclerView upcomingMatchesRecycler;
    private ProgressBar upcomingLoading;
    private TextView upcomingStatus;

    private UpcomingMatchesAdapter adapter;
    private MatchRepository matchRepository;
    private PlayerRepository playerRepository;
    private TeamRepository teamRepository;
    private TournamentRepository tournamentRepository;
    private int playerId;
    private Integer playerTeamId;

    private List<Match> allMatches = new ArrayList<>();
    private Map<Integer, Team> teamMap = new HashMap<>();
    private Map<Integer, Tournament> tournamentMap = new HashMap<>();

    public static UpcomingMatchesFragment newInstance(int playerId) {
        UpcomingMatchesFragment fragment = new UpcomingMatchesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upcoming_matches, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        upcomingMatchesRecycler = view.findViewById(R.id.upcomingMatchesRecycler);
        upcomingLoading = view.findViewById(R.id.upcomingLoading);
        upcomingStatus = view.findViewById(R.id.upcomingStatus);

        adapter = new UpcomingMatchesAdapter(teamMap, tournamentMap);
        upcomingMatchesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingMatchesRecycler.setAdapter(adapter);

        matchRepository = new MatchRepository();
        playerRepository = new PlayerRepository();
        teamRepository = new TeamRepository();
        tournamentRepository = new TournamentRepository();

        playerId = getArguments() != null ? getArguments().getInt(ARG_PLAYER_ID, -1) : -1;
        if (playerId == -1) {
            upcomingStatus.setText("Missing player id");
            return;
        }

        loadData();
    }

    private void loadData() {
        setLoading(true);

        // Load all tournaments
        tournamentRepository.getAll().addOnCompleteListener(tournamentTask -> {
            if (tournamentTask.isSuccessful() && tournamentTask.getResult() != null) {
                List<Tournament> tournaments = tournamentTask.getResult();
                for (Tournament tournament : tournaments) {
                    tournamentMap.put(tournament.getId(), tournament);
                }
            }

            // Load all teams
            teamRepository.getAll().addOnCompleteListener(teamTask -> {
                if (teamTask.isSuccessful() && teamTask.getResult() != null) {
                    List<Team> teams = teamTask.getResult();
                    for (Team t : teams) {
                        teamMap.put(t.getId(), t);
                    }
                }

            // Load player to get their team
            playerRepository.getById(playerId).addOnCompleteListener(playerTask -> {
                if (playerTask.isSuccessful() && playerTask.getResult() != null) {
                    Player player = playerTask.getResult();
                    playerTeamId = player.getTeamId();
                }

                // Then load all matches
                matchRepository.getAll().addOnCompleteListener(matchTask -> {
                    if (matchTask.isSuccessful() && matchTask.getResult() != null) {
                        allMatches = matchTask.getResult();

                        // Filter for upcoming matches
                        List<Match> upcomingMatches = filterUpcomingMatches(allMatches);

                        if (upcomingMatches.isEmpty()) {
                            upcomingStatus.setText("No upcoming matches scheduled");
                            upcomingMatchesRecycler.setVisibility(View.GONE);
                        } else {
                            // Sort by scheduled time
                            upcomingMatches.sort((a, b) -> Long.compare(a.getScheduledTimeAsLong(), b.getScheduledTimeAsLong()));
                            adapter.setMatches(upcomingMatches);
                            upcomingMatchesRecycler.setVisibility(View.VISIBLE);
                            upcomingStatus.setText("");
                        }

                        setLoading(false);
                    } else {
                        upcomingStatus.setText("Failed to load matches");
                        setLoading(false);
                    }
                });
            });
        });
        });
    }

    private List<Match> filterUpcomingMatches(List<Match> matches) {
        long currentTime = System.currentTimeMillis();
        
        List<Match> filtered = new ArrayList<>();
        for (Match m : matches) {
            // Check status
            String status = m.getStatus();
            if (status == null || !status.equals("SCHEDULED")) {
                continue;
            }
            
            // Check scheduled time
            long scheduledTime = m.getScheduledTimeAsLong();
            if (scheduledTime <= 0 || scheduledTime <= currentTime) {
                continue;
            }
            
            // Check team (if playerTeamId is available)
            if (playerTeamId != null) {
                if (m.getTeam1Id() != playerTeamId && m.getTeam2Id() != playerTeamId) {
                    continue;
                }
            }
            
            filtered.add(m);
        }
        
        return filtered;
    }

    private void setLoading(boolean loading) {
        upcomingLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        upcomingMatchesRecycler.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
