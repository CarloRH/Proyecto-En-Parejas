package com.example.fithub360.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fithub360.R;
import com.example.fithub360.adapters.ExerciseAdapter;
import com.example.fithub360.models.Exercise;
import com.example.fithub360.models.ExerciseResponse;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExerciseDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private ExerciseAdapter adapter;
    private final List<Exercise> exerciseList = new ArrayList<>();
    private int muscleGroupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        recyclerView = findViewById(R.id.exerciseRecyclerView);
        progressBar = findViewById(R.id.loadingProgress);
        emptyView = findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExerciseAdapter(exerciseList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(exercise -> {
            Intent intent = new Intent(ExerciseDetailActivity.this, ExerciseExecutionActivity.class);
            intent.putExtra("exercise_id", exercise.getId());
            intent.putExtra("exercise_name", exercise.getName());
            intent.putExtra("exercise_description", exercise.getDescription());
            intent.putExtra("muscle_group_id", muscleGroupId);
            startActivity(intent);
        });

        muscleGroupId = getIntent().getIntExtra("muscle_group_id", 0);
        if (muscleGroupId <= 0) {
            Toast.makeText(this, "Grupo muscular inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadExercises(muscleGroupId);
    }

    private void loadExercises(int muscleId) {
        showLoading(true);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wger.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        com.example.fithub360.network.WgerApiService api = retrofit.create(com.example.fithub360.network.WgerApiService.class);
        api.getExercises(2, muscleId, 50).enqueue(new retrofit2.Callback<com.example.fithub360.models.ExerciseResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.fithub360.models.ExerciseResponse> call, retrofit2.Response<com.example.fithub360.models.ExerciseResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    exerciseList.clear();
                    for (com.example.fithub360.models.ExerciseResponse.ExerciseItem item : response.body().getResults()) {
                        String desc = sanitize(item.description);
                        exerciseList.add(new Exercise(item.id, item.name, desc));
                    }
                    adapter.notifyDataSetChanged();
                    emptyView.setVisibility(exerciseList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.fithub360.models.ExerciseResponse> call, Throwable t) {
                showLoading(false);
                emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(ExerciseDetailActivity.this, "Error de red al cargar ejercicios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String sanitize(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("&nbsp;", " ").trim();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
