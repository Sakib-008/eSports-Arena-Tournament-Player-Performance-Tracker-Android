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

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.data.TeamRepository;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.Team;
import com.example.esports_arena.ui.TeamRosterAdapter;

import java.util.List;

public class TeamFragment extends Fragment {

    private static final String ARG_PLAYER_ID = "arg_player_id";

    private TextView teamName;
    private TextView teamTag;
    private TextView teamRegion;
    private TextView teamLeader;
    private TextView teamStatus;
    private ProgressBar teamLoading;
    private RecyclerView teamRecycler;

    private TeamRosterAdapter rosterAdapter;
    private PlayerRepository playerRepository;
    private TeamRepository teamRepository;
    private int playerId;

    public static TeamFragment newInstance(int playerId) {
        TeamFragment fragment = new TeamFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        teamName = view.findViewById(R.id.teamName);
        teamTag = view.findViewById(R.id.teamTag);
        teamRegion = view.findViewById(R.id.teamRegion);
        teamLeader = view.findViewById(R.id.teamLeader);
        teamStatus = view.findViewById(R.id.teamStatus);
        teamLoading = view.findViewById(R.id.teamLoading);
        teamRecycler = view.findViewById(R.id.teamRecycler);

        rosterAdapter = new TeamRosterAdapter();
        teamRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        teamRecycler.setAdapter(rosterAdapter);

        playerRepository = new PlayerRepository();
        teamRepository = new TeamRepository();

        playerId = getArguments() != null ? getArguments().getInt(ARG_PLAYER_ID, -1) : -1;
        if (playerId == -1) {
            teamStatus.setText("Missing player id");
            return;
        }

        loadPlayerAndTeam();
    }

    private void loadPlayerAndTeam() {
        setLoading(true);
        playerRepository.getById(playerId).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                setLoading(false);
                teamStatus.setText("Failed to load player");
                return;
            }
            Player player = task.getResult();
            if (player.getTeamId() == null) {
                setLoading(false);
                teamStatus.setText("This player is not assigned to a team.");
                return;
            }
            int teamId = player.getTeamId();
            loadTeam(teamId);
            loadRoster(teamId);
        });
    }

    private void loadTeam(int teamId) {
        teamRepository.getById(teamId).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                teamStatus.setText("Team not found");
                return;
            }
            Team team = task.getResult();
            teamName.setText(team.getName() != null ? team.getName() : "Team " + teamId);
            teamTag.setText(team.getTag() != null ? "Tag: " + team.getTag() : "Tag: —");
            teamRegion.setText(team.getRegion() != null ? "Region: " + team.getRegion() : "Region: —");
            if (team.getLeaderId() != null) {
                fetchLeaderName(team.getLeaderId());
            } else {
                teamLeader.setText("Leader: —");
            }
        });
    }

    private void fetchLeaderName(int leaderId) {
        playerRepository.getById(leaderId).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                teamLeader.setText("Leader: " + leaderId);
                return;
            }
            Player leader = task.getResult();
            String name = leader.getUsername() != null ? leader.getUsername() : "Player " + leaderId;
            teamLeader.setText("Leader: " + name);
        });
    }

    private void loadRoster(int teamId) {
        playerRepository.getByTeamId(teamId).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful() || task.getResult() == null) {
                teamStatus.setText("Failed to load roster");
                return;
            }
            List<Player> players = task.getResult();
            rosterAdapter.setPlayers(players);
            teamStatus.setText(players.isEmpty() ? "No roster yet." : "");
        });
    }

    private void setLoading(boolean loading) {
        teamLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        teamRecycler.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }
}
