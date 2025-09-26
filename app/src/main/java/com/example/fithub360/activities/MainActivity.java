package com.example.fithub360.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.fithub360.R;
import com.example.fithub360.fragments.RoutineFragment;
import com.example.fithub360.fragments.NutritionFragment;
import com.example.fithub360.fragments.MusicFragment;
import com.example.fithub360.fragments.CoachFragment;
import com.example.fithub360.fragments.AchievementsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();
            if (id == R.id.navigation_routine) {
                selectedFragment = new RoutineFragment();
            } else if (id == R.id.navigation_nutrition) {
                selectedFragment = new NutritionFragment();
            } else if (id == R.id.navigation_music) {
                selectedFragment = new MusicFragment();
            } else if (id == R.id.navigation_coach) {
                selectedFragment = new CoachFragment();
            } else if (id == R.id.navigation_achievements) {
                selectedFragment = new AchievementsFragment();
            } else {
                selectedFragment = new RoutineFragment();
            }
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, selectedFragment)
                .commit();
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new RoutineFragment())
                .commit();
        }
    }
}
