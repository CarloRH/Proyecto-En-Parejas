package com.example.fithub360.network;

import com.example.fithub360.models.ChatCompletionRequest;
import com.example.fithub360.models.ChatCompletionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NvidiaApiService {
    @POST("chat/completions")
    Call<ChatCompletionResponse> createCompletion(@Body ChatCompletionRequest request);
}

