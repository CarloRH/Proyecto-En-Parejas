package com.example.fithub360.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fithub360.R;
import com.example.fithub360.adapters.AchievementAdapter;
import com.example.fithub360.database.DatabaseHelper;
import com.example.fithub360.models.Achievement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementsFragment extends Fragment {
    private RecyclerView recyclerView;
    private AchievementAdapter adapter;
    private DatabaseHelper databaseHelper;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "AchievementsPrefs";
    private List<Achievement> achievements;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = new DatabaseHelper(requireContext());
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupRecyclerView(view);
        initializeAchievements();
        updateAchievements();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.achievementsRecyclerView);
        adapter = new AchievementAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void initializeAchievements() {
        achievements = new ArrayList<>();
        String[] categories = {"Pecho", "Espalda", "Piernas", "Brazos", "Abdominales"};

        for (String category : categories) {
            achievements.add(new Achievement(
                category + "_1",
                "Principiante " + category,
                "Completa 3 ejercicios de " + category.toLowerCase(),
                category,
                3
            ));
            achievements.add(new Achievement(
                category + "_2",
                "Intermedio " + category,
                "Completa 6 ejercicios de " + category.toLowerCase(),
                category,
                6
            ));
            achievements.add(new Achievement(
                category + "_3",
                "Experto " + category,
                "Completa 10 ejercicios de " + category.toLowerCase(),
                category,
                10
            ));
        }

        adapter.setAchievements(achievements);
    }

    private void updateAchievements() {
        Map<String, Integer> exerciseCounts = new HashMap<>();

        // Obtener conteos de ejercicios por categoría
        for (Achievement achievement : achievements) {
            String category = achievement.getCategory();
            int count = databaseHelper.getCompletedExercisesCount(category);
            exerciseCounts.put(category, count);

            // Actualizar progreso
            achievement.setCurrentProgress(count);

            // Guardar estado en SharedPreferences si está desbloqueado
            if (achievement.isUnlocked()) {
                prefs.edit().putBoolean(achievement.getId(), true).apply();
            }

            adapter.updateAchievement(achievement);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAchievements(); // Actualizar logros cada vez que el fragmento se muestra
    }
}
