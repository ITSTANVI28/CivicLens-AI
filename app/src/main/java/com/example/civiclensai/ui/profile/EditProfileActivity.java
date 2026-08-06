package com.example.civiclensai.ui.profile;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.civiclensai.databinding.ActivityEditProfileBinding;
import com.example.civiclensai.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.etEditName.setText(sessionManager.getUserName());
        binding.etEditEmail.setText(sessionManager.getUserEmail());

        binding.btnSaveProfile.setOnClickListener(v -> {
            String name = binding.etEditName.getText() != null ? binding.etEditName.getText().toString().trim() : "";
            String email = binding.etEditEmail.getText() != null ? binding.etEditEmail.getText().toString().trim() : "";

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please enter valid name and email.", Toast.LENGTH_SHORT).show();
                return;
            }

            sessionManager.updateUserProfile(name, email);
            Toast.makeText(this, "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
