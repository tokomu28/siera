package com.lab.siera;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lab.siera.admin.ProfileActivity;

public class DashboardActivity extends AppCompatActivity {

    private LinearLayout cardContainer;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize DataManager
        dataManager = DataManager.getInstance(this);

        // Initialize views
        cardContainer = findViewById(R.id.cardContainer); // Akan dibuat nanti

        // Setup bottom navigation
        setupBottomNavigation();

        // Load kegiatan dari database
        loadKegiatanFromDatabase();
    }

    private void loadKegiatanFromDatabase() {
        // Kosongkan container terlebih dahulu
        cardContainer.removeAllViews();

        // Dapatkan 3 kegiatan terbaru
        Cursor cursor = dataManager.getLatestKegiatan(3);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_ID));
                String nama = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_NAMA));
                String jenis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_JENIS));
                String penyelenggara = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_PENYELENGGARA));
                String deskripsi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_DESKRIPSI));
                String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_TANGGAL));
                String waktu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_WAKTU));
                String lokasi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_LOKASI));
                String fotoBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_FOTO));

                // Handle null values
                if (deskripsi == null) deskripsi = "";
                if (waktu == null) waktu = "";
                if (lokasi == null) lokasi = "";
                if (fotoBase64 == null) fotoBase64 = "";

                // Tambahkan card ke UI
                addKegiatanCardToUI(id, nama, jenis, penyelenggara, deskripsi, tanggal, waktu, lokasi, fotoBase64);

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            // Jika tidak ada data, tampilkan pesan
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Belum ada kegiatan tersedia");
            tvEmpty.setTextSize(16);
            tvEmpty.setPadding(0, 50, 0, 0);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            cardContainer.addView(tvEmpty);
        }
    }

    private void addKegiatanCardToUI(int id, String nama, String jenis, String penyelenggara,
                                     String deskripsi, String tanggal, String waktu,
                                     String lokasi, String fotoBase64) {
        // Inflate layout card kegiatan
        LayoutInflater inflater = LayoutInflater.from(this);
        View cardView = inflater.inflate(R.layout.item_kegiatan_dashboard, cardContainer, false);

        // Initialize views
        ImageView ivGambar = cardView.findViewById(R.id.ivGambar);
        TextView tvJenis = cardView.findViewById(R.id.tvJenis);
        TextView tvJudul = cardView.findViewById(R.id.tvJudul);
        TextView tvDetail = cardView.findViewById(R.id.tvDetail);
        Button btnDetail = cardView.findViewById(R.id.btnDetail);

        // Set data ke views
        tvJenis.setText(jenis);
        tvJudul.setText(nama);

        // Format detail: Tanggal, Waktu - Lokasi
        String detailText = tanggal;
        if (!waktu.isEmpty()) {
            detailText += ", " + waktu;
        }
        if (!lokasi.isEmpty()) {
            detailText += " - " + lokasi;
        }
        tvDetail.setText(detailText);

        // Load foto jika ada
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            Bitmap bitmap = DataManager.base64ToBitmap(fotoBase64);
            if (bitmap != null) {
                ivGambar.setImageBitmap(bitmap);
            } else {
                // Set default image jika gagal load
                ivGambar.setImageResource(R.drawable.img_event1);
            }
        } else {
            // Set default image jika tidak ada foto
            ivGambar.setImageResource(R.drawable.img_event1);
        }

        // Setup button detail
        btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DetailKegiatanActivity.class);
            intent.putExtra("KEGIATAN_ID", id);
            startActivity(intent);
        });

        // Tambahkan card ke container
        cardContainer.addView(cardView);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavBar);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Sudah di halaman home
                return true;
            } else if (itemId == R.id.nav_chart) {
                Intent intent = new Intent(DashboardActivity.this, DaftarKegiatanActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(DashboardActivity.this, ProfileUserActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });

        // Set home as selected
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data saat kembali ke dashboard
        loadKegiatanFromDatabase();
    }
}