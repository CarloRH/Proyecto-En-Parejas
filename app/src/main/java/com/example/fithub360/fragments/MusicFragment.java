package com.example.fithub360.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
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
import com.example.fithub360.adapters.TrackAdapter;
import com.example.fithub360.models.JamendoResponse;
import com.example.fithub360.models.JamendoTrack;
import com.example.fithub360.network.JamendoApiService;
import com.example.fithub360.services.MusicService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MusicFragment extends Fragment implements TrackAdapter.OnTrackPlayPauseListener, TrackAdapter.OnTrackFavoriteListener {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TrackAdapter adapter;
    private MediaPlayer mediaPlayer;
    private JamendoTrack currentTrack;
    private MusicService musicService;
    private boolean serviceBound = false;
    private List<JamendoTrack> trackList = new ArrayList<>();
    private Set<Integer> favoriteIds = new HashSet<>();
    private SharedPreferences prefs;
    private static final String PREFS_FAVORITES = "music_favorites";

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) binder;
            musicService = musicBinder.getService();
            serviceBound = true;
            musicService.setOnTrackChangeListener((track, isPlaying) -> {
                adapter.setPlayingTrack(track.id, isPlaying);
            });
            musicService.setTrackList(trackList);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.tracksRecyclerView);
        progressBar = view.findViewById(R.id.musicProgressBar);
        prefs = requireContext().getSharedPreferences(PREFS_FAVORITES, Context.MODE_PRIVATE);
        favoriteIds = prefs.getStringSet("ids", new HashSet<>()).stream().map(Integer::parseInt).collect(java.util.stream.Collectors.toSet());
        adapter = new TrackAdapter(new ArrayList<>(), this, this);
        adapter.setFavoriteIds(favoriteIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        Intent intent = new Intent(requireContext(), MusicService.class);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        loadTracks();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceBound) requireActivity().unbindService(connection);
    }

    @Override
    public void onTrackPlayPause(JamendoTrack track, boolean play) {
        if (!serviceBound || musicService == null) return;
        int index = -1;
        for (int i = 0; i < trackList.size(); i++) if (trackList.get(i).id == track.id) index = i;
        if (index == -1) return;
        if (play) musicService.playTrack(index);
        else musicService.pauseTrack();
    }

    @Override
    public void onTrackFavorite(JamendoTrack track, boolean isFavorite) {
        if (isFavorite) favoriteIds.add(track.id);
        else favoriteIds.remove(track.id);
        adapter.setFavoriteIds(favoriteIds);
        // Guardar favoritos
        Set<String> idsStr = new HashSet<>();
        for (Integer id : favoriteIds) idsStr.add(String.valueOf(id));
        prefs.edit().putStringSet("ids", idsStr).apply();
    }

    private void loadTracks() {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.jamendo.com/v3.0/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JamendoApiService api = retrofit.create(JamendoApiService.class);
        api.getTracks(
                "ac4bfbad",
                "json",
                20
        ).enqueue(new Callback<JamendoResponse>() {
            @Override
            public void onResponse(Call<JamendoResponse> call, Response<JamendoResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<JamendoTrack> tracks = response.body().results;
                    if (tracks != null && !tracks.isEmpty()) {
                        trackList = tracks;
                        adapter.setTracks(trackList);
                        adapter.setFavoriteIds(favoriteIds);
                        if (serviceBound && musicService != null) musicService.setTrackList(trackList);
                    } else {
                        Toast.makeText(getContext(), "No se encontraron canciones. Código: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al cargar canciones. Código: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<JamendoResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Sin conexión a internet o error de red.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
