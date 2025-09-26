package com.example.fithub360.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.fithub360.models.JamendoTrack;
import java.io.IOException;
import java.util.List;

public class MusicService extends Service {
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private List<JamendoTrack> trackList;
    private int currentIndex = -1;
    private OnTrackChangeListener trackChangeListener;

    public interface OnTrackChangeListener {
        void onTrackChanged(JamendoTrack track, boolean isPlaying);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setTrackList(List<JamendoTrack> tracks) {
        this.trackList = tracks;
    }

    public void setOnTrackChangeListener(OnTrackChangeListener listener) {
        this.trackChangeListener = listener;
    }

    public void playTrack(int index) {
        if (trackList == null || index < 0 || index >= trackList.size()) return;
        currentIndex = index;
        JamendoTrack track = trackList.get(index);
        releasePlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(track.audio);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                if (trackChangeListener != null) trackChangeListener.onTrackChanged(track, true);
            });
            mediaPlayer.setOnCompletionListener(mp -> playNext());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                if (trackChangeListener != null) trackChangeListener.onTrackChanged(track, false);
                return true;
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            if (trackChangeListener != null) trackChangeListener.onTrackChanged(track, false);
        }
    }

    public void pauseTrack() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (trackChangeListener != null && currentIndex >= 0 && trackList != null)
                trackChangeListener.onTrackChanged(trackList.get(currentIndex), false);
        }
    }

    public void resumeTrack() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (trackChangeListener != null && currentIndex >= 0 && trackList != null)
                trackChangeListener.onTrackChanged(trackList.get(currentIndex), true);
        }
    }

    public void playNext() {
        if (trackList == null || trackList.isEmpty()) return;
        int nextIndex = (currentIndex + 1) % trackList.size();
        playTrack(nextIndex);
    }

    public void playPrevious() {
        if (trackList == null || trackList.isEmpty()) return;
        int prevIndex = (currentIndex - 1 + trackList.size()) % trackList.size();
        playTrack(prevIndex);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}

