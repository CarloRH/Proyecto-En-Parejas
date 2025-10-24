package com.example.fithub360.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fithub360.R;
import com.example.fithub360.activities.SavedRoutinesActivity;
import com.example.fithub360.models.ChatCompletionRequest;
import com.example.fithub360.models.ChatCompletionResponse;
import com.example.fithub360.models.Message;
import com.example.fithub360.network.NvidiaApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CoachFragment extends Fragment {

    private static final String TAG = "CoachFragment";

    // API key exclusiva para el coach (proporcionada por el usuario)
    private static final String NVIDIA_API_KEY = "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";


    private EditText editGoal; // campo para que el usuario ingrese su objetivo
    private Spinner spinnerExperience;
    private View btnAsk;
    private Button btnSaveRoutine;
    private Button btnSavedHistory;
    private ProgressBar progressBar;
    private TextView routineTextView;

    private NvidiaApiService apiService;
    private Call<ChatCompletionResponse> currentCall;

    private static final String PREFS_NAME = "fithub_saved_routines";
    private static final String KEY_SAVED = "saved_routines";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coach, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupSpinner();
        setupApi();
        setupActions();
    }

    private void bindViews(View view) {
        editGoal = view.findViewById(R.id.editGoal);
        spinnerExperience = view.findViewById(R.id.spinnerExperience);
        btnAsk = view.findViewById(R.id.btnAskCoach);
        btnSaveRoutine = view.findViewById(R.id.btnSaveRoutine);
        btnSavedHistory = view.findViewById(R.id.btnSavedHistory);
        progressBar = view.findViewById(R.id.loadingProgressBarCoach);
        routineTextView = view.findViewById(R.id.routineTextView);
        routineTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    private void setupSpinner() {
        List<String> items = Arrays.asList("Selecciona nivel (opcional)", "Principiante", "Intermedio", "Avanzado");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExperience.setAdapter(adapter);
    }

    private void setupApi() {
        Interceptor headerInterceptor = chain -> {
            Request req = chain.request().newBuilder()
                    .header("Authorization", "Bearer " + NVIDIA_API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            return chain.proceed(req);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://integrate.api.nvidia.com/v1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(NvidiaApiService.class);
    }

    private void setupActions() {
        btnAsk.setOnClickListener(v -> {
            if (currentCall != null && !currentCall.isCanceled()) {
                return;
            }
            try {
                String goal = safeText(editGoal);
                if (TextUtils.isEmpty(goal)) {
                    Toast.makeText(requireContext(), "Por favor, ingresa tu objetivo (p. ej. perder peso, ganar masa muscular, mejorar resistencia).", Toast.LENGTH_SHORT).show();
                    return;
                }

                String experience = spinnerExperience.getSelectedItem() != null ? spinnerExperience.getSelectedItem().toString() : null;
                if (experience != null && experience.startsWith("Selecciona")) experience = null;

                List<Message> messages = new ArrayList<>();
                messages.add(new Message("system", "Eres un coach de gimnasio profesional y empático. NO HAGAS PREGUNTAS: no solicites información adicional al usuario. Con la información ya proporcionada por el usuario, genera directamente una rutina de entrenamiento clara y segura acorde al objetivo y nivel. Responde en español, con instrucciones paso a paso, ejercicios por sesión, repeticiones/series/tiempos y consejos de progresión. No des diagnóstico médico ni recomiendes medicamentos. Responde en texto plano, sin viñetas, sin markdown y mantén un tono motivador."));
                messages.add(new Message("user", "Mi objetivo: " + goal + (experience != null ? ". Nivel: " + experience : "")));

                ChatCompletionRequest request = new ChatCompletionRequest(
                        "meta/llama3-8b-instruct",
                        messages,
                        0.5,
                        1.0,
                        512
                );

                setLoading(true);
                routineTextView.setVisibility(View.GONE);

                currentCall = apiService.createCompletion(request);
                currentCall.enqueue(new Callback<ChatCompletionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatCompletionResponse> call, @NonNull Response<ChatCompletionResponse> response) {
                        setLoading(false);
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getChoices() != null
                                && !response.body().getChoices().isEmpty()
                                && response.body().getChoices().get(0).getMessage() != null) {
                            String content = response.body().getChoices().get(0).getMessage().getContent();
                            if (!TextUtils.isEmpty(content)) {
                                routineTextView.setText(content);
                                routineTextView.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                        Toast.makeText(requireContext(), "No se pudo generar la rutina. Verifica tu conexión.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatCompletionResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        if (!isAdded()) return;
                        Log.e(TAG, "Error de red: " + t.getMessage(), t);
                        Toast.makeText(requireContext(), "No se pudo generar la rutina. Verifica tu conexión.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error al preparar la solicitud", e);
                Toast.makeText(requireContext(), "No se pudo generar la rutina. Verifica tu conexión.", Toast.LENGTH_SHORT).show();
            }
        });

        // Guardar la rutina actual en SharedPreferences
        btnSaveRoutine.setOnClickListener(v -> saveCurrentRoutine());

        // Abrir historial de rutinas guardadas
        btnSavedHistory.setOnClickListener(v -> {
            try {
                Intent i = new Intent(requireContext(), SavedRoutinesActivity.class);
                startActivity(i);
            } catch (Exception ex) {
                Log.e(TAG, "Error al abrir historial", ex);
                Toast.makeText(requireContext(), "No se pudo abrir el historial.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCurrentRoutine() {
        String content = routineTextView.getText() == null ? "" : routineTextView.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(requireContext(), "No hay rutina para guardar. Genera una primero.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_SAVED, null);
        JSONArray arr = new JSONArray();
        try {
            if (raw != null) {
                arr = new JSONArray(raw);
            }
        } catch (JSONException e) {
            Log.w(TAG, "JSON parse error, creando array nuevo", e);
            arr = new JSONArray();
        }

        try {
            JSONObject item = new JSONObject();
            long ts = System.currentTimeMillis();
            item.put("id", ts);
            item.put("timestamp", ts);
            item.put("content", content);
            // título corto: primera línea o primeros 60 caracteres
            String title = content.split("\n")[0];
            if (title.length() > 60) title = title.substring(0, 57) + "...";
            item.put("title", title);
            arr.put(item);
            prefs.edit().putString(KEY_SAVED, arr.toString()).apply();
            Toast.makeText(requireContext(), "Rutina guardada.", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e(TAG, "Error guardando rutina", e);
            Toast.makeText(requireContext(), "No se pudo guardar la rutina.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAsk.setEnabled(!loading);
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
