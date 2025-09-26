package com.example.fithub360.models;

import java.util.List;

// Modelo para la respuesta de WGER en /exerciseimage/
// Solo consume datos; no realiza escrituras.
public class ExerciseImageResponse {
    private int count;
    private List<ExerciseImage> results;

    public int getCount() {
        return count;
    }

    public List<ExerciseImage> getResults() {
        return results;
    }

    public static class ExerciseImage {
        public int id;
        public int exercise;
        public String image; // URL de la imagen; limpiar con .trim() al usarla
        public boolean is_main;
    }
}

