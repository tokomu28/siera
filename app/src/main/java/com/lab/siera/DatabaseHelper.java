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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "siera.db";
    private static final int DATABASE_VERSION = 6;

    // ========== TABEL USERS ==========
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAMA = "nama";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_NPM = "npm";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USER_TYPE = "user_type";

    // ========== TABEL KEGIATAN ==========
    public static final String TABLE_KEGIATAN = "kegiatan";
    public static final String COLUMN_KEGIATAN_ID = "kegiatan_id"; // HANYA SATU KALI DI SINI
    public static final String COLUMN_KEGIATAN_NAMA = "nama_kegiatan";
    public static final String COLUMN_KEGIATAN_JENIS = "jenis";
    public static final String COLUMN_KEGIATAN_PENYELENGGARA = "penyelenggara";
    public static final String COLUMN_KEGIATAN_DESKRIPSI = "deskripsi";
    public static final String COLUMN_KEGIATAN_TANGGAL = "tanggal";
    public static final String COLUMN_KEGIATAN_WAKTU = "waktu";
    public static final String COLUMN_KEGIATAN_LOKASI = "lokasi";
    public static final String COLUMN_KEGIATAN_TANGGAL_TIMESTAMP = "tanggal_timestamp";
    public static final String COLUMN_KEGIATAN_FOTO = "foto";

    // ========== TABEL PENDAFTARAN ==========
    public static final String TABLE_PENDAFTARAN = "pendaftaran";
    public static final String COLUMN_PENDAFTARAN_ID = "pendaftaran_id";
    public static final String COLUMN_USER_ID = "user_id";
    // TIDAK PERLU DEKLARASI ULANG COLUMN_KEGIATAN_ID DI SINI
    public static final String COLUMN_TANGGAL_PENDAFTARAN = "tanggal_pendaftaran";
    public static final String COLUMN_STATUS = "status";

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

    // Query untuk membuat tabel kegiatan
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
                    COLUMN_KEGIATAN_FOTO + " TEXT" +
                    ")";

    // Query untuk membuat tabel pendaftaran
    private static final String CREATE_TABLE_PENDAFTARAN =
            "CREATE TABLE " + TABLE_PENDAFTARAN + "(" +
                    COLUMN_PENDAFTARAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_KEGIATAN_ID + " INTEGER NOT NULL," + // GUNAKAN YANG SUDAH ADA
                    COLUMN_TANGGAL_PENDAFTARAN + " TEXT NOT NULL," +
                    COLUMN_STATUS + " TEXT DEFAULT 'pending'," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")," +
                    "FOREIGN KEY(" + COLUMN_KEGIATAN_ID + ") REFERENCES " + TABLE_KEGIATAN + "(" + COLUMN_KEGIATAN_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABASE", "Creating database tables...");

        // Buat ketiga tabel
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_KEGIATAN);
        db.execSQL(CREATE_TABLE_PENDAFTARAN);

        Log.d("DATABASE", "Tables created successfully");

        // Insert data default
        insertDefaultAdmin(db);
        insertSampleUsers(db);
        insertSampleKegiatan(db);
        insertSamplePendaftaran(db);
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
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEGIATAN);
                db.execSQL(CREATE_TABLE_KEGIATAN);
                insertSampleKegiatan(db);
            }
        }

        if (oldVersion < 6) {
            db.execSQL(CREATE_TABLE_PENDAFTARAN);
            insertSamplePendaftaran(db);
            Log.d("DATABASE", "Database upgraded to version 6 - Added pendaftaran table");
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

    private void insertSamplePendaftaran(SQLiteDatabase db) {
        ContentValues pendaftaran1 = new ContentValues();
        pendaftaran1.put(COLUMN_USER_ID, 2);
        pendaftaran1.put(COLUMN_KEGIATAN_ID, 1);
        pendaftaran1.put(COLUMN_TANGGAL_PENDAFTARAN, "10 Jan 2025");
        pendaftaran1.put(COLUMN_STATUS, "pending");

        ContentValues pendaftaran2 = new ContentValues();
        pendaftaran2.put(COLUMN_USER_ID, 3);
        pendaftaran2.put(COLUMN_KEGIATAN_ID, 2);
        pendaftaran2.put(COLUMN_TANGGAL_PENDAFTARAN, "12 Jan 2025");
        pendaftaran2.put(COLUMN_STATUS, "pending");

        try {
            db.insert(TABLE_PENDAFTARAN, null, pendaftaran1);
            db.insert(TABLE_PENDAFTARAN, null, pendaftaran2);
            Log.d("DATABASE", "Sample pendaftaran created");
        } catch (Exception e) {
            Log.e("DATABASE", "Error inserting sample pendaftaran: " + e.getMessage());
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

    // ========== METHODS UNTUK KEGIATAN ==========
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
            db.delete(TABLE_PENDAFTARAN,
                    COLUMN_KEGIATAN_ID + " = ?",
                    new String[]{String.valueOf(id)});

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

    // ========== METHODS UNTUK PENDAFTARAN ==========
    public boolean daftarKegiatan(int userId, int kegiatanId, String tanggal) {
        SQLiteDatabase db = this.getWritableDatabase();

        String checkQuery = "SELECT * FROM " + TABLE_PENDAFTARAN +
                " WHERE " + COLUMN_USER_ID + " = ? AND " +
                COLUMN_KEGIATAN_ID + " = ?";

        Cursor cursor = db.rawQuery(checkQuery, new String[]{String.valueOf(userId), String.valueOf(kegiatanId)});

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return false;
        }

        if (cursor != null) {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_KEGIATAN_ID, kegiatanId);
        values.put(COLUMN_TANGGAL_PENDAFTARAN, tanggal);
        values.put(COLUMN_STATUS, "pending");

        try {
            long result = db.insert(TABLE_PENDAFTARAN, null, values);
            db.close();
            return result != -1;
        } catch (Exception e) {
            Log.e("DATABASE", "Error registering for kegiatan: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public boolean updateStatusPendaftaran(int pendaftaranId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);

        try {
            int result = db.update(TABLE_PENDAFTARAN, values,
                    COLUMN_PENDAFTARAN_ID + " = ?",
                    new String[]{String.valueOf(pendaftaranId)});
            db.close();
            return result > 0;
        } catch (Exception e) {
            Log.e("DATABASE", "Error updating status: " + e.getMessage());
            db.close();
            return false;
        }
    }

    public Cursor getAllPendaftaran() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT p.*, " +
                    "u." + COLUMN_NAMA + " as user_nama, " +
                    "u." + COLUMN_NPM + " as user_npm, " +
                    "k." + COLUMN_KEGIATAN_NAMA + " as kegiatan_nama, " +
                    "k." + COLUMN_KEGIATAN_JENIS + " as kegiatan_jenis, " +
                    "k." + COLUMN_KEGIATAN_TANGGAL + " as kegiatan_tanggal " +
                    "FROM " + TABLE_PENDAFTARAN + " p " +
                    "INNER JOIN " + TABLE_USERS + " u ON p." + COLUMN_USER_ID + " = u." + COLUMN_ID + " " +
                    "INNER JOIN " + TABLE_KEGIATAN + " k ON p." + COLUMN_KEGIATAN_ID + " = k." + COLUMN_KEGIATAN_ID + " " +
                    "ORDER BY p." + COLUMN_PENDAFTARAN_ID + " DESC";

            return db.rawQuery(query, null);
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting all pendaftaran: " + e.getMessage());
            return null;
        }
    }

    public Cursor getPendaftaranByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT p.*, " +
                    "u." + COLUMN_NAMA + " as user_nama, " +
                    "u." + COLUMN_NPM + " as user_npm, " +
                    "k." + COLUMN_KEGIATAN_NAMA + " as kegiatan_nama, " +
                    "k." + COLUMN_KEGIATAN_JENIS + " as kegiatan_jenis, " +
                    "k." + COLUMN_KEGIATAN_TANGGAL + " as kegiatan_tanggal " +
                    "FROM " + TABLE_PENDAFTARAN + " p " +
                    "INNER JOIN " + TABLE_USERS + " u ON p." + COLUMN_USER_ID + " = u." + COLUMN_ID + " " +
                    "INNER JOIN " + TABLE_KEGIATAN + " k ON p." + COLUMN_KEGIATAN_ID + " = k." + COLUMN_KEGIATAN_ID + " " +
                    "WHERE p." + COLUMN_STATUS + " = ? " +
                    "ORDER BY p." + COLUMN_PENDAFTARAN_ID + " DESC";

            return db.rawQuery(query, new String[]{status});
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting pendaftaran by status: " + e.getMessage());
            return null;
        }
    }

    public boolean isUserRegisteredForKegiatan(int userId, int kegiatanId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + TABLE_PENDAFTARAN +
                    " WHERE " + COLUMN_USER_ID + " = ? AND " +
                    COLUMN_KEGIATAN_ID + " = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(kegiatanId)});
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("DATABASE", "Error checking registration: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public Cursor getPendaftaranByUserId(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT p.*, " +
                    "k." + COLUMN_KEGIATAN_NAMA + " as kegiatan_nama, " +
                    "k." + COLUMN_KEGIATAN_JENIS + " as kegiatan_jenis, " +
                    "k." + COLUMN_KEGIATAN_TANGGAL + " as kegiatan_tanggal, " +
                    "k." + COLUMN_KEGIATAN_PENYELENGGARA + " as kegiatan_penyelenggara " +
                    "FROM " + TABLE_PENDAFTARAN + " p " +
                    "INNER JOIN " + TABLE_KEGIATAN + " k ON p." + COLUMN_KEGIATAN_ID + " = k." + COLUMN_KEGIATAN_ID + " " +
                    "WHERE p." + COLUMN_USER_ID + " = ? " +
                    "ORDER BY p." + COLUMN_PENDAFTARAN_ID + " DESC";

            return db.rawQuery(query, new String[]{String.valueOf(userId)});
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting pendaftaran by user ID: " + e.getMessage());
            return null;
        }
    }

    public int getJumlahPendaftaranByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) as total FROM " + TABLE_PENDAFTARAN +
                    " WHERE " + COLUMN_STATUS + " = ?";

            cursor = db.rawQuery(query, new String[]{status});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting jumlah pendaftaran: " + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public int getTotalPendaftaran() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) as total FROM " + TABLE_PENDAFTARAN;

            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting total pendaftaran: " + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
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

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date());
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

    public String getAllPendaftaranAsString() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        StringBuilder result = new StringBuilder();

        try {
            String query = "SELECT p.*, u.nama as user_nama, k.nama_kegiatan " +
                    "FROM " + TABLE_PENDAFTARAN + " p " +
                    "INNER JOIN " + TABLE_USERS + " u ON p.user_id = u.id " +
                    "INNER JOIN " + TABLE_KEGIATAN + " k ON p.kegiatan_id = k.kegiatan_id";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PENDAFTARAN_ID));
                    String userNama = cursor.getString(cursor.getColumnIndexOrThrow("user_nama"));
                    String kegiatanNama = cursor.getString(cursor.getColumnIndexOrThrow("nama_kegiatan"));
                    String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TANGGAL_PENDAFTARAN));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));

                    result.append("ID: ").append(id)
                            .append(", User: ").append(userNama)
                            .append(", Kegiatan: ").append(kegiatanNama)
                            .append(", Tanggal: ").append(tanggal)
                            .append(", Status: ").append(status)
                            .append("\n");
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DATABASE", "Error getting pendaftaran: " + e.getMessage());
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