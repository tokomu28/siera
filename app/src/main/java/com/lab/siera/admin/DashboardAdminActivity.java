package com.lab.siera.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lab.siera.MainActivity;
import com.lab.siera.R;
import com.lab.siera.admin.ProfileActivity; // Pastikan import ini benar

public class DashboardAdminActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvTotalPosts, tvActiveActivities, tvTotalRewards;
    private ImageView btnLogout, btnProfile;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
        //loadDashboardData();
    }

    private void initViews() {
        // Stats TextViews
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalPosts = findViewById(R.id.tvTotalPosts);
        tvActiveActivities = findViewById(R.id.tvActiveActivities);
        tvTotalRewards = findViewById(R.id.tvTotalRewards);

        // Action Buttons
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);

        // Bottom Navigation
        bottomNav = findViewById(R.id.bottomNavBar);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Jika sudah di halaman yang dituju, tidak perlu intent
            if (itemId == R.id.nav_admin_home) {
                return true;
            }

            try {
                Intent intent = null;

                if (itemId == R.id.nav_admin_activities) {
                    intent = new Intent(DashboardAdminActivity.this, ManajemenKegiatanActivity.class);
                } else if (itemId == R.id.nav_admin_users) {
                    intent = new Intent(DashboardAdminActivity.this, UserActivity.class);
                } else if (itemId == R.id.nav_admin_profile) {
                    intent = new Intent(DashboardAdminActivity.this, ProfileActivity.class);
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

        // Set home sebagai selected
        bottomNav.setSelectedItemId(R.id.nav_admin_home);
    }

    private void setupClickListeners() {
        // Logout button
        btnLogout.setOnClickListener(v -> {
            // Clear session/logout logic here
            Intent intent = new Intent(DashboardAdminActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity(); // Clear all activities
        });

        // Profile button di header
        btnProfile.setOnClickListener(v -> {
            // Navigate to profile
            Intent intent = new Intent(DashboardAdminActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Stats cards click listeners (jika ada CardView di layout)
        View cvUsers = findViewById(R.id.cvUsers);
        View cvPosts = findViewById(R.id.cvPosts);
        View cvActivities = findViewById(R.id.cvActivities);
        View cvRewards = findViewById(R.id.cvRewards);

//        if (cvUsers != null) {
//            cvUsers.setOnClickListener(v -> {
//                // Intent untuk UserActivity
//                Intent intent = new Intent(DashboardAdminActivity.this, UserActivity.class);
//                startActivity(intent);
//            });
//        }
//
//        if (cvPosts != null) {
//            cvPosts.setOnClickListener(v -> {
//                Toast.makeText(this, "Fitur post akan segera tersedia", Toast.LENGTH_SHORT).show();
//            });
//        }
//
//        if (cvActivities != null) {
//            cvActivities.setOnClickListener(v -> {
//                // Intent untuk ManajemenKegiatanActivity
//                Intent intent = new Intent(DashboardAdminActivity.this, ManajemenKegiatanActivity.class);
//                startActivity(intent);
//            });
//        }
//
//        if (cvRewards != null) {
//            cvRewards.setOnClickListener(v -> {
//                Toast.makeText(this, "Fitur reward akan segera tersedia", Toast.LENGTH_SHORT).show();
//            });
//        }
//    }

    //private void loadDashboardData() {
        // In a real app, load data from database/API
        // For now, setting dummy data
        tvTotalUsers.setText("100");
        tvTotalPosts.setText("40");
        tvActiveActivities.setText("31");
        tvTotalRewards.setText("40");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset bottom nav selection to home when returning to this activity
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_admin_home);
        }
    }

    @Override
    public void onBackPressed() {
        // Exit app confirmation
        new android.app.AlertDialog.Builder(this)
                .setTitle("Keluar")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> finishAffinity())
                .setNegativeButton("Tidak", null)
                .show();
    }
}