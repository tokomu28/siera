package com.lab.siera.admin;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lab.siera.DatabaseHelper;
import com.lab.siera.DataManager;
import com.lab.siera.R;
import com.lab.siera.SessionManager;
import androidx.appcompat.app.AlertDialog; // TAMBAHKAN IMPORT INI

public class UserActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView btnBack, btnProfile, btnLogout, btnFilter;
    private BottomNavigationView bottomNav;
    private LinearLayout containerPendaftaran;
    private DataManager dataManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        dataManager = DataManager.getInstance(this);
        sessionManager = new SessionManager(this);

        // Inisialisasi views
        initViews();
        setupClickListeners();
        setupBottomNavigation();

        // Load data pendaftaran
        loadPendaftaranData();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnFilter = findViewById(R.id.btnFilter);
        bottomNav = findViewById(R.id.bottomNavBar);
        containerPendaftaran = findViewById(R.id.containerPendaftaran);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Profile button di header
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Logout button di header - DITAMBAHKAN POPUP KONFIRMASI
        btnLogout.setOnClickListener(v -> {
            // Konfirmasi logout
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Apakah Anda yakin ingin logout?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        // Gunakan SessionManager untuk logout
                        sessionManager.logoutUser();

                        Intent intent = new Intent(UserActivity.this, com.lab.siera.MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });

        // Filter button (Q button)
        btnFilter.setOnClickListener(v -> {
            String searchText = etSearch.getText().toString().trim();
            if (searchText.isEmpty()) {
                // Tampilkan semua data
                loadPendaftaranData();
                Toast.makeText(this, "Menampilkan semua data", Toast.LENGTH_SHORT).show();
            } else {
                // Filter berdasarkan pencarian
                filterData(searchText);
            }
        });
    }

    private void loadPendaftaranData() {
        // Kosongkan container terlebih dahulu
        containerPendaftaran.removeAllViews();

        // Dapatkan semua pendaftaran
        Cursor cursor = dataManager.getAllPendaftaran();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int pendaftaranId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PENDAFTARAN_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                int kegiatanId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_ID));
                String tanggalPendaftaran = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL_PENDAFTARAN));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS));

                // Data dari join
                String userNama = cursor.getString(cursor.getColumnIndexOrThrow("user_nama"));
                String userNpm = cursor.getString(cursor.getColumnIndexOrThrow("user_npm"));
                String kegiatanNama = cursor.getString(cursor.getColumnIndexOrThrow("kegiatan_nama"));
                String kegiatanJenis = cursor.getString(cursor.getColumnIndexOrThrow("kegiatan_jenis"));
                String kegiatanTanggal = cursor.getString(cursor.getColumnIndexOrThrow("kegiatan_tanggal"));

                // Tambahkan item ke UI
                addPendaftaranItem(pendaftaranId, userNama, userNpm, kegiatanNama,
                        kegiatanJenis, kegiatanTanggal, tanggalPendaftaran, status);

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            // Jika tidak ada data
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Belum ada pendaftaran");
            tvEmpty.setTextSize(16);
            tvEmpty.setPadding(0, 50, 0, 0);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            containerPendaftaran.addView(tvEmpty);
        }
    }

    private void addPendaftaranItem(int pendaftaranId, String userNama, String userNpm,
                                    String kegiatanNama, String kegiatanJenis,
                                    String kegiatanTanggal, String tanggalPendaftaran, String status) {

        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_pendaftaran, containerPendaftaran, false);

        // Initialize views
        TextView tvNamaPendaftar = itemView.findViewById(R.id.tvNamaPendaftar);
        TextView tvNamaKegiatan = itemView.findViewById(R.id.tvNamaKegiatan);
        TextView tvJenis = itemView.findViewById(R.id.tvJenis);
        TextView tvTanggal = itemView.findViewById(R.id.tvTanggal);
        TextView tvStatus = itemView.findViewById(R.id.tvStatus);
        CheckBox cbStatus = itemView.findViewById(R.id.cbStatus);

        // Set data
        tvNamaPendaftar.setText(userNama + "\n(" + userNpm + ")");
        tvNamaKegiatan.setText(kegiatanNama);
        tvJenis.setText(kegiatanJenis);
        tvTanggal.setText(kegiatanTanggal);
        tvStatus.setText("Status: " + status);

        // Set checkbox berdasarkan status
        if (status.equals("approved")) {
            cbStatus.setChecked(true);
        } else {
            cbStatus.setChecked(false);
        }

        // Set warna status
        switch (status) {
            case "approved":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "rejected":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            case "pending":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
        }

        // Set listener untuk checkbox
        cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "approved" : "rejected";
            updatePendaftaranStatus(pendaftaranId, newStatus, userNama, kegiatanNama);
        });

        // Tambahkan click listener untuk item
        itemView.setOnClickListener(v -> {
            showDetailPendaftaran(userNama, userNpm, kegiatanNama, kegiatanJenis,
                    kegiatanTanggal, tanggalPendaftaran, status);
        });

        // Tambahkan ke container
        containerPendaftaran.addView(itemView);
    }

    private void updatePendaftaranStatus(int pendaftaranId, String status, String userNama, String kegiatanNama) {
        boolean success = dataManager.updateStatusPendaftaran(pendaftaranId, status);

        if (success) {
            String message = "Status pendaftaran " + userNama + " untuk " +
                    kegiatanNama + " diubah menjadi " + status;
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            // Refresh data
            loadPendaftaranData();
        } else {
            Toast.makeText(this, "Gagal mengubah status", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterData(String keyword) {
        // Kosongkan container terlebih dahulu
        containerPendaftaran.removeAllViews();

        // Dapatkan semua pendaftaran untuk difilter
        Cursor cursor = dataManager.getAllPendaftaran();

        boolean hasData = false;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int pendaftaranId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PENDAFTARAN_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
                int kegiatanId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_ID));
                String tanggalPendaftaran = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TANGGAL_PENDAFTARAN));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS));

                // Data dari join
                String userNama = cursor.getString(cursor.getColumnIndexOrThrow("user_nama"));
                String userNpm = cursor.getString(cursor.getColumnIndexOrThrow("user_npm"));
                String kegiatanNama = cursor.getString(cursor.getColumnIndexOrThrow("kegiatan_nama"));
                String kegiatanJenis = cursor.getString(cursor.getColumnIndexOrThrow("kegiatan_jenis"));
                String kegiatanTanggal = cursor.getString(cursor.getColumnIndexOrThrow("kegiatan_tanggal"));

                // Filter berdasarkan keyword
                if (userNama.toLowerCase().contains(keyword.toLowerCase()) ||
                        userNpm.toLowerCase().contains(keyword.toLowerCase()) ||
                        kegiatanNama.toLowerCase().contains(keyword.toLowerCase()) ||
                        kegiatanJenis.toLowerCase().contains(keyword.toLowerCase()) ||
                        status.toLowerCase().contains(keyword.toLowerCase())) {

                    // Tambahkan item ke UI
                    addPendaftaranItem(pendaftaranId, userNama, userNpm, kegiatanNama,
                            kegiatanJenis, kegiatanTanggal, tanggalPendaftaran, status);
                    hasData = true;
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        if (!hasData) {
            // Jika tidak ada data yang cocok
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Tidak ditemukan data dengan kata kunci: " + keyword);
            tvEmpty.setTextSize(14);
            tvEmpty.setPadding(0, 50, 0, 0);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            containerPendaftaran.addView(tvEmpty);
        }
    }

    private void showDetailPendaftaran(String userNama, String userNpm, String kegiatanNama,
                                       String kegiatanJenis, String kegiatanTanggal,
                                       String tanggalPendaftaran, String status) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Detail Pendaftaran");
        builder.setMessage(
                "Nama: " + userNama + "\n" +
                        "NPM: " + userNpm + "\n" +
                        "Kegiatan: " + kegiatanNama + "\n" +
                        "Jenis: " + kegiatanJenis + "\n" +
                        "Tanggal Kegiatan: " + kegiatanTanggal + "\n" +
                        "Tanggal Daftar: " + tanggalPendaftaran + "\n" +
                        "Status: " + status
        );
        builder.setPositiveButton("TUTUP", null);
        builder.show();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Jika sudah di halaman yang dituju, tidak perlu intent
            if (itemId == R.id.nav_admin_users) {
                return true;
            }

            try {
                Intent intent = null;

                if (itemId == R.id.nav_admin_home) {
                    intent = new Intent(UserActivity.this, DashboardAdminActivity.class);
                } else if (itemId == R.id.nav_admin_activities) {
                    intent = new Intent(UserActivity.this, ManajemenKegiatanActivity.class);
                } else if (itemId == R.id.nav_admin_profile) {
                    intent = new Intent(UserActivity.this, ProfileActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return true;
        });

        // Set users as selected
        bottomNav.setSelectedItemId(R.id.nav_admin_users);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali ke activity
        loadPendaftaranData();
        // Update bottom nav selection
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_admin_users);
        }
    }
}