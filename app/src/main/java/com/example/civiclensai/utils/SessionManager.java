package com.example.civiclensai.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "CivicLensUserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_KARMA = "karma";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String uid, String name, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putInt(KEY_KARMA, 150);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUid() {
        return pref.getString(KEY_UID, "usr_1001");
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, "Alex Citizen");
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "alex.citizen@example.com");
    }

    public int getKarmaPoints() {
        return pref.getInt(KEY_KARMA, 150);
    }

    public void updateUserProfile(String name, String email) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public void addKarmaPoints(int points) {
        int current = getKarmaPoints();
        editor.putInt(KEY_KARMA, current + points);
        editor.apply();
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
