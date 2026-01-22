package com.lab.siera.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lab.siera.R;
import com.lab.siera.admin.ProfileActivity;

public class UserActivity extends AppCompatActivity {

    private EditText etSearch;
    private CheckBox cbStatus1, cbStatus2;
    private ImageView btnBack, btnProfile, btnLogout, btnFilter;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Inisialisasi views
        initViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        cbStatus1 = findViewById(R.id.cbStatus1);
        cbStatus2 = findViewById(R.id.cbStatus2);
        btnBack = findViewById(R.id.btnBack);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnFilter = findViewById(R.id.btnFilter);
        bottomNav = findViewById(R.id.bottomNavBar);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Profile button di header
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Logout button di header
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, com.lab.siera.MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });

        // Filter button (Q button)
        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Filter diterapkan", Toast.LENGTH_SHORT).show();
            applyFilter();
        });

        // Checkbox status perubahan
        cbStatus1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatus(1, isChecked);
        });

        cbStatus2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatus(2, isChecked);
        });

        // Item pendaftaran klik
        findViewById(R.id.item_pendaftaran_1).setOnClickListener(v -> {
            showDetailPendaftaran(1);
        });

        findViewById(R.id.item_pendaftaran_2).setOnClickListener(v -> {
            showDetailPendaftaran(2);
        });
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
                    finish(); // TAMBAHKAN INI!
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return true;
        });

        // Set users as selected
        bottomNav.setSelectedItemId(R.id.nav_admin_users);
    }

    private void applyFilter() {
        String searchText = etSearch.getText().toString().trim();

        if (searchText.isEmpty()) {
            // Tampilkan semua data
            findViewById(R.id.item_pendaftaran_1).setVisibility(View.VISIBLE);
            findViewById(R.id.item_pendaftaran_2).setVisibility(View.VISIBLE);
            Toast.makeText(this, "Menampilkan semua data", Toast.LENGTH_SHORT).show();
        } else {
            // Filter berdasarkan pencarian
            filterData(searchText);
        }
    }

    private void filterData(String keyword) {
        boolean showItem1 = false;
        boolean showItem2 = false;

        // Cek item 1
        String nama1 = ((android.widget.TextView) findViewById(R.id.tvNamaPendaftar1)).getText().toString();
        String kegiatan1 = ((android.widget.TextView) findViewById(R.id.tvNamaKegiatan1)).getText().toString();

        if (nama1.toLowerCase().contains(keyword.toLowerCase()) ||
                kegiatan1.toLowerCase().contains(keyword.toLowerCase())) {
            showItem1 = true;
        }

        // Cek item 2
        String nama2 = ((android.widget.TextView) findViewById(R.id.tvNamaPendaftar2)).getText().toString();
        String kegiatan2 = ((android.widget.TextView) findViewById(R.id.tvNamaKegiatan2)).getText().toString();

        if (nama2.toLowerCase().contains(keyword.toLowerCase()) ||
                kegiatan2.toLowerCase().contains(keyword.toLowerCase())) {
            showItem2 = true;
        }

        // Atur visibility
        findViewById(R.id.item_pendaftaran_1).setVisibility(showItem1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.item_pendaftaran_2).setVisibility(showItem2 ? View.VISIBLE : View.GONE);

        if (!showItem1 && !showItem2) {
            Toast.makeText(this, "Tidak ditemukan data dengan kata kunci: " + keyword, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(int itemId, boolean isApproved) {
        String message = isApproved ? "Pendaftaran disetujui" : "Pendaftaran ditolak";
        Toast.makeText(this, "Item " + itemId + ": " + message, Toast.LENGTH_SHORT).show();

        // Di sini Anda bisa menambahkan logic untuk update status ke database
    }

    private void showDetailPendaftaran(int itemId) {
        String nama = "";
        String kegiatan = "";

        if (itemId == 1) {
            nama = ((android.widget.TextView) findViewById(R.id.tvNamaPendaftar1)).getText().toString();
            kegiatan = ((android.widget.TextView) findViewById(R.id.tvNamaKegiatan1)).getText().toString();
        } else if (itemId == 2) {
            nama = ((android.widget.TextView) findViewById(R.id.tvNamaPendaftar2)).getText().toString();
            kegiatan = ((android.widget.TextView) findViewById(R.id.tvNamaKegiatan2)).getText().toString();
        }

        Toast.makeText(this, "Detail: " + nama + " - " + kegiatan, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update bottom nav selection
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_admin_users);
        }
    }
}