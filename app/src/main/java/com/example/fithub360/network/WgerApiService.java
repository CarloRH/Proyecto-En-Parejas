package com.example.fithub360.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WgerApiService {
    @GET("api/v2/exercise/")
    Call<com.example.fithub360.models.ExerciseResponse> getExercises(
            @Query("language") int language,
            @Query("muscles") int muscleId,
            @Query("limit") int limit
    );

    @GET("api/v2/exerciseimage/")
    Call<com.example.fithub360.models.ExerciseImageResponse> getExerciseImages(
            @Query("exercise") int exerciseId,
            @Query("is_main") Boolean isMain
    );
}
