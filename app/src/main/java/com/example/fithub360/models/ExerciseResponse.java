package com.example.fithub360.models;

import java.util.List;

public class ExerciseResponse {
    private int count;
    private String next;
    private String previous;
    private List<ExerciseItem> results;

    public List<ExerciseItem> getResults() {
        return results;
    }

    public static class ExerciseItem {
        public int id;
        public String name;
        public String description;
    }
}

