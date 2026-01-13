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
import com.example.esports_arena.data.TeamRepository;
import com.example.esports_arena.model.Match;
import com.example.esports_arena.model.Team;
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
    private TeamRepository teamRepository;
    private int playerId;

    private List<Match> allMatches = new ArrayList<>();
    private Map<Integer, Team> teamMap = new HashMap<>();

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

        adapter = new UpcomingMatchesAdapter(teamMap);
        upcomingMatchesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingMatchesRecycler.setAdapter(adapter);

        matchRepository = new MatchRepository();
        teamRepository = new TeamRepository();

        playerId = getArguments() != null ? getArguments().getInt(ARG_PLAYER_ID, -1) : -1;
        if (playerId == -1) {
            upcomingStatus.setText("Missing player id");
            return;
        }

        loadData();
    }

    private void loadData() {
        setLoading(true);

        android.util.Log.d("UpcomingMatches", "Loading upcoming matches...");

        // Load all teams first
        teamRepository.getAll().addOnCompleteListener(teamTask -> {
            if (teamTask.isSuccessful() && teamTask.getResult() != null) {
                List<Team> teams = teamTask.getResult();
                for (Team t : teams) {
                    teamMap.put(t.getId(), t);
                }
                android.util.Log.d("UpcomingMatches", "Loaded " + teams.size() + " teams");
            } else {
                android.util.Log.e("UpcomingMatches", "Failed to load teams");
            }

            // Then load all matches
            matchRepository.getAll().addOnCompleteListener(matchTask -> {
                if (matchTask.isSuccessful() && matchTask.getResult() != null) {
                    allMatches = matchTask.getResult();
                    android.util.Log.d("UpcomingMatches", "Loaded " + allMatches.size() + " total matches");

                    // Filter for upcoming matches (SCHEDULED status and future time)
                    List<Match> upcomingMatches = filterUpcomingMatches(allMatches);
                    android.util.Log.d("UpcomingMatches", "Filtered " + upcomingMatches.size() + " upcoming matches");

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
                    android.util.Log.e("UpcomingMatches", "Failed to load matches");
                    upcomingStatus.setText("Failed to load matches");
                    setLoading(false);
                }
            });
        });
    }

    private List<Match> filterUpcomingMatches(List<Match> matches) {
        long currentTime = System.currentTimeMillis();
        
        return matches.stream()
            .filter(m -> {
                // Filter for SCHEDULED matches
                if (m.getStatus() == null || !m.getStatus().toString().equals("SCHEDULED")) {
                    return false;
                }
                
                // Filter for future matches (scheduled time is after now)
                long scheduledTime = m.getScheduledTimeAsLong();
                if (scheduledTime <= 0) {
                    return false; // No valid scheduled time
                }
                
                return scheduledTime > currentTime;
            })
            .collect(Collectors.toList());
    }

    private void setLoading(boolean loading) {
        upcomingLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        upcomingMatchesRecycler.setVisibility(loading ? View.GONE : View.VISIBLE);
    }
}
