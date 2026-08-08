package com.example.civiclensai;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.civiclensai.databinding.ActivityMainBinding;
import com.example.civiclensai.ui.FeedFragment;
import com.example.civiclensai.ui.MapFragment;
import com.example.civiclensai.ui.ReportFragment;
import com.example.civiclensai.ui.auth.LoginActivity;
import com.example.civiclensai.ui.myreports.MyReportsFragment;
import com.example.civiclensai.ui.profile.ProfileFragment;
import com.example.civiclensai.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize osmdroid tile configuration before super.onCreate
        org.osmdroid.config.Configuration.getInstance().load(
                getApplicationContext(),
                android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue("CivicLensAI_SmartTriage_App/1.0 (Android; contact@civiclens.ai)");

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply status bar window inset padding so toolbar is never cut off by camera notch
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, statusBarInsets.top, 0, 0);
            return insets;
        });

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Setup 5-page ViewPager2 Adapter
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setUserInputEnabled(false);

        // Synchronize BottomNavigationView with ViewPager2
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_map) {
                binding.viewPager.setCurrentItem(0, false);
                return true;
            } else if (itemId == R.id.nav_feed) {
                binding.viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.nav_report) {
                binding.viewPager.setCurrentItem(2, false);
                return true;
            } else if (itemId == R.id.nav_my_reports) {
                binding.viewPager.setCurrentItem(3, false);
                return true;
            } else if (itemId == R.id.nav_profile) {
                binding.viewPager.setCurrentItem(4, false);
                return true;
            }
            return false;
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int targetId;
                switch (position) {
                    case 0: targetId = R.id.nav_map; break;
                    case 1: targetId = R.id.nav_feed; break;
                    case 2: targetId = R.id.nav_report; break;
                    case 3: targetId = R.id.nav_my_reports; break;
                    case 4: targetId = R.id.nav_profile; break;
                    default: targetId = R.id.nav_map; break;
                }
                if (binding.bottomNavigation.getSelectedItemId() != targetId) {
                    binding.bottomNavigation.setSelectedItemId(targetId);
                }
            }
        });
    }

    private static class MainPagerAdapter extends FragmentStateAdapter {

        public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new MapFragment();
                case 1: return new FeedFragment();
                case 2: return new ReportFragment();
                case 3: return new MyReportsFragment();
                case 4: return new ProfileFragment();
                default: return new MapFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}