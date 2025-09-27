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
import java.util.concurrent.atomic.AtomicInteger;

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

    private com.example.fithub360.network.WgerApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        recyclerView = findViewById(R.id.exerciseRecyclerView);
        progressBar = findViewById(R.id.loadingProgress);
        emptyView = findViewById(R.id.emptyText);
        TextView title = findViewById(R.id.titleText);

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

        String displayName = getIntent().getStringExtra("display_group_name");
        if (title != null) {
            title.setText("Ejercicios - " + (displayName != null && !displayName.isEmpty() ? displayName : mapGroupName(muscleGroupId)));
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wger.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        api = retrofit.create(com.example.fithub360.network.WgerApiService.class);

        loadExercises(muscleGroupId);
    }

    private static String mapGroupName(int groupId) {
        switch (groupId) {
            case 1: return "Pecho";
            case 2: return "Espalda";
            case 3: return "Piernas";
            case 4: return "Hombros";
            case 5: return "Bíceps";
            case 6: return "Tríceps";
            case 7: return "Abdomen"; // antes: Abdominales
            case 8: return "Antebrazos";
            default: return "";
        }
    }

    private void loadExercises(int muscleId) {
        showLoading(true);
        api.getExercises(2, muscleId, 50).enqueue(new retrofit2.Callback<com.example.fithub360.models.ExerciseResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.fithub360.models.ExerciseResponse> call, retrofit2.Response<com.example.fithub360.models.ExerciseResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getResults() == null) {
                    showLoading(false);
                    emptyView.setVisibility(View.VISIBLE);
                    return;
                }
                List<com.example.fithub360.models.ExerciseResponse.ExerciseItem> items = response.body().getResults();
                // Filtrar por el músculo seleccionado (por si la API devuelve extras)
                List<com.example.fithub360.models.ExerciseResponse.ExerciseItem> filtered = new ArrayList<>();
                for (com.example.fithub360.models.ExerciseResponse.ExerciseItem it : items) {
                    if (it.muscles != null && it.muscles.contains(muscleId)) {
                        filtered.add(it);
                    }
                }
                if (filtered.isEmpty()) {
                    showLoading(false);
                    emptyView.setVisibility(View.VISIBLE);
                    return;
                }
                // Consultar imágenes principales para cada ejercicio y construir la lista final
                fetchImagesForExercises(filtered);
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.fithub360.models.ExerciseResponse> call, Throwable t) {
                showLoading(false);
                emptyView.setVisibility(View.VISIBLE);
                Toast.makeText(ExerciseDetailActivity.this, "Error de red al cargar ejercicios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchImagesForExercises(List<com.example.fithub360.models.ExerciseResponse.ExerciseItem> items) {
        exerciseList.clear();
        AtomicInteger pending = new AtomicInteger(items.size());
        for (com.example.fithub360.models.ExerciseResponse.ExerciseItem it : items) {
            api.getExerciseImages(it.id, true).enqueue(new Callback<com.example.fithub360.models.ExerciseImageResponse>() {
                @Override
                public void onResponse(Call<com.example.fithub360.models.ExerciseImageResponse> call, Response<com.example.fithub360.models.ExerciseImageResponse> response) {
                    String imageUrl = null;
                    if (response.isSuccessful() && response.body() != null && response.body().getResults() != null && !response.body().getResults().isEmpty()) {
                        // Buscar principal; si no hay, tomar la primera
                        for (com.example.fithub360.models.ExerciseImageResponse.ExerciseImage img : response.body().getResults()) {
                            if (img.is_main) { imageUrl = img.image; break; }
                        }
                        if (imageUrl == null) {
                            imageUrl = response.body().getResults().get(0).image;
                        }
                    }
                    // Agregar solo si hay imagen válida
                    if (imageUrl != null) {
                        String cleanUrl = imageUrl.trim();
                        if (!cleanUrl.isEmpty()) {
                            String desc = sanitize(it.description);
                            exerciseList.add(new Exercise(it.id, it.name, desc, cleanUrl));
                        }
                    }
                    if (pending.decrementAndGet() == 0) {
                        // Todas las llamadas han respondido
                        showLoading(false);
                        adapter.notifyDataSetChanged();
                        emptyView.setVisibility(exerciseList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<com.example.fithub360.models.ExerciseImageResponse> call, Throwable t) {
                    if (pending.decrementAndGet() == 0) {
                        showLoading(false);
                        adapter.notifyDataSetChanged();
                        emptyView.setVisibility(exerciseList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            });
        }
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
