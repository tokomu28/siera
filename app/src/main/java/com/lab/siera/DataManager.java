package com.lab.siera;

import android.content.Context;

public class DataManager {
    private static DataManager instance;
    private DatabaseHelper dbHelper;

    private DataManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // Method untuk registrasi (mahasiswa)
    public boolean registerUser(String nama, String email, String npm, String password) {
        // Cek apakah email sudah terdaftar
        if (dbHelper.isEmailExists(email)) {
            return false;
        }

        // Cek apakah npm sudah terdaftar
        if (dbHelper.isNpmExists(npm)) {
            return false;
        }

        // Registrasi user baru (mahasiswa)
        return dbHelper.registerUser(nama, email, npm, password);
    }

    // Method untuk login dengan email atau npm
    public DatabaseHelper.LoginResult loginUser(String identifier, String password) {
        return dbHelper.loginUser(identifier, password);
    }

    // Method untuk membuat admin (opsional, untuk admin management)
    public boolean createAdminUser(String nama, String email, String npm, String password) {
        // Cek apakah email sudah terdaftar
        if (dbHelper.isEmailExists(email)) {
            return false;
        }

        // Cek apakah npm sudah terdaftar
        if (dbHelper.isNpmExists(npm)) {
            return false;
        }

        // Buat user admin
        return dbHelper.createAdminUser(nama, email, npm, password);
    }
}