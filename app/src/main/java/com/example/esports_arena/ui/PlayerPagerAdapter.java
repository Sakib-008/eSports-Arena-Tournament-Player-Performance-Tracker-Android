package com.example.esports_arena.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.esports_arena.LeaderboardFragment;
import com.example.esports_arena.PlayerProfileFragment;
import com.example.esports_arena.PlayerStatsFragment;
import com.example.esports_arena.TeamFragment;

public class PlayerPagerAdapter extends FragmentStateAdapter {

    private final int playerId;

    public PlayerPagerAdapter(@NonNull FragmentActivity fragmentActivity, int playerId) {
        super(fragmentActivity);
        this.playerId = playerId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return PlayerStatsFragment.newInstance(playerId);
        }
        if (position == 2) {
            return TeamFragment.newInstance(playerId);
        }
        if (position == 3) {
            return LeaderboardFragment.newInstance(playerId);
        }
        return PlayerProfileFragment.newInstance(playerId);
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
