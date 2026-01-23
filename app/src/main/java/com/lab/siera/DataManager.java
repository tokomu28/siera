package com.lab.siera;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    public boolean tambahKegiatan(String nama, String jenis, String penyelenggara,
                                  String deskripsi, String tanggal, String waktu,
                                  String lokasi, long timestamp, String fotoBase64) {
        return dbHelper.tambahKegiatan(nama, jenis, penyelenggara, deskripsi,
                tanggal, waktu, lokasi, timestamp, fotoBase64);
    }

    public boolean updateKegiatan(int id, String nama, String jenis, String penyelenggara,
                                  String deskripsi, String tanggal, String waktu,
                                  String lokasi, long timestamp, String fotoBase64) {
        return dbHelper.updateKegiatan(id, nama, jenis, penyelenggara, deskripsi,
                tanggal, waktu, lokasi, timestamp, fotoBase64);
    }

    public Cursor getAllKegiatan() {
        return dbHelper.getAllKegiatan();
    }

    public Cursor getKegiatanById(int id) {
        return dbHelper.getKegiatanById(id);
    }

    public boolean deleteKegiatan(int id) {
        return dbHelper.deleteKegiatan(id);
    }

    public Cursor searchKegiatan(String keyword) {
        return dbHelper.searchKegiatan(keyword);
    }

    public Cursor getLatestKegiatan(int limit) {
        return dbHelper.getLatestKegiatan(limit);
    }

    // ========== METHODS UNTUK PENDAFTARAN (BARU) ==========
    public boolean daftarKegiatan(int userId, int kegiatanId, String tanggal) {
        return dbHelper.daftarKegiatan(userId, kegiatanId, tanggal);
    }

    public boolean updateStatusPendaftaran(int pendaftaranId, String status) {
        return dbHelper.updateStatusPendaftaran(pendaftaranId, status);
    }

    public Cursor getAllPendaftaran() {
        return dbHelper.getAllPendaftaran();
    }

    public Cursor getPendaftaranByStatus(String status) {
        return dbHelper.getPendaftaranByStatus(status);
    }

    public Cursor getPendaftaranByUserId(int userId) {
        return dbHelper.getPendaftaranByUserId(userId);
    }

    public boolean isUserRegisteredForKegiatan(int userId, int kegiatanId) {
        return dbHelper.isUserRegisteredForKegiatan(userId, kegiatanId);
    }

    // Method untuk mendapatkan statistik pendaftaran
    public int getJumlahPendaftaranPending() {
        return dbHelper.getJumlahPendaftaranByStatus("pending");
    }

    public int getJumlahPendaftaranApproved() {
        return dbHelper.getJumlahPendaftaranByStatus("approved");
    }

    public int getJumlahPendaftaranRejected() {
        return dbHelper.getJumlahPendaftaranByStatus("rejected");
    }

    public int getTotalPendaftaran() {
        return dbHelper.getTotalPendaftaran();
    }

    // Method untuk mendapatkan tanggal sekarang
    public static String getCurrentDate() {
        return DatabaseHelper.getCurrentDate();
    }

    // ========== HELPER METHODS UNTUK FOTO ==========
    public static String bitmapToBase64(Bitmap bitmap) {
        return DatabaseHelper.bitmapToBase64(bitmap);
    }

    public static Bitmap base64ToBitmap(String base64String) {
        return DatabaseHelper.base64ToBitmap(base64String);
    }

    // ========== METHOD DEBUG ==========
    public String debugAllUsers() {
        return dbHelper.getAllUsersAsString();
    }

    public String debugAllPendaftaran() {
        return dbHelper.getAllPendaftaranAsString();
    }

    // ========== METHOD UNTUK VALIDASI ==========
    public boolean isEmailValid(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isNpmValid(String npm) {
        return npm != null && npm.length() >= 8 && npm.matches("\\d+");
    }

    public boolean isPasswordValid(String password) {
        return password != null && password.length() >= 6;
    }
}