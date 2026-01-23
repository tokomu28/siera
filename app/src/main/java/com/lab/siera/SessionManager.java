package com.lab.siera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_TYPE = "userType"; // TAMBAHKAN
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email"; // TAMBAHKAN
    private static final String KEY_NAMA = "nama"; // TAMBAHKAN
    private static final String KEY_NPM = "npm";
    private static final String KEY_PROGRAM_STUDY = "programStudy";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private static final int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    // MARK: - CREATE/UPDATE SESSION (6 parameter)
    public void createLoginSession(int userId, String userType, String nama,
                                   String email, String npm, String programStudy) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_TYPE, userType); // SIMPAN userType
        editor.putString(KEY_NAMA, nama); // SIMPAN nama
        editor.putString(KEY_USERNAME, nama); // Simpan juga sebagai username
        editor.putString(KEY_EMAIL, email); // SIMPAN email
        editor.putString(KEY_NPM, npm);
        editor.putString(KEY_PROGRAM_STUDY, programStudy);
        editor.commit();
    }

    // MARK: - OVERLOAD METHOD (4 parameter - untuk backward compatibility)
    public void createLoginSession(int userId, String username, String npm, String programStudy) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NAMA, username); // Simpan juga sebagai nama
        editor.putString(KEY_NPM, npm);
        editor.putString(KEY_PROGRAM_STUDY, programStudy);
        editor.putString(KEY_USER_TYPE, "mahasiswa"); // Default sebagai mahasiswa
        editor.commit();
    }

    // MARK: - CHECK LOGIN STATUS
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // MARK: - GET USER DATA
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUserType() { // TAMBAHKAN method ini
        return pref.getString(KEY_USER_TYPE, "mahasiswa");
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getNama() { // TAMBAHKAN method ini
        return pref.getString(KEY_NAMA, getUsername());
    }

    public String getEmail() { // TAMBAHKAN method ini
        return pref.getString(KEY_EMAIL, null);
    }

    public String getNpm() {
        return pref.getString(KEY_NPM, null);
    }

    public String getProgramStudy() {
        return pref.getString(KEY_PROGRAM_STUDY, null);
    }

    // MARK: - LOGOUT USER
    public void logoutUser() {
        // Clear all data from SharedPreferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent intent = new Intent(context, MainActivity.class);

        // Closing all the Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    // MARK: - CLEAR SPECIFIC DATA
    public void clearSessionData() {
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_TYPE);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_NAMA);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_NPM);
        editor.remove(KEY_PROGRAM_STUDY);
        editor.commit();
    }
}