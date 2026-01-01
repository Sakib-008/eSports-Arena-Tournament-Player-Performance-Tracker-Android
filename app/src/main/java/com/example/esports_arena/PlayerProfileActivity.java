package com.example.esports_arena;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.esports_arena.ui.PlayerPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PlayerProfileActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYER_ID = "extra_player_id";
    private int playerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_profile);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);

        playerId = getIntent().getIntExtra(EXTRA_PLAYER_ID, -1);
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
            } else if (position == 1) {
                tab.setText("Stats");
            } else {
                tab.setText("Team");
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Refresh")
                .setIcon(android.R.drawable.ic_popup_sync)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            refreshData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {
        Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
        ViewPager2 viewPager = findViewById(R.id.profileViewPager);
        PlayerPagerAdapter adapter = (PlayerPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            adapter.notifyItemChanged(0);
            adapter.notifyItemChanged(1);
            adapter.notifyItemChanged(2);
        }
    }
}
