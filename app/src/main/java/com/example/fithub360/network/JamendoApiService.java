package com.example.fithub360.network;

import com.example.fithub360.models.JamendoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface JamendoApiService {
    @GET("tracks")
    Call<JamendoResponse> getTracks(
        @Query("client_id") String clientId,
        @Query("format") String format,
        @Query("limit") int limit
    );
}
