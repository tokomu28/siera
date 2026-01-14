package com.lab.siera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private TextView txtSubtitle;
    private final Handler handler = new Handler();
    private int index = 0;

    // Teks muncul bertahap (seperti animasi pertama)
    private final String[] texts = {
            "Sistem Informasi",
            "Sistem Informasi Event",
            "Sistem Informasi Event, Riset",
            "Sistem Informasi Event, Riset & Akademik"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        txtSubtitle = findViewById(R.id.txtSubtitle);

        startTextAnimation();

        // Pindah ke LoginActivity setelah semua teks tampil
        handler.postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 4200);
    }

    private void startTextAnimation() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_fade);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                txtSubtitle.setText(texts[index]);
                txtSubtitle.startAnimation(anim);
                index++;

                if (index < texts.length) {
                    handler.postDelayed(this, 900);
                }
            }
        }, 300);
    }
}
