package com.lab.siera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;

public class DetailKegiatanActivity extends AppCompatActivity {

    private ImageView ivFotoDetail;
    private TextView tvJudulDetail, tvJenisDetail, tvPenyelenggaraDetail;
    private TextView tvDeskripsiDetail, tvTanggalDetail, tvWaktuDetail, tvLokasiDetail;
    private Button btnDaftarSekarang;
    private DataManager dataManager;
    private SessionManager sessionManager;
    private int kegiatanId;
    private String kegiatanNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kegiatan);

        // Initialize DataManager dan SessionManager
        dataManager = DataManager.getInstance(this);
        sessionManager = new SessionManager(this);

        // Initialize views
        ivFotoDetail = findViewById(R.id.ivFotoDetail);
        tvJudulDetail = findViewById(R.id.tvJudulDetail);
        tvJenisDetail = findViewById(R.id.tvJenisDetail);
        tvPenyelenggaraDetail = findViewById(R.id.tvPenyelenggaraDetail);
        tvDeskripsiDetail = findViewById(R.id.tvDeskripsiDetail);
        tvTanggalDetail = findViewById(R.id.tvTanggalDetail);
        tvWaktuDetail = findViewById(R.id.tvWaktuDetail);
        tvLokasiDetail = findViewById(R.id.tvLokasiDetail);
        btnDaftarSekarang = findViewById(R.id.btnDaftarSekarang);

        // Get kegiatan ID from intent
        kegiatanId = getIntent().getIntExtra("KEGIATAN_ID", -1);

        if (kegiatanId != -1) {
            loadKegiatanDetail(kegiatanId);
        }

        // Cek apakah user sudah login
        if (!sessionManager.isLoggedIn()) {
            btnDaftarSekarang.setEnabled(false);
            btnDaftarSekarang.setText("LOGIN TERLEBIH DAHULU");
            btnDaftarSekarang.setBackgroundResource(R.drawable.bg_gradient_gray);
            return;
        }

        // Cek apakah user sudah terdaftar di kegiatan ini
        int userId = sessionManager.getUserId();
        if (dataManager.isUserRegisteredForKegiatan(userId, kegiatanId)) {
            btnDaftarSekarang.setEnabled(false);
            btnDaftarSekarang.setText("SUDAH TERDAFTAR");
            btnDaftarSekarang.setBackgroundResource(R.drawable.bg_gradient_gray);
        }

        // Set click listener untuk button daftar
        btnDaftarSekarang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });
    }

    private void loadKegiatanDetail(int id) {
        Cursor cursor = dataManager.getKegiatanById(id);

        if (cursor != null && cursor.moveToFirst()) {
            kegiatanNama = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_NAMA));
            String jenis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_JENIS));
            String penyelenggara = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_PENYELENGGARA));
            String deskripsi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_DESKRIPSI));
            String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_TANGGAL));
            String waktu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_WAKTU));
            String lokasi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_LOKASI));
            String fotoBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_FOTO));

            // Set data ke views
            tvJudulDetail.setText(kegiatanNama);
            tvJenisDetail.setText(jenis);
            tvPenyelenggaraDetail.setText("Diselenggarakan oleh: " + penyelenggara);
            tvDeskripsiDetail.setText(deskripsi);
            tvTanggalDetail.setText("Tanggal: " + tanggal);

            if (waktu != null && !waktu.isEmpty()) {
                tvWaktuDetail.setText("Waktu: " + waktu);
            } else {
                tvWaktuDetail.setVisibility(View.GONE);
            }

            if (lokasi != null && !lokasi.isEmpty()) {
                tvLokasiDetail.setText("Lokasi: " + lokasi);
            } else {
                tvLokasiDetail.setVisibility(View.GONE);
            }

            // Load foto jika ada
            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                Bitmap bitmap = DataManager.base64ToBitmap(fotoBase64);
                if (bitmap != null) {
                    ivFotoDetail.setImageBitmap(bitmap);
                }
            }

            cursor.close();
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Pendaftaran");
        builder.setMessage("Apakah Anda yakin ingin mendaftar pada kegiatan \"" + kegiatanNama + "\"?");

        builder.setPositiveButton("DAFTAR", (dialog, which) -> {
            prosesPendaftaran();
        });

        builder.setNegativeButton("BATAL", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void prosesPendaftaran() {
        // Ambil data user dari session
        int userId = sessionManager.getUserId();
        String tanggalPendaftaran = DataManager.getCurrentDate();

        // Simpan pendaftaran ke database
        boolean success = dataManager.daftarKegiatan(userId, kegiatanId, tanggalPendaftaran);

        if (success) {
            // Update UI
            btnDaftarSekarang.setEnabled(false);
            btnDaftarSekarang.setText("SUDAH TERDAFTAR");
            btnDaftarSekarang.setBackgroundResource(R.drawable.bg_gradient_gray);

            // Tampilkan pesan sukses
            new AlertDialog.Builder(this)
                    .setTitle("Pendaftaran Berhasil")
                    .setMessage("Pendaftaran Anda telah dikirim dan menunggu persetujuan admin.")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            Toast.makeText(this, "Gagal mendaftar. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
        }
    }
}