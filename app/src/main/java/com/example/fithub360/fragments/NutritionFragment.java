package com.example.fithub360.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fithub360.R;
import com.example.fithub360.models.ChatCompletionRequest;
import com.example.fithub360.models.ChatCompletionResponse;
import com.example.fithub360.models.Message;
import com.example.fithub360.network.NvidiaApiService;
 import com.example.fithub360.utils.N8nEmailSender;
import com.example.fithub360.utils.SessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NutritionFragment extends Fragment {

    private static final String TAG = "NutritionFragment";

    // ⚠️ Reemplaza "TU_CLAVE_AQUI" con tu clave real de NVIDIA (https://build.nvidia.com/)
    // Nunca subas tu clave a repositorios públicos.
    private static final String NVIDIA_API_KEY = "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";

    private EditText editWeight;
    private EditText editHeight;
    private EditText editAge;
    private Spinner spinnerGender;
    private View btnCalculate;
    private ProgressBar progressBar;
    private TextView imcTextView;
    private TextView recommendationTextView;

    // Nuevos: envío por correo
    private View btnSendEmailNutrition;
    private ProgressBar sendingProgressNutrition;

    private NvidiaApiService apiService;
    private Call<ChatCompletionResponse> currentCall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nutrition, container, false);
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
        editWeight = view.findViewById(R.id.editWeight);
        editHeight = view.findViewById(R.id.editHeight);
        editAge = view.findViewById(R.id.editAge);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        btnCalculate = view.findViewById(R.id.btnCalculate);
        progressBar = view.findViewById(R.id.loadingProgressBar);
        imcTextView = view.findViewById(R.id.imcTextView);
        recommendationTextView = view.findViewById(R.id.recommendationTextView);
        recommendationTextView.setMovementMethod(new ScrollingMovementMethod());

        btnSendEmailNutrition = view.findViewById(R.id.btnSendEmailNutrition);
        sendingProgressNutrition = view.findViewById(R.id.sendingProgressNutrition);
        btnSendEmailNutrition.setEnabled(false); // deshabilitado hasta tener recomendación
    }

    private void setupSpinner() {
        List<String> items = Arrays.asList("Selecciona género (opcional)", "Masculino", "Femenino", "Otro");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
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
        btnCalculate.setOnClickListener(v -> {
            if (currentCall != null && !currentCall.isCanceled()) {
                // Evita múltiples solicitudes simultáneas
                return;
            }
            try {
                String weightStr = safeText(editWeight);
                String heightStr = safeText(editHeight);
                String ageStr = safeText(editAge);

                if (TextUtils.isEmpty(weightStr) || TextUtils.isEmpty(heightStr)) {
                    Toast.makeText(requireContext(), "Por favor, ingresa peso y altura.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double weight = parseDoubleSafe(weightStr);
                double heightCm = parseDoubleSafe(heightStr);
                if (weight <= 0 || heightCm <= 0) {
                    Toast.makeText(requireContext(), "Valores inválidos de peso o altura.", Toast.LENGTH_SHORT).show();
                    return;
                }

                double heightMeters = heightCm / 100.0d;
                double imc = weight / (heightMeters * heightMeters);
                String classification = classifyImc(imc);

                DecimalFormat df = new DecimalFormat("0.0");
                DecimalFormat df2 = new DecimalFormat("0.00");
                String imcText = "IMC: " + df.format(imc) + " (" + classification + ")";
                imcTextView.setText(imcText);
                imcTextView.setVisibility(View.VISIBLE);

                String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : null;
                if (gender != null && gender.startsWith("Selecciona")) gender = null;

                Integer age = null;
                if (!TextUtils.isEmpty(ageStr)) {
                    try {
                        age = Integer.parseInt(ageStr.trim());
                        if (age <= 0) age = null;
                    } catch (Exception ignored) {}
                }

                StringBuilder userMsg = new StringBuilder();
                userMsg.append("Tengo un IMC de ")
                        .append(df.format(imc)).append(" (").append(classification).append(")")
                        .append(", peso ").append(df2.format(weight)).append(" kg y mido ")
                        .append(df2.format(heightMeters)).append(" m.");
                if (age != null) userMsg.append(" Edad: ").append(age).append(" años.");
                if (gender != null) userMsg.append(" Género: ").append(gender).append(".");
                userMsg.append(" Dame recomendaciones de alimentación.");

                List<Message> messages = new ArrayList<>();
                messages.add(new Message("system", "Eres un nutricionista profesional y empático. Responde en español con consejos prácticos, realistas y motivadores sobre alimentación saludable. No menciones medicamentos, suplementos, ni dietas extremas. Enfócate en hábitos sostenibles, hidratación, control de porciones y alimentos integrales. Responde en texto plano, sin viñetas, números, títulos ni markdown."));
                messages.add(new Message("user", userMsg.toString()));

                ChatCompletionRequest request = new ChatCompletionRequest(
                        "meta/llama3-8b-instruct",
                        messages,
                        0.5,
                        1.0,
                        512
                );

                setLoading(true);
                recommendationTextView.setVisibility(View.GONE);
                btnSendEmailNutrition.setEnabled(false);

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
                                recommendationTextView.setText(content);
                                recommendationTextView.setVisibility(View.VISIBLE);
                                btnSendEmailNutrition.setEnabled(true);
                                return;
                            }
                        }
                        Toast.makeText(requireContext(), "No se pudieron obtener recomendaciones. Verifica tu conexión.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatCompletionResponse> call, @NonNull Throwable t) {
                        setLoading(false);
                        if (!isAdded()) return;
                        Log.e(TAG, "Error de red: " + t.getMessage(), t);
                        Toast.makeText(requireContext(), "No se pudieron obtener recomendaciones. Verifica tu conexión.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error al preparar la solicitud", e);
                Toast.makeText(requireContext(), "No se pudieron obtener recomendaciones. Verifica tu conexión.", Toast.LENGTH_SHORT).show();
            }
        });

        btnSendEmailNutrition.setOnClickListener(v -> {
            try {
                SessionManager sm = new SessionManager(requireContext());
                String email = sm.getCurrentEmail();
                String content = recommendationTextView.getText() != null ? recommendationTextView.getText().toString().trim() : "";
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(requireContext(), "Inicia sesión para enviar por correo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(requireContext(), "No hay recomendaciones para enviar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnSendEmailNutrition.setEnabled(false);
                sendingProgressNutrition.setVisibility(View.VISIBLE);

                N8nEmailSender.sendEmail(email, "FitHub360 - Nutrición", content, (success, message) -> {
                    if (!isAdded()) return;
                    sendingProgressNutrition.setVisibility(View.GONE);
                    btnSendEmailNutrition.setEnabled(true);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                sendingProgressNutrition.setVisibility(View.GONE);
                btnSendEmailNutrition.setEnabled(true);
                Toast.makeText(requireContext(), "Error al enviar. Inténtalo más tarde.", Toast.LENGTH_SHORT).show();
            }
        });
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
        btnCalculate.setEnabled(!loading);
    }

    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private double parseDoubleSafe(String s) {
        try { return Double.parseDouble(s.replace(",", ".")); } catch (Exception e) { return -1; }
    }

    private String classifyImc(double imc) {
        if (imc < 18.5) return "bajo peso";
        if (imc < 25.0) return "peso normal";
        if (imc < 30.0) return "sobrepeso";
        return "obesidad";
    }
}
