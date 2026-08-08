package com.example.civiclensai.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.civiclensai.databinding.FragmentLeaderboardBinding;
import com.example.civiclensai.utils.SessionManager;

public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        updateLeaderboardStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLeaderboardStats();
    }

    private void updateLeaderboardStats() {
        if (binding == null || sessionManager == null) return;
        binding.tvUserScore.setText(sessionManager.getKarmaPoints() + " PTS");
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
