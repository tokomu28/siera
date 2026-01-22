package com.lab.siera;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class DataManager {
    private static DataManager instance;
    private DatabaseHelper dbHelper;
    private Context context;

    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        dbHelper = new DatabaseHelper(this.context);
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // ========== METHODS UNTUK USERS ==========
    public boolean registerUser(String nama, String email, String npm, String password) {
        Log.d("DATAMANAGER", "Registering user: " + email);

        if (dbHelper.isEmailExists(email)) {
            Log.d("DATAMANAGER", "Email already exists: " + email);
            return false;
        }

        if (dbHelper.isNpmExists(npm)) {
            Log.d("DATAMANAGER", "NPM already exists: " + npm);
            return false;
        }

        boolean result = dbHelper.registerUser(nama, email, npm, password);
        Log.d("DATAMANAGER", "Register result: " + result);
        return result;
    }

    public DatabaseHelper.LoginResult loginUser(String identifier, String password) {
        Log.d("DATAMANAGER", "Login attempt: " + identifier);

        if (identifier == null || identifier.isEmpty() || password == null || password.isEmpty()) {
            Log.d("DATAMANAGER", "Empty credentials");
            return new DatabaseHelper.LoginResult(false, null, 0, null, null, null);
        }

        DatabaseHelper.LoginResult result = dbHelper.loginUser(identifier, password);
        Log.d("DATAMANAGER", "Login result: " + result.isSuccess() + ", Type: " + result.getUserType());
        return result;
    }

    public boolean createAdminUser(String nama, String email, String npm, String password) {
        if (dbHelper.isEmailExists(email)) {
            return false;
        }

        if (dbHelper.isNpmExists(npm)) {
            return false;
        }

        return dbHelper.createAdminUser(nama, email, npm, password);
    }

    // ========== METHODS UNTUK KEGIATAN ==========
    public boolean tambahKegiatan(String nama, String jenis, String penyelenggara, String tanggal, long timestamp) {
        return dbHelper.tambahKegiatan(nama, jenis, penyelenggara, tanggal, timestamp);
    }

    public Cursor getAllKegiatan() {
        return dbHelper.getAllKegiatan();
    }

    public Cursor getKegiatanById(int id) {
        return dbHelper.getKegiatanById(id);
    }

    public boolean updateKegiatan(int id, String nama, String jenis, String penyelenggara, String tanggal, long timestamp) {
        return dbHelper.updateKegiatan(id, nama, jenis, penyelenggara, tanggal, timestamp);
    }

    public boolean deleteKegiatan(int id) {
        return dbHelper.deleteKegiatan(id);
    }

    public Cursor searchKegiatan(String keyword) {
        return dbHelper.searchKegiatan(keyword);
    }

    // ========== METHOD DEBUG ==========
    public String debugAllUsers() {
        return dbHelper.getAllUsersAsString();
    }
}