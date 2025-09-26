package com.example.fithub360.models;

public class Track {
    private String id;
    private String title;
    private String artist;
    private String streamUrl;
    private boolean isPlaying;

    public Track(String id, String title, String artist, String streamUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.streamUrl = streamUrl;
        this.isPlaying = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
