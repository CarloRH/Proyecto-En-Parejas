package com.example.fithub360;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class FitHub360Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Forzar modo oscuro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
