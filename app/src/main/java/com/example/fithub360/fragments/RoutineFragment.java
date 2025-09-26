package com.example.fithub360.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
        Spinner spinner = view.findViewById(R.id.muscleGroupSpinner);
        String[] groups = new String[]{
                "Pecho (ID 1)",
                "Espalda (ID 2)",
                "Piernas (ID 3)",
                "Hombros (ID 4)",
                "Bíceps (ID 5)",
                "Tríceps (ID 6)",
                "Abdominales (ID 7)",
                "Antebrazos (ID 8)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (first) { first = false; return; }
                int muscleId = position + 1; // mapeo directo según orden
                Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
                intent.putExtra("muscle_group_id", muscleId);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
