package com.example.civiclensai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import com.example.civiclensai.MainActivity;
import com.example.civiclensai.databinding.ActivitySplashBinding;
import com.example.civiclensai.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final long SPLASH_DELAY_MS = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize Material 3 Android 12+ SplashScreen API
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Perform Entrance Animation
        binding.cardSplashLogo.setAlpha(0.2f);
        binding.cardSplashLogo.setScaleX(0.7f);
        binding.cardSplashLogo.setScaleY(0.7f);

        binding.cardSplashLogo.animate()
                .alpha(1.0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        binding.tvSplashTitle.setAlpha(0f);
        binding.tvSplashTitle.animate()
                .alpha(1.0f)
                .setStartDelay(300)
                .setDuration(800)
                .start();

        binding.tvSplashSubtitle.setAlpha(0f);
        binding.tvSplashSubtitle.animate()
                .alpha(1.0f)
                .setStartDelay(500)
                .setDuration(800)
                .start();

        // Delay and route session
        new Handler(Looper.getMainLooper()).postDelayed(this::routeToNextScreen, SPLASH_DELAY_MS);
    }

    private void routeToNextScreen() {
        if (isFinishing() || isDestroyed()) return;

        SessionManager sessionManager = new SessionManager(this);
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
