package com.example.civiclensai.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.civiclensai.databinding.FragmentLeaderboardBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.repository.IssueRepository;
import com.example.civiclensai.utils.CivicAnalyticsEngine;
import com.example.civiclensai.utils.CrewRouteOptimizer;
import com.example.civiclensai.utils.GeoLocationResolver;
import com.example.civiclensai.utils.SessionManager;

import java.util.List;

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

        binding.btnOptimizeCrewRoute.setOnClickListener(v -> runTruckRouteOptimizer());
        binding.btnExportGeoJson.setOnClickListener(v -> exportCityGeoJson());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLeaderboardStats();
    }

    private void updateLeaderboardStats() {
        if (binding == null || sessionManager == null) return;
        binding.tvUserScore.setText(sessionManager.getKarmaPoints() + " PTS");

        List<CivicIssue> issues = IssueRepository.getInstance().getIssues().getValue();
        if (issues != null) {
            int kothrudScore = CivicAnalyticsEngine.computeWardHealthScore(issues, "Kothrud");
            int shivajiScore = CivicAnalyticsEngine.computeWardHealthScore(issues, "Shivajinagar");
            int vimanScore = CivicAnalyticsEngine.computeWardHealthScore(issues, "Viman");

            binding.tvWardKothrud.setText("📍 Kothrud Ward: " + kothrudScore + "/100 (Healthy)");
            binding.tvWardShivajinagar.setText("📍 Shivajinagar Ward: " + shivajiScore + "/100 (Routine Maintenance)");
            binding.tvWardVimanNagar.setText("📍 Viman Nagar Ward: " + vimanScore + "/100 (Good Infrastructure)");
        }
    }

    private void runTruckRouteOptimizer() {
        List<CivicIssue> issues = IssueRepository.getInstance().getIssues().getValue();
        CrewRouteOptimizer.OptimizedRouteResult result = CrewRouteOptimizer.computeOptimalCrewRoute(
                issues, GeoLocationResolver.PUNE_LAT, GeoLocationResolver.PUNE_LNG);

        new AlertDialog.Builder(requireContext())
                .setTitle("🚒 Municipal Crew Route Optimizer")
                .setMessage(result.getSummaryText() + "\n\nStops Sequenced:\n" + formatStopsList(result.orderedRoute))
                .setPositiveButton("Dispatch Truck", (d, w) -> Toast.makeText(requireContext(), "Truck Dispatched on Optimal Route!", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Close", null)
                .show();
    }

    private String formatStopsList(List<CivicIssue> route) {
        if (route == null || route.isEmpty()) return "No open stops required today!";
        StringBuilder sb = new StringBuilder();
        int stopNum = 1;
        for (CivicIssue issue : route) {
            sb.append(stopNum++).append(". ").append(issue.getTitle()).append(" (").append(issue.getCategory().getDisplayName()).append(")\n");
            if (stopNum > 5) {
                sb.append("... + ").append(route.size() - 5).append(" additional stops");
                break;
            }
        }
        return sb.toString();
    }

    private void exportCityGeoJson() {
        List<CivicIssue> issues = IssueRepository.getInstance().getIssues().getValue();
        String geoJson = CivicAnalyticsEngine.exportCityDataToGeoJson(issues);

        new AlertDialog.Builder(requireContext())
                .setTitle("📊 Open Data GeoJSON Export")
                .setMessage("Generated GeoJSON Payload (" + geoJson.length() + " bytes):\n\n" + (geoJson.length() > 300 ? geoJson.substring(0, 300) + "..." : geoJson))
                .setPositiveButton("Copy / Share", (d, w) -> Toast.makeText(requireContext(), "GeoJSON Payload Ready for Urban Planners!", Toast.LENGTH_SHORT).show())
                .setNegativeButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}

