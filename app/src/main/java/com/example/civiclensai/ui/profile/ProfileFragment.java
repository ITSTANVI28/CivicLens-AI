package com.example.civiclensai.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.civiclensai.databinding.FragmentProfileBinding;
import com.example.civiclensai.ui.auth.LoginActivity;
import com.example.civiclensai.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        renderProfileData();

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditProfileActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        renderProfileData();
    }

    private void renderProfileData() {
        if (binding == null || sessionManager == null) return;
        binding.tvProfileName.setText(sessionManager.getUserName());
        binding.tvProfileEmail.setText(sessionManager.getUserEmail());
        binding.tvProfileKarma.setText("🏅 Civic Karma: " + sessionManager.getKarmaPoints() + " pts");
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
