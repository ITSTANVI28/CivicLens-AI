package com.example.civiclensai.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.civiclensai.MainActivity;
import com.example.civiclensai.databinding.ActivityRegisterBinding;
import com.example.civiclensai.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);

        binding.btnRegisterSubmit.setOnClickListener(v -> {
            String name = binding.etRegName.getText() != null ? binding.etRegName.getText().toString().trim() : "";
            String email = binding.etRegEmail.getText() != null ? binding.etRegEmail.getText().toString().trim() : "";
            String pass = binding.etRegPassword.getText() != null ? binding.etRegPassword.getText().toString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all registration fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.createLoginSession("usr_" + System.currentTimeMillis(), name, email);
            Toast.makeText(this, "🎉 Registration Successful! Account Created.", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        binding.tvLoginLink.setOnClickListener(v -> finish());
    }
}
