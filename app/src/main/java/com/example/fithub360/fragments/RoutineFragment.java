package com.example.fithub360.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fithub360.R;
import com.example.fithub360.activities.ExerciseDetailActivity;

public class RoutineFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Botones visibles (sin Abdomen, que fue eliminado del layout)
        Button btnChest = view.findViewById(R.id.btnChest);      // Texto: BÍCEPS, mantiene grupo 1 (Pecho)
        Button btnBack = view.findViewById(R.id.btnBack);        // Texto: TRÍCEPS, mantiene grupo 2 (Espalda)
        Button btnLegs = view.findViewById(R.id.btnLegs);        // Texto: ESPALDA, mantiene grupo 3 (Piernas)
        Button btnShoulders = view.findViewById(R.id.btnShoulders); // Texto: PECHO, mantiene grupo 4 (Hombros)
        Button btnBiceps = view.findViewById(R.id.btnBiceps);    // Texto: ANTEBRAZOS, mantiene grupo 5 (Bíceps)
        Button btnTriceps = view.findViewById(R.id.btnTriceps);  // Texto: ABDOMEN, mantiene grupo 6 (Tríceps)
        Button btnForearms = view.findViewById(R.id.btnForearms);// Texto: PIERNAS, mantiene grupo 8 (Antebrazos)

        if (btnChest != null) btnChest.setOnClickListener(v -> openExercises(1, "Bíceps"));
        if (btnBack != null) btnBack.setOnClickListener(v -> openExercises(2, "Tríceps"));
        if (btnLegs != null) btnLegs.setOnClickListener(v -> openExercises(3, "Espalda"));
        if (btnShoulders != null) btnShoulders.setOnClickListener(v -> openExercises(4, "Pecho"));
        if (btnBiceps != null) btnBiceps.setOnClickListener(v -> openExercises(5, "Antebrazos"));
        if (btnTriceps != null) btnTriceps.setOnClickListener(v -> openExercises(6, "Abdomen"));
        if (btnForearms != null) btnForearms.setOnClickListener(v -> openExercises(8, "Piernas"));
    }

    private void openExercises(int muscleGroupId, String displayName) {
        Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
        intent.putExtra("muscle_group_id", muscleGroupId);
        intent.putExtra("display_group_name", displayName);
        startActivity(intent);
    }
}
