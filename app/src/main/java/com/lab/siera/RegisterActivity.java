package com.lab.siera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText edtNama, edtEmail, edtNpm, edtPassword;
    Button btnSignUp;
    CheckBox cbAgreement;
    TextView txtMasuk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtNama = findViewById(R.id.edtNama);
        edtEmail = findViewById(R.id.edtEmail);
        edtNpm = findViewById(R.id.edtNpm);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        cbAgreement = findViewById(R.id.cbAgreement);
        txtMasuk = findViewById(R.id.login);

        txtMasuk.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, MainActivity.class))
        );
    }
}
