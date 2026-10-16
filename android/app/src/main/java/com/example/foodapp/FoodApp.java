package com.example.foodapp;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.foodapp.data.SessionManager;

public class FoodApp extends Application {
    private static FoodApp instance;

    public static FoodApp get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SessionManager.init(this);
        AppCompatDelegate.setDefaultNightMode(SessionManager.get().isDarkMode()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
