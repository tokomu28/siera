package com.lab.siera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText edtNama, edtEmail, edtNpm, edtPassword;
    Button btnSignUp;
    CheckBox cbAgreement;
    TextView txtMasuk;
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inisialisasi DataManager dengan context
        dataManager = DataManager.getInstance(this);

        edtNama = findViewById(R.id.edtNama);
        edtEmail = findViewById(R.id.edtEmail);
        edtNpm = findViewById(R.id.edtNpm);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        cbAgreement = findViewById(R.id.cbAgreement);
        txtMasuk = findViewById(R.id.login);

        btnSignUp.setOnClickListener(v -> {
            if (validateForm()) {
                registerUser();
            }
        });

        txtMasuk.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });
    }

    private boolean validateForm() {
        String nama = edtNama.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String npm = edtNpm.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (nama.isEmpty()) {
            edtNama.setError("Nama tidak boleh kosong");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email tidak valid");
            return false;
        }
        if (npm.isEmpty() || npm.length() < 8) {
            edtNpm.setError("NPM minimal 8 karakter");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            edtPassword.setError("Password minimal 6 karakter");
            return false;
        }
        if (!cbAgreement.isChecked()) {
            Toast.makeText(this, "Anda harus menyetujui persyaratan", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerUser() {
        String nama = edtNama.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String npm = edtNpm.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        boolean success = dataManager.registerUser(nama, email, npm, password);

        if (success) {
            Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();

            // Kembali ke login
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show();
        }
    }
}