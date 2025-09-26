package com.example.fithub360.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.fithub360.R;
import com.example.fithub360.database.DatabaseHelper;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ExerciseExecutionActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView nameText;
    private TextView descText;
    private TextView timerText;
    private TextView repsText;
    private Button startTimerBtn;
    private Button countRepBtn;
    private ProgressBar imageProgress;

    private int exerciseId;
    private String exerciseName;
    private String exerciseDescription;
    private int muscleGroupId;

    private int repsCount = 0;
    private boolean completedInSession = false;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_execution);

        imageView = findViewById(R.id.exerciseImageView);
        nameText = findViewById(R.id.exerciseNameText);
        descText = findViewById(R.id.exerciseDescText);
        timerText = findViewById(R.id.timerText);
        repsText = findViewById(R.id.repsText);
        startTimerBtn = findViewById(R.id.startTimerBtn);
        countRepBtn = findViewById(R.id.countRepBtn);
        imageProgress = findViewById(R.id.imageLoadingProgress);

        dbHelper = new DatabaseHelper(this);

        exerciseId = getIntent().getIntExtra("exercise_id", 0);
        exerciseName = getIntent().getStringExtra("exercise_name");
        exerciseDescription = getIntent().getStringExtra("exercise_description");
        muscleGroupId = getIntent().getIntExtra("muscle_group_id", 0);

        nameText.setText(exerciseName != null ? exerciseName : "Ejercicio");
        descText.setText(exerciseDescription != null ? exerciseDescription : "");

        loadMainImage(exerciseId);

        startTimerBtn.setOnClickListener(v -> startTimer());
        countRepBtn.setOnClickListener(v -> {
            if (completedInSession) return;
            repsCount++;
            repsText.setText("Reps: " + repsCount + "/10");
            if (repsCount >= 10) {
                onExerciseCompleted();
            }
        });
    }

    private void startTimer() {
        if (completedInSession) return;
        startTimerBtn.setEnabled(false);
        new CountDownTimer(30_000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Tiempo: " + (millisUntilFinished / 1000) + "s");
            }
            public void onFinish() {
                timerText.setText("¡Completado!");
                onExerciseCompleted();
            }
        }.start();
    }

    private void onExerciseCompleted() {
        if (completedInSession) return;
        completedInSession = true;
        dbHelper.insertCompletedExercise(exerciseName != null ? exerciseName : "Ejercicio", muscleGroupId);
        int count = dbHelper.getCountByMuscleGroup(muscleGroupId);
        int oldLevel = getAchievementLevel(this, muscleGroupId);
        int newLevel = calculateAchievementLevel(count);
        updateAchievementLevel(this, muscleGroupId, newLevel);
        if (newLevel > oldLevel) {
            String groupName = mapGroupName(muscleGroupId);
            Toast.makeText(this, "¡Logro desbloqueado: Nivel " + newLevel + " en " + groupName + "!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Ejercicio registrado", Toast.LENGTH_SHORT).show();
        }
        // Desactivar botones
        startTimerBtn.setEnabled(false);
        countRepBtn.setEnabled(false);
    }

    private void loadMainImage(int exId) {
        imageProgress.setVisibility(View.VISIBLE);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wger.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        com.example.fithub360.network.WgerApiService api = retrofit.create(com.example.fithub360.network.WgerApiService.class);
        api.getExerciseImages(exId, true).enqueue(new Callback<com.example.fithub360.models.ExerciseImageResponse>() {
            @Override
            public void onResponse(Call<com.example.fithub360.models.ExerciseImageResponse> call, Response<com.example.fithub360.models.ExerciseImageResponse> response) {
                imageProgress.setVisibility(View.GONE);
                String url = null;
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null && !response.body().getResults().isEmpty()) {
                    for (com.example.fithub360.models.ExerciseImageResponse.ExerciseImage img : response.body().getResults()) {
                        if (img.is_main) { url = img.image; break; }
                    }
                    if (url == null) {
                        url = response.body().getResults().get(0).image;
                    }
                }
                if (url != null) {
                    String cleanUrl = url.trim();
                    Glide.with(ExerciseExecutionActivity.this)
                            .load(cleanUrl)
                            .placeholder(R.drawable.ic_exercise_placeholder)
                            .error(R.drawable.ic_exercise_placeholder)
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.ic_exercise_placeholder);
                }
            }

            @Override
            public void onFailure(Call<com.example.fithub360.models.ExerciseImageResponse> call, Throwable t) {
                imageProgress.setVisibility(View.GONE);
                imageView.setImageResource(R.drawable.ic_exercise_placeholder);
            }
        });
    }

    private static int calculateAchievementLevel(int completedCount) {
        if (completedCount >= 10) return 3;
        if (completedCount >= 6) return 2;
        if (completedCount >= 3) return 1;
        return 0;
    }

    private static void updateAchievementLevel(Context ctx, int groupId, int level) {
        SharedPreferences prefs = ctx.getSharedPreferences("AchievementsLevels", Context.MODE_PRIVATE);
        prefs.edit().putInt(mapLevelKey(groupId), level).apply();
    }

    private static int getAchievementLevel(Context ctx, int groupId) {
        SharedPreferences prefs = ctx.getSharedPreferences("AchievementsLevels", Context.MODE_PRIVATE);
        return prefs.getInt(mapLevelKey(groupId), 0);
    }

    private static String mapLevelKey(int groupId) {
        switch (groupId) {
            case 1: return "achievement_chest_level";
            case 2: return "achievement_back_level";
            case 3: return "achievement_legs_level";
            case 4: return "achievement_shoulders_level";
            case 5: return "achievement_biceps_level";
            case 6: return "achievement_triceps_level";
            case 7: return "achievement_abs_level";
            case 8: return "achievement_forearms_level";
            default: return "achievement_other_level";
        }
    }

    private static String mapGroupName(int groupId) {
        switch (groupId) {
            case 1: return "Pecho";
            case 2: return "Espalda";
            case 3: return "Piernas";
            case 4: return "Hombros";
            case 5: return "Bíceps";
            case 6: return "Tríceps";
            case 7: return "Abdominales";
            case 8: return "Antebrazos";
            default: return "Otro";
        }
    }
}
