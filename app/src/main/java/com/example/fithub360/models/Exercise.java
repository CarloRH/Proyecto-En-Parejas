package com.example.fithub360.models;

public class Exercise {
    private int id; // para WGER
    private String name;
    private String description;
    private String imageUrl; // opcional

    public Exercise(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Exercise(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Exercise(int id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
