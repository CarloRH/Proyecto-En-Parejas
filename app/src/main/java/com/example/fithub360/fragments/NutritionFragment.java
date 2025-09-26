package com.example.fithub360.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fithub360.R;
import com.example.fithub360.adapters.FoodAdapter;
import com.example.fithub360.models.Food;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutritionFragment extends Fragment {
    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.foodRecyclerView);
        progressBar = view.findViewById(R.id.loadingProgressBar);

        setupRecyclerView();
        loadMockData();
    }

    private void setupRecyclerView() {
        adapter = new FoodAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadMockData() {
        List<Food> foodList = Arrays.asList(
            new Food("Manzana", "Fruta rica en fibra"),
            new Food("Pollo", "Proteína magra"),
            new Food("Arroz", "Fuente de carbohidratos")
        );
        adapter.setFoods(foodList);
    }
}
