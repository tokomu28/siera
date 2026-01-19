package com.lab.siera.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lab.siera.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManajemenKegiatanActivity extends AppCompatActivity {

    private LinearLayout containerKegiatan;
    private Button btnTambahKegiatan;
    private List<Kegiatan> kegiatanList = new ArrayList<>();
    private int editingPosition = -1; // -1 means adding new, >=0 means editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manajemen_kegiatan);

        // Initialize views
        containerKegiatan = findViewById(R.id.containerKegiatan);
        btnTambahKegiatan = findViewById(R.id.btnTambahKegiatan);

        // Setup bottom navigation
        setupBottomNavigation();

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Add sample data
        kegiatanList.add(new Kegiatan("Workshop Android Development", "Workshop", "FTS", "15 Jan 2025"));
        kegiatanList.add(new Kegiatan("Seminar Revolusi", "Seminar", "FTS", "25 Jan 2025"));

        // Setup click listeners for existing items
        setupItemClickListeners();

        // Add button click listener
        btnTambahKegiatan.setOnClickListener(v -> showTambahKegiatanDialog());
    }

    private void setupItemClickListeners() {
        // Item 1
        findViewById(R.id.btnEdit1).setOnClickListener(v -> showEditKegiatanDialog(0));
        findViewById(R.id.btnDelete1).setOnClickListener(v -> showDeleteDialog(0));

        // Item 2
        findViewById(R.id.btnEdit2).setOnClickListener(v -> showEditKegiatanDialog(1));
        findViewById(R.id.btnDelete2).setOnClickListener(v -> showDeleteDialog(1));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavBar);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_admin_home) {
                // Navigate back to Dashboard
                finish();
                return true;
            } else if (itemId == R.id.nav_admin_users) {
                // Already in Manajemen Kegiatan (this activity)
                // You might want to create a separate UserManagementActivity
                Toast.makeText(this, "Manajemen User akan segera tersedia", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_admin_activities) {
                // Already in activities management
                return true;
            } else if (itemId == R.id.nav_admin_profile) {
                // Navigate to more/settings
                Toast.makeText(this, "Menu More akan segera tersedia", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // Set activities as selected
        bottomNav.setSelectedItemId(R.id.nav_admin_activities);
    }

    private void showTambahKegiatanDialog() {
        editingPosition = -1;
        showKegiatanDialog(null);
    }

    private void showEditKegiatanDialog(int position) {
        editingPosition = position;
        showKegiatanDialog(kegiatanList.get(position));
    }

    private void showKegiatanDialog(Kegiatan kegiatan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_form_kegiatan, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize dialog views
        EditText etNamaKegiatan = dialogView.findViewById(R.id.etNamaKegiatan);
        Spinner spinnerJenis = dialogView.findViewById(R.id.spinnerJenis);
        EditText etPenyelenggara = dialogView.findViewById(R.id.etPenyelenggara);
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
            String hari = etHari.getText().toString().trim();
            String bulan = etBulan.getText().toString().trim();
            String tahun = etTahun.getText().toString().trim();

            // Validation
            if (nama.isEmpty() || penyelenggara.isEmpty() ||
                    hari.isEmpty() || bulan.isEmpty() || tahun.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            String tanggal = hari + " " + bulan + " " + tahun;

            if (editingPosition == -1) {
                // Add new kegiatan
                Kegiatan newKegiatan = new Kegiatan(nama, jenis, penyelenggara, tanggal);
                kegiatanList.add(newKegiatan);
                addKegiatanToUI(newKegiatan, kegiatanList.size() - 1);
                Toast.makeText(this, "Kegiatan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
            } else {
                // Update existing kegiatan
                Kegiatan updatedKegiatan = new Kegiatan(nama, jenis, penyelenggara, tanggal);
                kegiatanList.set(editingPosition, updatedKegiatan);
                updateKegiatanInUI(updatedKegiatan, editingPosition);
                Toast.makeText(this, "Kegiatan berhasil diupdate", Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(this)
                .setTitle("Hapus Kegiatan")
                .setMessage("Apakah Anda yakin ingin menghapus kegiatan ini?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    kegiatanList.remove(position);
                    removeKegiatanFromUI(position);
                    Toast.makeText(this, "Kegiatan berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void addKegiatanToUI(Kegiatan kegiatan, int position) {
        // In a real app, you would use RecyclerView
        // For now, just update the existing views or show toast
        Toast.makeText(this, "Added: " + kegiatan.getNama(), Toast.LENGTH_SHORT).show();
    }

    private void updateKegiatanInUI(Kegiatan kegiatan, int position) {
        // Update the specific item based on position
        switch (position) {
            case 0:
                ((TextView) findViewById(R.id.tvNamaKegiatan1)).setText(kegiatan.getNama());
                ((TextView) findViewById(R.id.tvJenis1)).setText(kegiatan.getJenis());
                ((TextView) findViewById(R.id.tvPenyelenggara1)).setText(kegiatan.getPenyelenggara());
                ((TextView) findViewById(R.id.tvTanggal1)).setText(kegiatan.getTanggal());
                break;
            case 1:
                ((TextView) findViewById(R.id.tvNamaKegiatan2)).setText(kegiatan.getNama());
                ((TextView) findViewById(R.id.tvJenis2)).setText(kegiatan.getJenis());
                ((TextView) findViewById(R.id.tvPenyelenggara2)).setText(kegiatan.getPenyelenggara());
                ((TextView) findViewById(R.id.tvTanggal2)).setText(kegiatan.getTanggal());
                break;
        }
    }

    private void removeKegiatanFromUI(int position) {
        // Remove the specific item based on position
        switch (position) {
            case 0:
                // In a real app, you would remove from RecyclerView
                Toast.makeText(this, "Item 1 dihapus", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "Item 2 dihapus", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Model class for Kegiatan
    class Kegiatan {
        private String nama;
        private String jenis;
        private String penyelenggara;
        private String tanggal;

        public Kegiatan(String nama, String jenis, String penyelenggara, String tanggal) {
            this.nama = nama;
            this.jenis = jenis;
            this.penyelenggara = penyelenggara;
            this.tanggal = tanggal;
        }

        public String getNama() {
            return nama;
        }

        public String getJenis() {
            return jenis;
        }

        public String getPenyelenggara() {
            return penyelenggara;
        }

        public String getTanggal() {
            return tanggal;
        }
    }
}