package com.lab.siera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    TextView txtRegister;
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi DataManager dengan context
        dataManager = DataManager.getInstance(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegister = findViewById(R.id.register);

        // Auto-fill email jika dari register
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EMAIL")) {
            edtEmail.setText(intent.getStringExtra("EMAIL"));
        }

        btnLogin.setOnClickListener(v -> {
            if (validateLoginForm()) {
                checkLogin();
            }
        });

        txtRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private boolean validateLoginForm() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Email tidak boleh kosong");
            return false;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Password tidak boleh kosong");
            return false;
        }
        return true;
    }

    private void checkLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (dataManager.validateLogin(email, password)) {
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();

            // Ambil data user
            DataManager.User user = dataManager.getUserByEmail(email);

            // Pindah ke HomeActivity dengan membawa data user
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("USER_NAME", user.getNama());
            intent.putExtra("USER_EMAIL", user.getEmail());
            intent.putExtra("USER_NPM", user.getNpm());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show();
        }
    }
}