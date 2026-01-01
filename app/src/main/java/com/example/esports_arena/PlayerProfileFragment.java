package com.example.esports_arena;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.esports_arena.data.PlayerRepository;
import com.example.esports_arena.data.TeamRepository;
import com.example.esports_arena.model.Player;
import com.example.esports_arena.model.Team;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PlayerProfileFragment extends Fragment {

    private static final String ARG_PLAYER_ID = "arg_player_id";

    private TextView profileUsername;
    private TextView profileRealName;
    private TextView profileRole;
    private TextView profileEmail;
    private TextView profileTeam;
    private SwitchMaterial profileAvailabilitySwitch;
    private TextInputLayout profileAvailabilityReasonLayout;
    private TextInputEditText profileAvailabilityReasonInput;
    private Button profileUpdateAvailabilityButton;
    private ProgressBar profileLoading;
    private TextView profileStatus;

    private PlayerRepository playerRepository;
    private TeamRepository teamRepository;
    private Player currentPlayer;

    public static PlayerProfileFragment newInstance(int playerId) {
        PlayerProfileFragment fragment = new PlayerProfileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_ID, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileUsername = view.findViewById(R.id.profileUsername);
        profileRealName = view.findViewById(R.id.profileRealName);
        profileRole = view.findViewById(R.id.profileRole);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileTeam = view.findViewById(R.id.profileTeam);
        profileAvailabilitySwitch = view.findViewById(R.id.profileAvailabilitySwitch);
        profileAvailabilityReasonLayout = view.findViewById(R.id.profileAvailabilityReasonLayout);
        profileAvailabilityReasonInput = view.findViewById(R.id.profileAvailabilityReasonInput);
        profileUpdateAvailabilityButton = view.findViewById(R.id.profileUpdateAvailabilityButton);
        profileLoading = view.findViewById(R.id.profileLoading);
        profileStatus = view.findViewById(R.id.profileStatus);

        playerRepository = new PlayerRepository();
        teamRepository = new TeamRepository();

        int playerId = getArguments() != null ? getArguments().getInt(ARG_PLAYER_ID, -1) : -1;
        if (playerId == -1) {
            Toast.makeText(requireContext(), "Missing player id", Toast.LENGTH_SHORT).show();
            return;
        }

        loadPlayer(playerId);
        profileUpdateAvailabilityButton.setOnClickListener(v -> updateAvailability());
    }

    private void loadPlayer(int playerId) {
        setLoading(true);
        playerRepository.getById(playerId).addOnCompleteListener(task -> {
            setLoading(false);
            if (!task.isSuccessful()) {
                profileStatus.setText("Failed to load player");
                return;
            }

            Player player = task.getResult();
            if (player == null) {
                profileStatus.setText("Player not found");
                return;
            }

            currentPlayer = player;
            bind(player);
        });
    }

    private void bind(Player player) {
        profileUsername.setText(player.getUsername() != null ? player.getUsername() : "Player");
        profileRealName.setText(player.getRealName() != null ? player.getRealName() : "");
        profileRole.setText(player.getRole() != null ? player.getRole() : "");
        profileEmail.setText(player.getEmail() != null ? player.getEmail() : "");

        if (player.getTeamId() != null) {
            profileTeam.setText("Team: " + player.getTeamId());
            loadTeam(player.getTeamId());
        } else {
            profileTeam.setText("Team: None");
        }

        profileAvailabilitySwitch.setChecked(player.isAvailable());
        if (player.getAvailabilityReason() != null) {
            profileAvailabilityReasonInput.setText(player.getAvailabilityReason());
        } else {
            profileAvailabilityReasonInput.setText("");
        }
    }

    private void loadTeam(int teamId) {
        teamRepository.getById(teamId).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return;
            }
            Team team = task.getResult();
            String tag = team.getTag() != null ? team.getTag() : "";
            String name = team.getName() != null ? team.getName() : ("Team " + teamId);
            profileTeam.setText(tag.isEmpty() ? name : name + " (" + tag + ")");
        });
    }

    private void updateAvailability() {
        if (currentPlayer == null) {
            profileStatus.setText("No player loaded");
            return;
        }
        boolean available = profileAvailabilitySwitch.isChecked();
        String reason = profileAvailabilityReasonInput.getText() != null
                ? profileAvailabilityReasonInput.getText().toString().trim()
                : "";

        currentPlayer.setAvailable(available);
        currentPlayer.setAvailabilityReason(reason.isEmpty() ? null : reason);

        setLoading(true);
        playerRepository.update(currentPlayer).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                profileStatus.setText(available ? "Set to available" : "Set to unavailable");
            } else {
                profileStatus.setText("Failed to update availability");
            }
        });
    }

    private void setLoading(boolean loading) {
        profileLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        profileUpdateAvailabilityButton.setEnabled(!loading);
        profileAvailabilitySwitch.setEnabled(!loading);
        profileAvailabilityReasonInput.setEnabled(!loading);
    }
}
