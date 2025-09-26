package com.example.fithub360.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fithub360.R;
import com.example.fithub360.models.JamendoTrack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    public interface OnTrackPlayPauseListener {
        void onTrackPlayPause(JamendoTrack track, boolean play);
    }
    public interface OnTrackFavoriteListener {
        void onTrackFavorite(JamendoTrack track, boolean isFavorite);
    }
    private List<JamendoTrack> tracks;
    private final OnTrackPlayPauseListener playPauseListener;
    private final OnTrackFavoriteListener favoriteListener;
    private Set<Integer> favoriteIds = new HashSet<>();
    private int playingTrackId = -1;
    private boolean isPlaying = false;

    public TrackAdapter(List<JamendoTrack> tracks, OnTrackPlayPauseListener playPauseListener, OnTrackFavoriteListener favoriteListener) {
        this.tracks = new ArrayList<>(tracks);
        this.playPauseListener = playPauseListener;
        this.favoriteListener = favoriteListener;
    }

    public void setTracks(List<JamendoTrack> tracks) {
        this.tracks = new ArrayList<>(tracks);
        sortFavoritesFirst();
        notifyDataSetChanged();
    }

    public void setFavoriteIds(Set<Integer> ids) {
        this.favoriteIds = ids;
        sortFavoritesFirst();
        notifyDataSetChanged();
    }

    public void setPlayingTrack(int trackId, boolean playing) {
        this.playingTrackId = trackId;
        this.isPlaying = playing;
        notifyDataSetChanged();
    }

    private void sortFavoritesFirst() {
        List<JamendoTrack> favs = new ArrayList<>();
        List<JamendoTrack> others = new ArrayList<>();
        for (JamendoTrack t : tracks) {
            if (favoriteIds.contains(t.id)) favs.add(t);
            else others.add(t);
        }
        favs.addAll(others);
        tracks = favs;
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        JamendoTrack track = tracks.get(position);
        holder.trackName.setText(track.name);
        holder.trackArtist.setText(track.artist_name);
        // Favorito
        holder.favoriteButton.setColorFilter(favoriteIds.contains(track.id) ? Color.YELLOW : Color.GRAY);
        holder.favoriteButton.setOnClickListener(v -> {
            boolean isFav = !favoriteIds.contains(track.id);
            favoriteListener.onTrackFavorite(track, isFav);
        });
        // Play/Pause
        if (track.id == playingTrackId && isPlaying) {
            holder.playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            holder.playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
        holder.playPauseButton.setOnClickListener(v -> {
            boolean play = !(track.id == playingTrackId && isPlaying);
            playPauseListener.onTrackPlayPause(track, play);
        });
    }

    @Override
    public int getItemCount() {
        return tracks != null ? tracks.size() : 0;
    }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView trackName, trackArtist;
        ImageButton playPauseButton, favoriteButton;
        TrackViewHolder(View itemView) {
            super(itemView);
            trackName = itemView.findViewById(R.id.trackNameTextView);
            trackArtist = itemView.findViewById(R.id.trackArtistTextView);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
        }
    }
}
