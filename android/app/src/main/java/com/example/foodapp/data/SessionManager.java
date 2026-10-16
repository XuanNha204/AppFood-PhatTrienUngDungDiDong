package com.example.foodapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodapp.model.User;
import com.google.gson.Gson;

/** Lưu JWT token + thông tin user đăng nhập vào SharedPreferences. */
public final class SessionManager {
    private static final String PREFS = "food_app_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";
    private static final String KEY_DARK_MODE = "dark_mode";

    private static SessionManager instance;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private User cachedUser;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (instance == null) instance = new SessionManager(context);
    }

    public static SessionManager get() {
        return instance;
    }

    public void saveSession(String token, User user) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        saveUser(user);
    }

    public void saveUser(User user) {
        cachedUser = user;
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply();
        AppState.setFavorites(user.getFavorites());
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public User getUser() {
        if (cachedUser == null) {
            String json = prefs.getString(KEY_USER, null);
            if (json != null) cachedUser = gson.fromJson(json, User.class);
        }
        return cachedUser;
    }

    public String getUserId() {
        User user = getUser();
        return user == null ? null : user.getId();
    }

    public boolean isLoggedIn() {
        return getToken() != null && getUser() != null;
    }

    public void clear() {
        cachedUser = null;
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }
}
