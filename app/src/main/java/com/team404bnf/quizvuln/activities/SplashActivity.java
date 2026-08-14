package com.team404bnf.quizvuln.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import com.team404bnf.quizvuln.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1700; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Fade-in animation for overlay (or for future elements)
        View fadeOverlay = findViewById(R.id.fadeOverlay);
        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeOverlay.startAnimation(fadeIn);

        // Move to Dashboard after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}
