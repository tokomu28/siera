package com.lab.siera.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lab.siera.DataManager;
import com.lab.siera.DatabaseHelper;
import com.lab.siera.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManajemenKegiatanActivity extends AppCompatActivity {

    private LinearLayout containerKegiatan;
    private Button btnTambahKegiatan;
    private EditText etSearch;
    private List<Kegiatan> kegiatanList = new ArrayList<>();
    private int editingPosition = -1;
    private DataManager dataManager;

    // Model class Kegiatan (diperbarui dengan semua field)
    class Kegiatan {
        private int id;
        private String nama;
        private String jenis;
        private String penyelenggara;
        private String deskripsi;
        private String tanggal;
        private String waktu;
        private String lokasi;
        private long timestamp;
        private String fotoBase64;

        public Kegiatan(int id, String nama, String jenis, String penyelenggara,
                        String deskripsi, String tanggal, String waktu,
                        String lokasi, long timestamp, String fotoBase64) {
            this.id = id;
            this.nama = nama;
            this.jenis = jenis;
            this.penyelenggara = penyelenggara;
            this.deskripsi = deskripsi;
            this.tanggal = tanggal;
            this.waktu = waktu;
            this.lokasi = lokasi;
            this.timestamp = timestamp;
            this.fotoBase64 = fotoBase64;
        }

        // Getters
        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getJenis() { return jenis; }
        public String getPenyelenggara() { return penyelenggara; }
        public String getDeskripsi() { return deskripsi; }
        public String getTanggal() { return tanggal; }
        public String getWaktu() { return waktu; }
        public String getLokasi() { return lokasi; }
        public long getTimestamp() { return timestamp; }
        public String getFotoBase64() { return fotoBase64; }

        // Setters
        public void setNama(String nama) { this.nama = nama; }
        public void setJenis(String jenis) { this.jenis = jenis; }
        public void setPenyelenggara(String penyelenggara) { this.penyelenggara = penyelenggara; }
        public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
        public void setTanggal(String tanggal) { this.tanggal = tanggal; }
        public void setWaktu(String waktu) { this.waktu = waktu; }
        public void setLokasi(String lokasi) { this.lokasi = lokasi; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manajemen_kegiatan);

        // Initialize DataManager
        dataManager = DataManager.getInstance(this);

        // Initialize views
        containerKegiatan = findViewById(R.id.containerKegiatan);
        btnTambahKegiatan = findViewById(R.id.btnTambahKegiatan);
        etSearch = findViewById(R.id.etSearch);

        // Setup bottom navigation
        setupBottomNavigation();

        // Setup click listeners for header buttons
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            Intent intent = new Intent(ManajemenKegiatanActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Intent intent = new Intent(ManajemenKegiatanActivity.this, com.lab.siera.MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup search functionality
        setupSearch();

        // Load data from database
        loadKegiatanFromDatabase();

        // Add button click listener
        btnTambahKegiatan.setOnClickListener(v -> showTambahKegiatanDialog());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKegiatan(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadKegiatanFromDatabase() {
        kegiatanList.clear();
        Cursor cursor = dataManager.getAllKegiatan();

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
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_TANGGAL_TIMESTAMP));
                String fotoBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_FOTO));

                kegiatanList.add(new Kegiatan(id, nama, jenis, penyelenggara, deskripsi,
                        tanggal, waktu, lokasi, timestamp, fotoBase64));
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Tampilkan data di UI
        displayKegiatan();
    }

    private void searchKegiatan(String keyword) {
        if (keyword.isEmpty()) {
            loadKegiatanFromDatabase();
            return;
        }

        kegiatanList.clear();
        Cursor cursor = dataManager.searchKegiatan(keyword);

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
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_TANGGAL_TIMESTAMP));
                String fotoBase64 = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEGIATAN_FOTO));

                kegiatanList.add(new Kegiatan(id, nama, jenis, penyelenggara, deskripsi,
                        tanggal, waktu, lokasi, timestamp, fotoBase64));
            } while (cursor.moveToNext());
            cursor.close();
        }

        displayKegiatan();
    }

    private void displayKegiatan() {
        // Kosongkan container terlebih dahulu
        containerKegiatan.removeAllViews();

        // Tambahkan header
        View headerView = getLayoutInflater().inflate(R.layout.item_kegiatan_header, containerKegiatan, false);
        containerKegiatan.addView(headerView);

        // Tambahkan setiap kegiatan ke UI
        for (int i = 0; i < kegiatanList.size(); i++) {
            addKegiatanItemToUI(kegiatanList.get(i), i);
        }

        // Jika tidak ada data, tampilkan pesan
        if (kegiatanList.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Tidak ada kegiatan");
            tvEmpty.setTextColor(Color.GRAY);
            tvEmpty.setTextSize(16);
            tvEmpty.setGravity(Gravity.CENTER);
            tvEmpty.setPadding(0, 50, 0, 0);
            containerKegiatan.addView(tvEmpty);
        }
    }

    private void addKegiatanItemToUI(Kegiatan kegiatan, int position) {
        // Buat layout untuk item kegiatan
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setBackgroundResource(R.drawable.bg_item_kegiatan);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 8);
        itemLayout.setLayoutParams(layoutParams);
        itemLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        // Nama Kegiatan
        TextView tvNama = new TextView(this);
        LinearLayout.LayoutParams paramsNama = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        tvNama.setLayoutParams(paramsNama);
        tvNama.setText(kegiatan.getNama());
        tvNama.setTextColor(Color.parseColor("#333333"));
        tvNama.setTextSize(14);

        // Jenis
        TextView tvJenis = new TextView(this);
        LinearLayout.LayoutParams paramsJenis = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvJenis.setLayoutParams(paramsJenis);
        tvJenis.setText(kegiatan.getJenis());
        tvJenis.setTextColor(Color.parseColor("#666666"));
        tvJenis.setTextSize(14);

        // Penyelenggara
        TextView tvPenyelenggara = new TextView(this);
        LinearLayout.LayoutParams paramsPenyelenggara = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvPenyelenggara.setLayoutParams(paramsPenyelenggara);
        tvPenyelenggara.setText(kegiatan.getPenyelenggara());
        tvPenyelenggara.setTextColor(Color.parseColor("#666666"));
        tvPenyelenggara.setTextSize(14);

        // Tanggal
        TextView tvTanggal = new TextView(this);
        LinearLayout.LayoutParams paramsTanggal = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvTanggal.setLayoutParams(paramsTanggal);
        String displayDate = kegiatan.getTanggal() + " " + kegiatan.getWaktu();
        tvTanggal.setText(displayDate);
        tvTanggal.setTextColor(Color.parseColor("#666666"));
        tvTanggal.setTextSize(14);

        // Container untuk aksi (edit dan delete)
        LinearLayout containerAksi = new LinearLayout(this);
        LinearLayout.LayoutParams paramsAksi = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        containerAksi.setLayoutParams(paramsAksi);
        containerAksi.setOrientation(LinearLayout.HORIZONTAL);
        containerAksi.setGravity(Gravity.CENTER);

        // Tombol Edit dengan ImageView
        ImageView btnEdit = new ImageView(this);
        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(
                dpToPx(30),
                dpToPx(30)
        );
        editParams.setMargins(0, 0, dpToPx(8), 0);
        btnEdit.setLayoutParams(editParams);
        btnEdit.setImageResource(R.drawable.ic_edit);
        btnEdit.setTag(position);
        btnEdit.setOnClickListener(v -> {
            int pos = (int) v.getTag();
            showEditKegiatanDialog(pos);
        });

        // Tombol Delete dengan ImageView
        ImageView btnDelete = new ImageView(this);
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                dpToPx(30),
                dpToPx(30)
        );
        btnDelete.setLayoutParams(deleteParams);
        btnDelete.setImageResource(R.drawable.ic_delete);
        btnDelete.setTag(position);
        btnDelete.setOnClickListener(v -> {
            int pos = (int) v.getTag();
            showDeleteDialog(pos);
        });

        // Tambahkan tombol ke container aksi
        containerAksi.addView(btnEdit);
        containerAksi.addView(btnDelete);

        // Tambahkan semua view ke item layout
        itemLayout.addView(tvNama);
        itemLayout.addView(tvJenis);
        itemLayout.addView(tvPenyelenggara);
        itemLayout.addView(tvTanggal);
        itemLayout.addView(containerAksi);

        // Tambahkan item layout ke container utama
        containerKegiatan.addView(itemLayout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showTambahKegiatanDialog() {
        editingPosition = -1;
        showKegiatanDialog(null);
    }

    private void showEditKegiatanDialog(int position) {
        if (position >= 0 && position < kegiatanList.size()) {
            editingPosition = position;
            showKegiatanDialog(kegiatanList.get(position));
        }
    }

    private void showKegiatanDialog(Kegiatan kegiatan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_form_kegiatan, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize dialog views - PERLU DITAMBAHKAN INPUT BARU
        EditText etNamaKegiatan = dialogView.findViewById(R.id.etNamaKegiatan);
        Spinner spinnerJenis = dialogView.findViewById(R.id.spinnerJenis);
        EditText etPenyelenggara = dialogView.findViewById(R.id.etPenyelenggara);
        EditText etDeskripsi = dialogView.findViewById(R.id.etDeskripsi); // TAMBAH INI
        EditText etLokasi = dialogView.findViewById(R.id.etLokasi); // TAMBAH INI
        EditText etWaktu = dialogView.findViewById(R.id.etWaktu); // TAMBAH INI
        EditText etHari = dialogView.findViewById(R.id.etHari);
        EditText etBulan = dialogView.findViewById(R.id.etBulan);
        EditText etTahun = dialogView.findViewById(R.id.etTahun);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnSimpan = dialogView.findViewById(R.id.btnSimpan);

        // Setup jenis kegiatan spinner
        String[] jenisArray = {"Workshop", "Seminar", "Pelatihan", "Lomba", "Lainnya"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, jenisArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenis.setAdapter(adapter);

        // Setup waktu (contoh: 09:00 - 17:00)
        etWaktu.setHint("Contoh: 09:00 - 17:00");

        // Populate data if editing
        if (kegiatan != null) {
            etNamaKegiatan.setText(kegiatan.getNama());

            // Set spinner selection
            for (int i = 0; i < jenisArray.length; i++) {
                if (jenisArray[i].equals(kegiatan.getJenis())) {
                    spinnerJenis.setSelection(i);
                    break;
                }
            }

            etPenyelenggara.setText(kegiatan.getPenyelenggara());
            etDeskripsi.setText(kegiatan.getDeskripsi());
            etLokasi.setText(kegiatan.getLokasi());
            etWaktu.setText(kegiatan.getWaktu());

            // Parse tanggal
            String[] tanggalParts = kegiatan.getTanggal().split(" ");
            if (tanggalParts.length >= 3) {
                etHari.setText(tanggalParts[0]);
                etBulan.setText(tanggalParts[1]);
                etTahun.setText(tanggalParts[2]);
            }
        }

        // Date picker for bulan field
        etBulan.setOnClickListener(v -> showDatePickerDialog(etHari, etBulan, etTahun));

        // Button listeners
        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnSimpan.setOnClickListener(v -> {
            String nama = etNamaKegiatan.getText().toString().trim();
            String jenis = spinnerJenis.getSelectedItem().toString();
            String penyelenggara = etPenyelenggara.getText().toString().trim();
            String deskripsi = etDeskripsi.getText().toString().trim();
            String lokasi = etLokasi.getText().toString().trim();
            String waktu = etWaktu.getText().toString().trim();
            String hari = etHari.getText().toString().trim();
            String bulan = etBulan.getText().toString().trim();
            String tahun = etTahun.getText().toString().trim();

            // Validation
            if (nama.isEmpty() || penyelenggara.isEmpty() || deskripsi.isEmpty() ||
                    lokasi.isEmpty() || waktu.isEmpty() ||
                    hari.isEmpty() || bulan.isEmpty() || tahun.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            String tanggal = hari + " " + bulan + " " + tahun;

            // Convert tanggal ke timestamp
            Calendar calendar = Calendar.getInstance();
            String[] bulanArray = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

            int day = Integer.parseInt(hari);
            int year = Integer.parseInt(tahun);
            int month = 0;
            for (int i = 0; i < bulanArray.length; i++) {
                if (bulan.equals(bulanArray[i])) {
                    month = i;
                    break;
                }
            }

            calendar.set(year, month, day, 0, 0, 0);
            long timestamp = calendar.getTimeInMillis();

            // Default fotoBase64 (kosong)
            String fotoBase64 = "";

            if (editingPosition == -1) {
                // Add new kegiatan to database - DIPERBAIKI dengan semua parameter
                boolean success = dataManager.tambahKegiatan(
                        nama,           // String nama
                        jenis,          // String jenis
                        penyelenggara,  // String penyelenggara
                        deskripsi,      // String deskripsi
                        tanggal,        // String tanggal
                        waktu,          // String waktu
                        lokasi,         // String lokasi
                        timestamp,      // long timestamp
                        fotoBase64      // String fotoBase64
                );

                if (success) {
                    // Reload data dari database
                    loadKegiatanFromDatabase();
                    Toast.makeText(this, "Kegiatan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Gagal menambahkan kegiatan", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Update existing kegiatan in database - DIPERBAIKI dengan semua parameter
                Kegiatan kegiatanToUpdate = kegiatanList.get(editingPosition);
                boolean success = dataManager.updateKegiatan(
                        kegiatanToUpdate.getId(),
                        nama,           // String nama
                        jenis,          // String jenis
                        penyelenggara,  // String penyelenggara
                        deskripsi,      // String deskripsi
                        tanggal,        // String tanggal
                        waktu,          // String waktu
                        lokasi,         // String lokasi
                        timestamp,      // long timestamp
                        fotoBase64      // String fotoBase64
                );

                if (success) {
                    // Reload data dari database
                    loadKegiatanFromDatabase();
                    Toast.makeText(this, "Kegiatan berhasil diupdate", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Gagal mengupdate kegiatan", Toast.LENGTH_SHORT).show();
                }
            }

            dialog.dismiss();
        });
    }

    private void showDatePickerDialog(EditText etHari, EditText etBulan, EditText etTahun) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format bulan
                    String[] bulanArray = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
                    String bulanText = bulanArray[selectedMonth];

                    etHari.setText(String.valueOf(selectedDay));
                    etBulan.setText(bulanText);
                    etTahun.setText(String.valueOf(selectedYear));
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showDeleteDialog(int position) {
        if (position < 0 || position >= kegiatanList.size()) return;

        Kegiatan kegiatan = kegiatanList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Hapus Kegiatan")
                .setMessage("Apakah Anda yakin ingin menghapus kegiatan '" + kegiatan.getNama() + "'?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    // Delete from database
                    boolean success = dataManager.deleteKegiatan(kegiatan.getId());

                    if (success) {
                        // Reload data dari database
                        loadKegiatanFromDatabase();
                        Toast.makeText(this, "Kegiatan berhasil dihapus", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Gagal menghapus kegiatan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavBar);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Jika sudah di halaman yang dituju, tidak perlu intent
            if (itemId == R.id.nav_admin_activities) {
                return true;
            }

            try {
                Intent intent = null;

                if (itemId == R.id.nav_admin_home) {
                    intent = new Intent(ManajemenKegiatanActivity.this, DashboardAdminActivity.class);
                } else if (itemId == R.id.nav_admin_users) {
                    intent = new Intent(ManajemenKegiatanActivity.this, UserActivity.class);
                } else if (itemId == R.id.nav_admin_profile) {
                    intent = new Intent(ManajemenKegiatanActivity.this, ProfileActivity.class);
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

        // Set activities as selected
        bottomNav.setSelectedItemId(R.id.nav_admin_activities);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update bottom nav selection
        if (findViewById(R.id.bottomNavBar) != null) {
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavBar);
            bottomNav.setSelectedItemId(R.id.nav_admin_activities);
        }

        // Reload data jika perlu
        loadKegiatanFromDatabase();
    }
}