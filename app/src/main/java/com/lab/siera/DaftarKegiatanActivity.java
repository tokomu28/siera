package com.lab.siera;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DaftarKegiatanActivity extends AppCompatActivity {

    private LinearLayout kegiatanContainer;
    private EditText etSearchKegiatan;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_kegiatan);

        // Initialize DataManager
        dataManager = DataManager.getInstance(this);

        // Initialize views
        kegiatanContainer = findViewById(R.id.kegiatanContainer);
        etSearchKegiatan = findViewById(R.id.etSearchKegiatan);

        // Setup search
        setupSearch();

        // Setup back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Load semua kegiatan
        loadAllKegiatan("");
    }

    private void setupSearch() {
        etSearchKegiatan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadAllKegiatan(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadAllKegiatan(String keyword) {
        // Kosongkan container
        kegiatanContainer.removeAllViews();

        Cursor cursor;
        if (keyword.isEmpty()) {
            cursor = dataManager.getAllKegiatan();
        } else {
            cursor = dataManager.searchKegiatan(keyword);
        }

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

                // Tambahkan card
                addKegiatanCard(id, nama, jenis, penyelenggara, deskripsi, tanggal, waktu, lokasi, fotoBase64);

            } while (cursor.moveToNext());
            cursor.close();
        } else {
            showEmptyState();
        }
    }

    private void addKegiatanCard(int id, String nama, String jenis, String penyelenggara,
                                 String deskripsi, String tanggal, String waktu,
                                 String lokasi, String fotoBase64) {
        LayoutInflater inflater = LayoutInflater.from(this);
        CardView cardView = (CardView) inflater.inflate(R.layout.item_kegiatan_list, kegiatanContainer, false);

        ImageView ivGambar = cardView.findViewById(R.id.ivGambarList);
        TextView tvJenis = cardView.findViewById(R.id.tvJenisList);
        TextView tvJudul = cardView.findViewById(R.id.tvJudulList);
        TextView tvDetail = cardView.findViewById(R.id.tvDetailList);
        Button btnDetail = cardView.findViewById(R.id.btnDetailList);

        tvJenis.setText(jenis);
        tvJudul.setText(nama);

        String detailText = tanggal;
        if (waktu != null && !waktu.isEmpty()) {
            detailText += ", " + waktu;
        }
        if (lokasi != null && !lokasi.isEmpty()) {
            detailText += " - " + lokasi;
        }
        tvDetail.setText(detailText);

        // Load gambar
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            Bitmap bitmap = DataManager.base64ToBitmap(fotoBase64);
            if (bitmap != null) {
                ivGambar.setImageBitmap(bitmap);
            }
        }

        btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(DaftarKegiatanActivity.this, DetailKegiatanActivity.class);
            intent.putExtra("KEGIATAN_ID", id);
            startActivity(intent);
        });

        kegiatanContainer.addView(cardView);
    }

    private void showEmptyState() {
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("Tidak ada kegiatan ditemukan");
        tvEmpty.setTextSize(16);
        tvEmpty.setGravity(android.view.Gravity.CENTER);
        tvEmpty.setPadding(0, 50, 0, 0);
        kegiatanContainer.addView(tvEmpty);
    }
}