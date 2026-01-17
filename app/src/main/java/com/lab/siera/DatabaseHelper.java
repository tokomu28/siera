package com.lab.siera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "siera.db";
    private static final int DATABASE_VERSION = 2; // Update version

    // Tabel users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMA = "nama";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NPM = "npm";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_TYPE = "user_type"; // "mahasiswa" atau "admin"

    // Query untuk membuat tabel (update dengan user_type)
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAMA + " TEXT NOT NULL," +
                    COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
                    COLUMN_NPM + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD + " TEXT NOT NULL," +
                    COLUMN_USER_TYPE + " TEXT DEFAULT 'mahasiswa'" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);

        // Insert admin default (opsional)
        insertDefaultAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add user_type column if upgrading from version 1
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_TYPE + " TEXT DEFAULT 'mahasiswa'");

            // Set default admin
            updateExistingUsersToMahasiswa(db);
            insertDefaultAdmin(db);
        }
    }

    private void insertDefaultAdmin(SQLiteDatabase db) {
        // Insert default admin account
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, "Admin SIERA");
        values.put(COLUMN_EMAIL, "admin@siera.com");
        values.put(COLUMN_NPM, "00000000");
        values.put(COLUMN_PASSWORD, "admin123");
        values.put(COLUMN_USER_TYPE, "admin");

        try {
            db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            // Admin already exists
        }
    }

    private void updateExistingUsersToMahasiswa(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_TYPE, "mahasiswa");
        db.update(TABLE_USERS, values, null, null);
    }

    // Method untuk registrasi user baru (mahasiswa)
    public boolean registerUser(String nama, String email, String npm, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, nama);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NPM, npm);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_USER_TYPE, "mahasiswa"); // Default untuk registrasi

        try {
            long result = db.insert(TABLE_USERS, null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            db.close();
            return false;
        }
    }

    // Method untuk login dengan email ATAU npm
    public LoginResult loginUser(String identifier, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query untuk mencari user dengan email ATAU npm
        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE (" + COLUMN_EMAIL + " = ? OR " + COLUMN_NPM + " = ?) " +
                " AND " + COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{identifier, identifier, password});

        if (cursor.moveToFirst()) {
            // Get user type from database
            int userTypeIndex = cursor.getColumnIndex(COLUMN_USER_TYPE);
            String userType = cursor.getString(userTypeIndex);

            // Get user data
            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int namaIndex = cursor.getColumnIndex(COLUMN_NAMA);
            int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int npmIndex = cursor.getColumnIndex(COLUMN_NPM);

            int userId = cursor.getInt(idIndex);
            String nama = cursor.getString(namaIndex);
            String email = cursor.getString(emailIndex);
            String npm = cursor.getString(npmIndex);

            cursor.close();
            db.close();

            return new LoginResult(true, userType, userId, nama, email, npm);
        }

        cursor.close();
        db.close();
        return new LoginResult(false, null, 0, null, null, null);
    }

    // Class untuk menyimpan hasil login
    public static class LoginResult {
        private boolean success;
        private String userType;
        private int userId;
        private String nama;
        private String email;
        private String npm;

        public LoginResult(boolean success, String userType, int userId, String nama, String email, String npm) {
            this.success = success;
            this.userType = userType;
            this.userId = userId;
            this.nama = nama;
            this.email = email;
            this.npm = npm;
        }

        public boolean isSuccess() { return success; }
        public String getUserType() { return userType; }
        public int getUserId() { return userId; }
        public String getNama() { return nama; }
        public String getEmail() { return email; }
        public String getNpm() { return npm; }
    }

    // Method untuk mengecek apakah email sudah terdaftar
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Method untuk mengecek apakah npm sudah terdaftar
    public boolean isNpmExists(String npm) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_NPM + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{npm});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Method untuk membuat user admin (bisa digunakan untuk fitur admin management)
    public boolean createAdminUser(String nama, String email, String npm, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, nama);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NPM, npm);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_USER_TYPE, "admin");

        try {
            long result = db.insert(TABLE_USERS, null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            db.close();
            return false;
        }
    }
}