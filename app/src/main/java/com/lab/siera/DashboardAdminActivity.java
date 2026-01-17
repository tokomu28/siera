package com.lab.siera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DashboardAdminActivity extends AppCompatActivity {

    private TextView tvTotalUsers, tvTotalPosts, tvActiveActivities, tvTotalRewards;
    private ImageView btnLogout, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        initViews();
        setupClickListeners();
        loadDashboardData();
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
    }

    private void setupClickListeners() {
        // Logout button
        btnLogout.setOnClickListener(v -> {
            // Clear session/logout logic here
            Intent intent = new Intent(DashboardAdminActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Profile button
        btnProfile.setOnClickListener(v -> {
            // Navigate to profile
            Toast.makeText(this, "Fitur profil akan segera tersedia", Toast.LENGTH_SHORT).show();
        });

        // Stats cards click listeners (jika ada CardView di layout)
        View cvUsers = findViewById(R.id.cvUsers);
        View cvPosts = findViewById(R.id.cvPosts);
        View cvActivities = findViewById(R.id.cvActivities);
        View cvRewards = findViewById(R.id.cvRewards);

        if (cvUsers != null) {
            cvUsers.setOnClickListener(v -> {
                // Intent untuk UserListActivity
                Toast.makeText(this, "Lihat daftar user", Toast.LENGTH_SHORT).show();
            });
        }

        if (cvPosts != null) {
            cvPosts.setOnClickListener(v -> {
                // Intent untuk PostListActivity
                Toast.makeText(this, "Lihat daftar post", Toast.LENGTH_SHORT).show();
            });
        }

        if (cvActivities != null) {
            cvActivities.setOnClickListener(v -> {
                // Intent untuk ActivityListActivity
                Toast.makeText(this, "Lihat daftar kegiatan", Toast.LENGTH_SHORT).show();
            });
        }

        if (cvRewards != null) {
            cvRewards.setOnClickListener(v -> {
                // Intent untuk RewardListActivity
                Toast.makeText(this, "Lihat daftar reward", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadDashboardData() {
        // In a real app, load data from database/API
        // For now, setting dummy data
        tvTotalUsers.setText("100");
        tvTotalPosts.setText("40");
        tvActiveActivities.setText("31");
        tvTotalRewards.setText("40");
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog or exit
        super.onBackPressed();
    }
}