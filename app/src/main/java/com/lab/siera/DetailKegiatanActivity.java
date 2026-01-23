package com.lab.siera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;

public class DetailKegiatanActivity extends AppCompatActivity {

    private ImageView ivFotoDetail;
    private TextView tvJudulDetail, tvJenisDetail, tvPenyelenggaraDetail;
    private TextView tvDeskripsiDetail, tvTanggalDetail, tvWaktuDetail, tvLokasiDetail;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_kegiatan);

        // Initialize DataManager
        dataManager = DataManager.getInstance(this);

        // Initialize views
        ivFotoDetail = findViewById(R.id.ivFotoDetail);
        tvJudulDetail = findViewById(R.id.tvJudulDetail);
        tvJenisDetail = findViewById(R.id.tvJenisDetail);
        tvPenyelenggaraDetail = findViewById(R.id.tvPenyelenggaraDetail);
        tvDeskripsiDetail = findViewById(R.id.tvDeskripsiDetail);
        tvTanggalDetail = findViewById(R.id.tvTanggalDetail);
        tvWaktuDetail = findViewById(R.id.tvWaktuDetail);
        tvLokasiDetail = findViewById(R.id.tvLokasiDetail);

        // Get kegiatan ID from intent
        int kegiatanId = getIntent().getIntExtra("KEGIATAN_ID", -1);

        if (kegiatanId != -1) {
            loadKegiatanDetail(kegiatanId);
        }
    }

    private void loadKegiatanDetail(int id) {
        Cursor cursor = dataManager.getKegiatanById(id);

        if (cursor != null && cursor.moveToFirst()) {
            String nama = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_NAMA));
            String jenis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_JENIS));
            String penyelenggara = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_PENYELENGGARA));
            String deskripsi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_DESKRIPSI));
            String tanggal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_TANGGAL));
            String waktu = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_WAKTU));
            String lokasi = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_LOKASI));
            String fotoBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_FOTO));

            // Set data ke views
            tvJudulDetail.setText(nama);
            tvJenisDetail.setText(jenis);
            tvPenyelenggaraDetail.setText("Diselenggarakan oleh: " + penyelenggara);
            tvDeskripsiDetail.setText(deskripsi);
            tvTanggalDetail.setText("Tanggal: " + tanggal);

            if (waktu != null && !waktu.isEmpty()) {
                tvWaktuDetail.setText("Waktu: " + waktu);
            } else {
                tvWaktuDetail.setVisibility(android.view.View.GONE);
            }

            if (lokasi != null && !lokasi.isEmpty()) {
                tvLokasiDetail.setText("Lokasi: " + lokasi);
            } else {
                tvLokasiDetail.setVisibility(android.view.View.GONE);
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
}