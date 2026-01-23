package com.lab.siera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.lab.siera.admin.DashboardAdminActivity;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView txtRegister, txtForgot;
    private DataManager dataManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi DataManager dan SessionManager
        dataManager = DataManager.getInstance(this);
        sessionManager = new SessionManager(this);

        // Cek apakah user sudah login sebelumnya
        if (sessionManager.isLoggedIn()) {
            redirectBasedOnUserType();
            return;
        }

        // Inisialisasi view
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.register);
        txtForgot = findViewById(R.id.txtForgot);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> {
            if (validateLoginForm()) {
                loginUser();
            }
        });

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        txtForgot.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private boolean validateLoginForm() {
        String identifier = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (identifier.isEmpty()) {
            edtEmail.setError("Email/NPM tidak boleh kosong");
            edtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Password tidak boleh kosong");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Password minimal 6 karakter");
            edtPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser() {
        String identifier = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Tampilkan loading
        showLoading(true);

        try {
            // Coba login dari DataManager (database)
            DatabaseHelper.LoginResult loginResult = dataManager.loginUser(identifier, password);

            if (loginResult.isSuccess()) {
                // Gunakan method dengan 6 parameter
                sessionManager.createLoginSession(
                        loginResult.getUserId(),
                        loginResult.getUserType(),
                        loginResult.getNama(),
                        loginResult.getEmail(),
                        loginResult.getNpm(),
                        "Tidak tersedia" // Default value untuk programStudy
                );

                String userType = loginResult.getUserType();
                String nama = loginResult.getNama();

                Toast.makeText(this, "Login berhasil! Selamat datang " + nama, Toast.LENGTH_SHORT).show();

                // Redirect berdasarkan user type
                redirectBasedOnUserType();
                finish();
            } else {
                // Coba login dengan akun demo jika database gagal
                tryDemoLogin(identifier, password);
            }
        } catch (Exception e) {
            // Jika terjadi error, coba login demo
            tryDemoLogin(identifier, password);
        }
    }

    private void tryDemoLogin(String identifier, String password) {
        showLoading(false);

        // Login demo untuk testing
        if (identifier.equals("xiera") && password.equals("123456")) {
            // Simpan session dengan data demo mahasiswa (6 parameter)
            sessionManager.createLoginSession(
                    1, // userId demo
                    "mahasiswa", // userType
                    "Xiera", // nama
                    "xiera@email.com", // email
                    "231106040000", // npm
                    "FTS - Teknik Informatika" // programStudy
            );

            Toast.makeText(this, "Login demo berhasil! Selamat datang Xiera", Toast.LENGTH_SHORT).show();
            redirectBasedOnUserType();
            finish();

        } else if (identifier.equals("admin") && password.equals("admin123")) {
            // Login admin demo (6 parameter)
            sessionManager.createLoginSession(
                    2, // userId demo
                    "admin", // userType
                    "Admin", // nama
                    "admin@siera.com", // email
                    null, // npm
                    null // programStudy
            );

            Toast.makeText(this, "Login admin demo berhasil!", Toast.LENGTH_SHORT).show();
            redirectBasedOnUserType();
            finish();

        } else {
            Toast.makeText(this, "Email/NPM atau password salah", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectBasedOnUserType() {
        if (!sessionManager.isLoggedIn()) {
            // Jika tidak login, tetap di MainActivity
            return;
        }

        // Dapatkan userType dari SessionManager (bukan dari LoginResult)
        String userType = sessionManager.getUserType();
        Intent intent;

        if ("admin".equals(userType)) {
            // Redirect ke DashboardAdminActivity untuk admin
            intent = new Intent(this, DashboardAdminActivity.class);
        } else {
            // Redirect ke DashboardActivity untuk user/mahasiswa
            intent = new Intent(this, DashboardActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Tutup MainActivity
    }

    private void showForgotPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Lupa Password");
        builder.setMessage("Fitur lupa password saat ini belum tersedia. Silakan hubungi admin di admin@siera.com");
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Salin Email", (dialog, which) -> {
            // Copy email ke clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Email Admin", "admin@siera.com");
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Email admin disalin ke clipboard", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setEnabled(false);
            btnLogin.setText("Memproses...");
            edtEmail.setEnabled(false);
            edtPassword.setEnabled(false);
        } else {
            btnLogin.setEnabled(true);
            btnLogin.setText("Login");
            edtEmail.setEnabled(true);
            edtPassword.setEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Jika sudah login, langsung redirect
        if (sessionManager.isLoggedIn()) {
            redirectBasedOnUserType();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset form jika kembali ke login
        if (!sessionManager.isLoggedIn()) {
            edtEmail.setText("");
            edtPassword.setText("");
            edtEmail.requestFocus();
        }
    }

    @Override
    public void onBackPressed() {
        // Jika sudah login, jangan kembali ke MainActivity
        if (sessionManager.isLoggedIn()) {
            // Keluar dari aplikasi
            finishAffinity();
        } else {
            // Keluar dari aplikasi jika di MainActivity dan tekan back
            finishAffinity();
        }
    }
}a