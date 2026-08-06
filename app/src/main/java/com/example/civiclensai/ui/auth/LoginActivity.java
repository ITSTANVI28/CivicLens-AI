package com.example.civiclensai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.civiclensai.MainActivity;
import com.example.civiclensai.databinding.ActivityLoginBinding;
import com.example.civiclensai.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etLoginEmail.getText() != null ? binding.etLoginEmail.getText().toString().trim() : "";
            String pass = binding.etLoginPassword.getText() != null ? binding.etLoginPassword.getText().toString().trim() : "";

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.createLoginSession("usr_" + System.currentTimeMillis(), "Alex Citizen", email);
            Toast.makeText(this, "🎉 Welcome back to CivicLens AI!", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        });

        binding.tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        binding.tvRegisterLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
}
