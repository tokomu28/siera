package com.lab.siera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.lab.siera.admin.DashboardAdminActivity;

import com.lab.siera.admin.DashboardAdminActivity;

public class MainActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView txtRegister, txtForgot;
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi DataManager
        dataManager = DataManager.getInstance(this);

        // Inisialisasi view
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.register);
        txtForgot = findViewById(R.id.txtForgot);

        btnLogin.setOnClickListener(v -> {
            if (validateLoginForm()) {
                loginUser();
            }
        });

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        txtForgot.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur lupa password belum tersedia", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateLoginForm() {
        String identifier = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (identifier.isEmpty()) {
            edtEmail.setError("Email/NPM tidak boleh kosong");
            return false;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Password tidak boleh kosong");
            return false;
        }
        return true;
    }

    private void loginUser() {
        String identifier = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        DatabaseHelper.LoginResult loginResult = dataManager.loginUser(identifier, password);

        if (loginResult.isSuccess()) {
            String userType = loginResult.getUserType();
            String nama = loginResult.getNama();

            Toast.makeText(this, "Login berhasil! Selamat datang " + nama, Toast.LENGTH_SHORT).show();

            // Redirect berdasarkan user type
            if (userType != null && userType.equals("admin")) {
                // Redirect ke Dashboard Admin
                Intent intent = new Intent(this, DashboardAdminActivity.class);
                intent.putExtra("USER_ID", loginResult.getUserId());
                intent.putExtra("NAMA", nama);
                intent.putExtra("EMAIL", loginResult.getEmail());
                intent.putExtra("NPM", loginResult.getNpm());
                startActivity(intent);
            } else {
                // Default: Redirect ke Dashboard Mahasiswa
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("USER_ID", loginResult.getUserId());
                intent.putExtra("NAMA", nama);
                intent.putExtra("EMAIL", loginResult.getEmail());
                intent.putExtra("NPM", loginResult.getNpm());
                intent.putExtra("USER_TYPE", "mahasiswa");
                startActivity(intent);
            }
            finish();
        } else {
            Toast.makeText(this, "Email/NPM atau password salah", Toast.LENGTH_SHORT).show();
        }
    }
}