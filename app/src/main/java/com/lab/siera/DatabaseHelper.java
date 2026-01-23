package com.lab.siera;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "siera.db";
    private static final int DATABASE_VERSION = 5; // Update ke versi 5

    // ========== TABEL USERS ==========
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMA = "nama";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NPM = "npm";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_TYPE = "user_type";

    // ========== TABEL KEGIATAN (DIPERBARUI) ==========
    public static final String TABLE_KEGIATAN = "kegiatan";
    public static final String COLUMN_KEGIATAN_ID = "kegiatan_id";
    public static final String COLUMN_KEGIATAN_NAMA = "nama_kegiatan";
    public static final String COLUMN_KEGIATAN_JENIS = "jenis";
    public static final String COLUMN_KEGIATAN_PENYELENGGARA = "penyelenggara";
    public static final String COLUMN_KEGIATAN_DESKRIPSI = "deskripsi";
    public static final String COLUMN_KEGIATAN_TANGGAL = "tanggal";
    public static final String COLUMN_KEGIATAN_WAKTU = "waktu";
    public static final String COLUMN_KEGIATAN_LOKASI = "lokasi";
    public static final String COLUMN_KEGIATAN_TANGGAL_TIMESTAMP = "tanggal_timestamp";
    public static final String COLUMN_KEGIATAN_FOTO = "foto"; // Kolom baru untuk foto

    // Query untuk membuat tabel users
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAMA + " TEXT NOT NULL," +
                    COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
                    COLUMN_NPM + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD + " TEXT NOT NULL," +
                    COLUMN_USER_TYPE + " TEXT DEFAULT 'mahasiswa'" +
                    ")";

    // Query untuk membuat tabel kegiatan (diperbarui)
    private static final String CREATE_TABLE_KEGIATAN =
            "CREATE TABLE " + TABLE_KEGIATAN + "(" +
                    COLUMN_KEGIATAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_KEGIATAN_NAMA + " TEXT NOT NULL," +
                    COLUMN_KEGIATAN_JENIS + " TEXT NOT NULL," +
                    COLUMN_KEGIATAN_PENYELENGGARA + " TEXT NOT NULL," +
                    COLUMN_KEGIATAN_DESKRIPSI + " TEXT," +
                    COLUMN_KEGIATAN_TANGGAL + " TEXT NOT NULL," +
                    COLUMN_KEGIATAN_WAKTU + " TEXT," +
                    COLUMN_KEGIATAN_LOKASI + " TEXT," +
                    COLUMN_KEGIATAN_TANGGAL_TIMESTAMP + " INTEGER," +
                    COLUMN_KEGIATAN_FOTO + " TEXT" + // Foto disimpan sebagai Base64 string
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABASE", "Creating database tables...");

        // Buat kedua tabel
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_KEGIATAN);

        Log.d("DATABASE", "Tables created successfully");

        // Insert admin default
        insertDefaultAdmin(db);

        // Insert sample mahasiswa untuk testing
        insertSampleUsers(db);

        // Insert sample kegiatan
        insertSampleKegiatan(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DATABASE", "Upgrading database from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_USER_TYPE + " TEXT DEFAULT 'mahasiswa'");
            updateExistingUsersToMahasiswa(db);
        }

        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEGIATAN);
            db.execSQL(CREATE_TABLE_KEGIATAN);
            insertSampleKegiatan(db);
        }

        if (oldVersion < 4) {
            insertDefaultAdmin(db);
        }

        if (oldVersion < 5) {
            try {
                // Cek apakah kolom sudah ada
                Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_KEGIATAN + " LIMIT 1", null);
                String[] columnNames = cursor.getColumnNames();
                cursor.close();

                boolean hasDeskripsi = false;
                boolean hasWaktu = false;
                boolean hasLokasi = false;
                boolean hasFoto = false;

                for (String column : columnNames) {
                    if (column.equals(COLUMN_KEGIATAN_DESKRIPSI)) hasDeskripsi = true;
                    if (column.equals(COLUMN_KEGIATAN_WAKTU)) hasWaktu = true;
                    if (column.equals(COLUMN_KEGIATAN_LOKASI)) hasLokasi = true;
                    if (column.equals(COLUMN_KEGIATAN_FOTO)) hasFoto = true;
                }

                if (!hasDeskripsi) {
                    db.execSQL("ALTER TABLE " + TABLE_KEGIATAN + " ADD COLUMN " + COLUMN_KEGIATAN_DESKRIPSI + " TEXT");
                }
                if (!hasWaktu) {
                    db.execSQL("ALTER TABLE " + TABLE_KEGIATAN + " ADD COLUMN " + COLUMN_KEGIATAN_WAKTU + " TEXT");
                }
                if (!hasLokasi) {
                    db.execSQL("ALTER TABLE " + TABLE_KEGIATAN + " ADD COLUMN " + COLUMN_KEGIATAN_LOKASI + " TEXT");
                }
                if (!hasFoto) {
                    db.execSQL("ALTER TABLE " + TABLE_KEGIATAN + " ADD COLUMN " + COLUMN_KEGIATAN_FOTO + " TEXT");
                }

                Log.d("DATABASE", "Database upgraded to version 5");
            } catch (Exception e) {
                Log.e("DATABASE", "Error upgrading database: " + e.getMessage());
                // Jika gagal, drop dan buat ulang tabel
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEGIATAN);
                db.execSQL(CREATE_TABLE_KEGIATAN);
                insertSampleKegiatan(db);
            }
        }
    }

    private void insertDefaultAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, "Admin SIERA");
        values.put(COLUMN_EMAIL, "admin@siera.com");
        values.put(COLUMN_NPM, "00000000");
        values.put(COLUMN_PASSWORD, "admin123");
        values.put(COLUMN_USER_TYPE, "admin");

        try {
            String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ? OR " + COLUMN_NPM + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{"admin@siera.com", "00000000"});

            if (cursor.getCount() == 0) {
                long result = db.insert(TABLE_USERS, null, values);
                Log.d("DATABASE", "Admin default created: " + (result != -1 ? "Success" : "Failed"));
            } else {
                Log.d("DATABASE", "Admin already exists");
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DATABASE", "Error inserting admin: " + e.getMessage());
        }
    }

    private void insertSampleUsers(SQLiteDatabase db) {
        ContentValues mahasiswa1 = new ContentValues();
        mahasiswa1.put(COLUMN_NAMA, "Ahmad Eagle");
        mahasiswa1.put(COLUMN_EMAIL, "ahmad@student.uika-bogor.ac.id");
        mahasiswa1.put(COLUMN_NPM, "20231001");
        mahasiswa1.put(COLUMN_PASSWORD, "mahasiswa123");
        mahasiswa1.put(COLUMN_USER_TYPE, "mahasiswa");

        ContentValues mahasiswa2 = new ContentValues();
        mahasiswa2.put(COLUMN_NAMA, "Vivi Santoso");
        mahasiswa2.put(COLUMN_EMAIL, "vivi@student.uika-bogor.ac.id");
        mahasiswa2.put(COLUMN_NPM, "20231002");
        mahasiswa2.put(COLUMN_PASSWORD, "mahasiswa123");
        mahasiswa2.put(COLUMN_USER_TYPE, "mahasiswa");

        try {
            db.insert(TABLE_USERS, null, mahasiswa1);
            db.insert(TABLE_USERS, null, mahasiswa2);
            Log.d("DATABASE", "Sample users created");
        } catch (Exception e) {
            Log.e("DATABASE", "Error inserting sample users: " + e.getMessage());
        }
    }

    private void insertSampleKegiatan(SQLiteDatabase db) {
        // Insert sample kegiatan
        ContentValues kegiatan1 = new ContentValues();
        kegiatan1.put(COLUMN_KEGIATAN_NAMA, "Workshop Android Development");
        kegiatan1.put(COLUMN_KEGIATAN_JENIS, "Workshop");
        kegiatan1.put(COLUMN_KEGIATAN_PENYELENGGARA, "Fakultas Teknologi Informasi");
        kegiatan1.put(COLUMN_KEGIATAN_DESKRIPSI, "Workshop pengembangan aplikasi Android untuk pemula");
        kegiatan1.put(COLUMN_KEGIATAN_TANGGAL, "15 Jan 2025");
        kegiatan1.put(COLUMN_KEGIATAN_WAKTU, "14.00");
        kegiatan1.put(COLUMN_KEGIATAN_LOKASI, "Lab Komputer FTI");
        kegiatan1.put(COLUMN_KEGIATAN_TANGGAL_TIMESTAMP, System.currentTimeMillis());
        kegiatan1.put(COLUMN_KEGIATAN_FOTO, "");

        ContentValues kegiatan2 = new ContentValues();
        kegiatan2.put(COLUMN_KEGIATAN_NAMA, "Seminar Revolusi Industri 4.0");
        kegiatan2.put(COLUMN_KEGIATAN_JENIS, "Seminar");
        kegiatan2.put(COLUMN_KEGIATAN_PENYELENGGARA, "Fakultas Teknik");
        kegiatan2.put(COLUMN_KEGIATAN_DESKRIPSI, "Seminar tentang perkembangan industri 4.0");
        kegiatan2.put(COLUMN_KEGIATAN_TANGGAL, "25 Jan 2025");
        kegiatan2.put(COLUMN_KEGIATAN_WAKTU, "09.00");
        kegiatan2.put(COLUMN_KEGIATAN_LOKASI, "Aula Utama");
        kegiatan2.put(COLUMN_KEGIATAN_TANGGAL_TIMESTAMP, System.currentTimeMillis() + 86400000);
        kegiatan2.put(COLUMN_KEGIATAN_FOTO, "");

        try {
            db.insert(TABLE_KEGIATAN, null, kegiatan1);
            db.insert(TABLE_KEGIATAN, null, kegiatan2);
            Log.d("DATABASE", "Sample kegiatan created");
        } catch (Exception e) {
            Log.e("DATABASE", "Error inserting sample kegiatan: " + e.getMessage());
        }
    }

    private void updateExistingUsersToMahasiswa(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_TYPE, "mahasiswa");
        db.update(TABLE_USERS, values, null, null);
    }

    // ========== METHODS UNTUK USERS ==========
    public boolean registerUser(String nama, String email, String npm, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA, nama);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_NPM, npm);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_USER_TYPE, "mahasiswa");

        try {
            long result = db.insert(TABLE_USERS, null, values);
            db.close();
            Log.d("REGISTER", "User registered: " + (result != -1));
            return result != -1;
        } catch (Exception e) {
            Log.e("REGISTER", "Error: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public LoginResult loginUser(String identifier, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("LOGIN", "Attempting login for: " + identifier);

        String query = "SELECT * FROM " + TABLE_USERS +
                " WHERE (" + COLUMN_EMAIL + " = ? OR " + COLUMN_NPM + " = ?) " +
                " AND " + COLUMN_PASSWORD + " = ?";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{identifier, identifier, password});

            if (cursor != null && cursor.moveToFirst()) {
                int userTypeIndex = cursor.getColumnIndex(COLUMN_USER_TYPE);
                int idIndex = cursor.getColumnIndex(COLUMN_ID);
                int namaIndex = cursor.getColumnIndex(COLUMN_NAMA);
                int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
                int npmIndex = cursor.getColumnIndex(COLUMN_NPM);

                if (userTypeIndex == -1 || idIndex == -1 || namaIndex == -1 ||
                        emailIndex == -1 || npmIndex == -1) {
                    Log.e("LOGIN", "Column not found in cursor");
                    return new LoginResult(false, null, 0, null, null, null);
                }

                String userType = cursor.getString(userTypeIndex);
                int userId = cursor.getInt(idIndex);
                String nama = cursor.getString(namaIndex);
                String email = cursor.getString(emailIndex);
                String npm = cursor.getString(npmIndex);

                Log.d("LOGIN", "Login successful - User Type: " + userType + ", Name: " + nama);

                cursor.close();
                db.close();

                return new LoginResult(true, userType, userId, nama, email, npm);
            } else {
                Log.d("LOGIN", "Login failed - No user found or wrong credentials");
            }
        } catch (Exception e) {
            Log.e("LOGIN", "Database error: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return new LoginResult(false, null, 0, null, null, null);
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + " = ?";
            cursor = db.rawQuery(query, new String[]{email});
            boolean exists = cursor.getCount() > 0;
            return exists;
        } catch (Exception e) {
            Log.e("DATABASE", "Error checking email: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public boolean isNpmExists(String npm) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_NPM + " = ?";
            cursor = db.rawQuery(query, new String[]{npm});
            boolean exists = cursor.getCount() > 0;
            return exists;
        } catch (Exception e) {
            Log.e("DATABASE", "Error checking NPM: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public boolean createAdminUser(String nama, String email, String npm, String password) {
        if (isEmailExists(email) || isNpmExists(npm)) {
            return false;
        }

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

    // ========== METHODS UNTUK KEGIATAN (DIPERBARUI) ==========
    public boolean tambahKegiatan(String nama, String jenis, String penyelenggara,
                                  String deskripsi, String tanggal, String waktu,
                                  String lokasi, long timestamp, String fotoBase64) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEGIATAN_NAMA, nama);
        values.put(COLUMN_KEGIATAN_JENIS, jenis);
        values.put(COLUMN_KEGIATAN_PENYELENGGARA, penyelenggara);
        values.put(COLUMN_KEGIATAN_DESKRIPSI, deskripsi);
        values.put(COLUMN_KEGIATAN_TANGGAL, tanggal);
        values.put(COLUMN_KEGIATAN_WAKTU, waktu);
        values.put(COLUMN_KEGIATAN_LOKASI, lokasi);
        values.put(COLUMN_KEGIATAN_TANGGAL_TIMESTAMP, timestamp);
        values.put(COLUMN_KEGIATAN_FOTO, fotoBase64);

        try {
            long result = db.insert(TABLE_KEGIATAN, null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            Log.e("DATABASE", "Error adding kegiatan: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public Cursor getAllKegiatan() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT * FROM " + TABLE_KEGIATAN +
                    " ORDER BY " + COLUMN_KEGIATAN_TANGGAL_TIMESTAMP + " DESC";
            return db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting kegiatan: " + e.getMessage());
            return null;
        }
    }

    public Cursor getKegiatanById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT * FROM " + TABLE_KEGIATAN +
                    " WHERE " + COLUMN_KEGIATAN_ID + " = ?";
            return db.rawQuery(query, new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting kegiatan by ID: " + e.getMessage());
            return null;
        }
    }

    public boolean updateKegiatan(int id, String nama, String jenis, String penyelenggara,
                                  String deskripsi, String tanggal, String waktu,
                                  String lokasi, long timestamp, String fotoBase64) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEGIATAN_NAMA, nama);
        values.put(COLUMN_KEGIATAN_JENIS, jenis);
        values.put(COLUMN_KEGIATAN_PENYELENGGARA, penyelenggara);
        values.put(COLUMN_KEGIATAN_DESKRIPSI, deskripsi);
        values.put(COLUMN_KEGIATAN_TANGGAL, tanggal);
        values.put(COLUMN_KEGIATAN_WAKTU, waktu);
        values.put(COLUMN_KEGIATAN_LOKASI, lokasi);
        values.put(COLUMN_KEGIATAN_TANGGAL_TIMESTAMP, timestamp);
        values.put(COLUMN_KEGIATAN_FOTO, fotoBase64);

        try {
            int result = db.update(TABLE_KEGIATAN, values,
                    COLUMN_KEGIATAN_ID + " = ?",
                    new String[]{String.valueOf(id)});
            db.close();
            return result > 0;
        } catch (Exception e) {
            Log.e("DATABASE", "Error updating kegiatan: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public boolean deleteKegiatan(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int result = db.delete(TABLE_KEGIATAN,
                    COLUMN_KEGIATAN_ID + " = ?",
                    new String[]{String.valueOf(id)});
            db.close();
            return result > 0;
        } catch (Exception e) {
            Log.e("DATABASE", "Error deleting kegiatan: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public Cursor searchKegiatan(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT * FROM " + TABLE_KEGIATAN +
                    " WHERE " + COLUMN_KEGIATAN_NAMA + " LIKE ? OR " +
                    COLUMN_KEGIATAN_JENIS + " LIKE ? OR " +
                    COLUMN_KEGIATAN_PENYELENGGARA + " LIKE ? OR " +
                    COLUMN_KEGIATAN_DESKRIPSI + " LIKE ? " +
                    " ORDER BY " + COLUMN_KEGIATAN_TANGGAL_TIMESTAMP + " DESC";
            return db.rawQuery(query,
                    new String[]{"%" + keyword + "%", "%" + keyword + "%",
                            "%" + keyword + "%", "%" + keyword + "%"});
        } catch (Exception e) {
            Log.e("DATABASE", "Error searching kegiatan: " + e.getMessage());
            return null;
        }
    }

    // Method untuk mendapatkan 3 kegiatan terbaru
    public Cursor getLatestKegiatan(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT * FROM " + TABLE_KEGIATAN +
                    " ORDER BY " + COLUMN_KEGIATAN_TANGGAL_TIMESTAMP + " DESC LIMIT ?";
            return db.rawQuery(query, new String[]{String.valueOf(limit)});
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting latest kegiatan: " + e.getMessage());
            return null;
        }
    }

    // ========== HELPER METHODS UNTUK FOTO ==========
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("DATABASE", "Error converting bitmap to base64: " + e.getMessage());
            return "";
        }
    }

    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("DATABASE", "Error converting base64 to bitmap: " + e.getMessage());
            return null;
        }
    }

    // ========== METHOD TAMBAHAN UNTUK DEBUG ==========
    public String getAllUsersAsString() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        StringBuilder result = new StringBuilder();

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String nama = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAMA));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                    String npm = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NPM));
                    String userType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_TYPE));

                    result.append("ID: ").append(id)
                            .append(", Nama: ").append(nama)
                            .append(", Email: ").append(email)
                            .append(", NPM: ").append(npm)
                            .append(", Type: ").append(userType)
                            .append("\n");
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting users: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return result.toString();
    }

    // ========== INNER CLASS LOGIN RESULT ==========
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
}