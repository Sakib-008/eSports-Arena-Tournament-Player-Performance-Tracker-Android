package com.example.esports_arena;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.esports_arena.ui.PlayerPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PlayerProfileActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYER_ID = "extra_player_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_profile);

        int playerId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
        if (playerId == -1) {
            finish();
            return;
        }

        TabLayout tabLayout = findViewById(R.id.profileTabLayout);
        ViewPager2 viewPager = findViewById(R.id.profileViewPager);
        viewPager.setAdapter(new PlayerPagerAdapter(this, playerId));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Profile");
            } else {
                tab.setText("Stats");
            }
        }).attach();
    }
}
