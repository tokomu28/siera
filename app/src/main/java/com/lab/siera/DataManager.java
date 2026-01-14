package com.lab.siera;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class DataManager {
    private static DataManager instance;
    private ArrayList<User> users = new ArrayList<>();
    private static final String PREF_NAME = "UserData";
    private static final String KEY_USERS = "users_list";
    private Context context;
    private Gson gson = new Gson();

    private DataManager(Context context) {
        this.context = context;
        loadUsersFromPrefs();

        // Jika tidak ada data, tambahkan admin default
        if (users.isEmpty()) {
            users.add(new User("Admin", "admin@siera.com", "12345678", "admin123"));
            saveUsersToPrefs();
        }
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // Register user baru
    public boolean registerUser(String nama, String email, String npm, String password) {
        // Cek apakah email sudah terdaftar
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return false; // Email sudah terdaftar
            }
        }

        users.add(new User(nama, email, npm, password));
        saveUsersToPrefs(); // Simpan ke SharedPreferences
        return true;
    }

    // Validasi login
    public boolean validateLogin(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) &&
                    user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    // Get user by email
    public User getUserByEmail(String email) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    // Simpan data ke SharedPreferences
    private void saveUsersToPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = gson.toJson(users);
        editor.putString(KEY_USERS, json);
        editor.apply();
    }

    // Load data dari SharedPreferences
    private void loadUsersFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USERS, "");

        if (!json.isEmpty()) {
            Type type = new TypeToken<ArrayList<User>>() {}.getType();
            users = gson.fromJson(json, type);
        }

        if (users == null) {
            users = new ArrayList<>();
        }
    }

    // Hapus semua data (untuk testing/debug)
    public void clearAllData() {
        users.clear();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    // Model User
    public static class User {
        private String nama;
        private String email;
        private String npm;
        private String password;

        public User(String nama, String email, String npm, String password) {
            this.nama = nama;
            this.email = email;
            this.npm = npm;
            this.password = password;
        }

        public String getNama() { return nama; }
        public String getEmail() { return email; }
        public String getNpm() { return npm; }
        public String getPassword() { return password; }
    }
}