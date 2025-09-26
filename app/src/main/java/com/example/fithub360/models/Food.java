package com.example.fithub360.models;

public class Food {
    private String name;
    private String description;

    public Food(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
