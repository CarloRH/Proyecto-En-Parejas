package com.example.fithub360.models;

public class Achievement {
    private String id;
    private String name;
    private String description;
    private String category;
    private int requiredCount;
    private boolean unlocked;
    private int currentProgress;

    public Achievement(String id, String name, String description, String category, int requiredCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.requiredCount = requiredCount;
        this.unlocked = false;
        this.currentProgress = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
        this.unlocked = currentProgress >= requiredCount;
    }
}
