package com.lab.siera.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lab.siera.R;
import com.lab.siera.admin.DashboardAdminActivity;
import com.lab.siera.admin.ManajemenKegiatanActivity;
import com.lab.siera.admin.UserActivity;

public class ProfileActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ImageView btnBack, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadProfileData();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottomNavBar);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, com.lab.siera.MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });

    }

    private void setupBottomNavigation() {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Jika sudah di halaman yang dituju, tidak perlu intent
            if (itemId == R.id.nav_admin_profile) {
                return true;
            }

            try {
                Intent intent = null;

                if (itemId == R.id.nav_admin_home) {
                    intent = new Intent(ProfileActivity.this, DashboardAdminActivity.class);
                } else if (itemId == R.id.nav_admin_activities) {
                    intent = new Intent(ProfileActivity.this, ManajemenKegiatanActivity.class);
                } else if (itemId == R.id.nav_admin_users) {
                    intent = new Intent(ProfileActivity.this, UserActivity.class);
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

        // Set profile as selected
        bottomNav.setSelectedItemId(R.id.nav_admin_profile);
    }

    private void loadProfileData() {
        // Load profile data from shared preferences or API
        TextView tvName = findViewById(R.id.tvName);
        //TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvRole = findViewById(R.id.tvRole);

        // Set dummy data for now
        if (tvName != null) tvName.setText("Admin Fakultas");
        //if (tvEmail != null) tvEmail.setText("admin@uika-bogor.ac.id");
        if (tvRole != null) tvRole.setText("Administrator");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_admin_profile);
        }
    }
}