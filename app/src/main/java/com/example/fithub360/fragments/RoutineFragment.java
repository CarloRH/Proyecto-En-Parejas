package com.example.fithub360.fragments;

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
import com.example.fithub360.adapters.ExerciseAdapter;
import com.example.fithub360.models.Exercise;
import java.util.Arrays;
import java.util.List;

public class RoutineFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routine, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.routineRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<Exercise> exercises = Arrays.asList(
            new Exercise("Push Ups", "Ejercicio de pecho y brazos"),
            new Exercise("Squats", "Ejercicio de piernas"),
            new Exercise("Plank", "Ejercicio de abdomen"),
            new Exercise("Burpees", "Ejercicio cardiovascular")
        );
        ExerciseAdapter adapter = new ExerciseAdapter(exercises);
        recyclerView.setAdapter(adapter);
    }
}
