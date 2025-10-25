package com.example.fithub360.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.example.fithub360.network.N8nApiService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class N8nEmailSender {

    private static final String TAG = "N8nEmailSender";

    // Base de Railway (el path exacto se define en la interfaz con webhook-test)
    private static final String N8N_BASE_URL = "https://primary-production-2b90.up.railway.app/";

    private static Retrofit retrofit;

    public interface ResultCallback {
        void onResult(boolean success, String message);
    }

    private static final ResultCallback NOOP_CALLBACK = (success, message) -> { /* no-op */ };

    private static N8nApiService getService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(N8N_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit.create(N8nApiService.class);
    }

    // Post the result on the main thread to ensure UI can be updated safely
    private static void postResult(ResultCallback cb, final boolean success, final String message) {
        ResultCallback callback = (cb != null) ? cb : NOOP_CALLBACK;
        new Handler(Looper.getMainLooper()).post(() -> callback.onResult(success, message));
    }

    public static void sendEmail(String email, String subject, String content, ResultCallback cb) {
        final ResultCallback callback = (cb != null) ? cb : NOOP_CALLBACK;
        try {
            if (TextUtils.isEmpty(email)) {
                postResult(callback, false, "Correo no disponible en la sesión");
                return;
            }
            if (TextUtils.isEmpty(content)) {
                postResult(callback, false, "No hay contenido para enviar");
                return;
            }

            final N8nApiService service = getService();
            final N8nApiService.EmailPayload payload = new N8nApiService.EmailPayload(email, subject, content);

            // Intentamos primero el endpoint de test (sin secret => null)
            Call<ResponseBody> call = service.sendEmailTest(null, payload);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "webhook-test HTTP " + response.code());
                    if (response.isSuccessful()) {
                        postResult(callback, true, "Correo enviado correctamente.");
                    } else {
                        String err = "HTTP " + response.code();
                        try (ResponseBody eb = response.errorBody()) {
                            if (eb != null) {
                                String ebStr = eb.string();
                                err += " - " + ebStr;
                                Log.e(TAG, "webhook-test error body: " + ebStr);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error leyendo errorBody: " + e.getMessage(), e);
                        }
                        // Intentar fallback al endpoint prod (sin secret => null)
                        Log.w(TAG, "Intentando fallback al endpoint prod");
                        tryFallbackProd(service, payload, callback, err);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Fallo en webhook-test: " + t.getMessage(), t);
                    // Intentar fallback al endpoint prod (sin secret)
                    tryFallbackProd(service, payload, callback, "Fallo: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Excepción al enviar: " + e.getMessage(), e);
            postResult(callback, false, "Error al enviar. Inténtalo más tarde.");
        }
    }

    private static void tryFallbackProd(N8nApiService service, N8nApiService.EmailPayload payload, ResultCallback callback, String previousError) {
        try {
            Call<ResponseBody> prodCall = service.sendEmailProd(null, payload);
            prodCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "webhook-prod HTTP " + response.code());
                    if (response.isSuccessful()) {
                        postResult(callback, true, "Correo enviado correctamente (fallback)." );
                    } else {
                        String err = previousError + " | prod HTTP " + response.code();
                        try (ResponseBody eb = response.errorBody()) {
                            if (eb != null) {
                                String ebStr = eb.string();
                                err += " - " + ebStr;
                                Log.e(TAG, "webhook-prod error body: " + ebStr);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error leyendo errorBody prod: " + e.getMessage(), e);
                        }
                        postResult(callback, false, "Error al enviar. Inténtalo más tarde. (" + err + ")");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Fallo en webhook-prod: " + t.getMessage(), t);
                    postResult(callback, false, "Error al enviar. Inténtalo más tarde. (" + previousError + " | " + t.getMessage() + ")");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Excepción en fallback prod: " + e.getMessage(), e);
            postResult(callback, false, "Error al enviar. Inténtalo más tarde. (" + previousError + ")");
        }
    }
}
