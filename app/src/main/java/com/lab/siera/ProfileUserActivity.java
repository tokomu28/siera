package com.lab.siera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileUserActivity extends AppCompatActivity {

    // SharedPreferences keys
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NPM = "npm";
    private static final String KEY_PROGRAM_STUDY = "programStudy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        // Inisialisasi waktu saat ini
        updateCurrentTime();

        // Setup data profil dari SharedPreferences atau intent
        setupProfileData();

        // Setup klik untuk card internship
        CardView cvInternship = findViewById(R.id.cv_internship);
        cvInternship.setOnClickListener(v -> {
            Toast.makeText(this, "Membuka detail Magang Berdampak", Toast.LENGTH_SHORT).show();
            // Tambahkan intent untuk membuka detail internship
        });

        // Setup klik untuk tombol logout
        CardView cvLogout = findViewById(R.id.cv_logout);
        cvLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void updateCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH.mm", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        TextView tvTime = findViewById(R.id.tv_time);
        tvTime.setText(currentTime);
    }

    private void setupProfileData() {
        // Ambil data dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        String username = prefs.getString(KEY_USERNAME, "Xiera");
        String npm = prefs.getString(KEY_NPM, "231106040000");
        String programStudy = prefs.getString(KEY_PROGRAM_STUDY, "FTS - Teknik Informatika");

        // Atau ambil dari intent jika ada
        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("USERNAME") != null ?
                    intent.getStringExtra("USERNAME") : username;
            npm = intent.getStringExtra("NPM") != null ?
                    intent.getStringExtra("NPM") : npm;
            programStudy = intent.getStringExtra("PROGRAM_STUDY") != null ?
                    intent.getStringExtra("PROGRAM_STUDY") : programStudy;
        }

        // Set data ke TextView
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvNpm = findViewById(R.id.tv_npm);
        TextView tvProgramStudy = findViewById(R.id.tv_program_study);

        tvName.setText(username);
        tvNpm.setText("NPM - " + npm);
        tvProgramStudy.setText(programStudy);
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout dari akun Anda?")
                .setPositiveButton("Ya", (dialog, which) -> performLogout())
                .setNegativeButton("Tidak", null)
                .setCancelable(true)
                .show();
    }

    private void performLogout() {
        // 1. Hapus status login dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_NPM);
        editor.remove(KEY_PROGRAM_STUDY);
        editor.apply();

        // 2. Tampilkan pesan logout
        Toast.makeText(this, "Berhasil logout", Toast.LENGTH_SHORT).show();

        // 3. Redirect ke MainActivity (Login)
        Intent intent = new Intent(ProfileUserActivity.this, MainActivity.class);

        // Clear activity stack agar tidak bisa kembali dengan tombol back
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish(); // Tutup activity saat ini
    }

    @Override
    public void onBackPressed() {
        // Optional: Tampilkan konfirmasi saat tombol back ditekan
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Keluar Aplikasi")
                .setMessage("Apakah Anda ingin keluar dari aplikasi?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    super.onBackPressed();
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }
}