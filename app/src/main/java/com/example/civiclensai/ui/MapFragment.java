package com.example.civiclensai.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.civiclensai.databinding.FragmentMapBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.repository.IssueRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private final Map<Marker, CivicIssue> markerIssueMap = new HashMap<>();
    private CivicIssue selectedIssue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        binding.btnViewDetails.setOnClickListener(v -> {
            if (selectedIssue != null) {
                Intent intent = new Intent(requireContext(), IssueDetailActivity.class);
                intent.putExtra("issue", selectedIssue);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Observe issues from repository
        IssueRepository.getInstance().getIssues().observe(getViewLifecycleOwner(), this::renderMapMarkers);
    }

    private void renderMapMarkers(List<CivicIssue> issues) {
        if (googleMap == null || issues == null) return;

        googleMap.clear();
        markerIssueMap.clear();

        LatLng defaultCenter = new LatLng(12.9716, 77.5946); // Center on Bangalore / city center

        for (CivicIssue issue : issues) {
            LatLng pos = new LatLng(issue.getLatitude(), issue.getLongitude());

            float hue;
            switch (issue.getSeverity()) {
                case CRITICAL: hue = BitmapDescriptorFactory.HUE_RED; break;
                case HIGH: hue = BitmapDescriptorFactory.HUE_ORANGE; break;
                case MEDIUM: hue = BitmapDescriptorFactory.HUE_YELLOW; break;
                case LOW: default: hue = BitmapDescriptorFactory.HUE_GREEN; break;
            }

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(issue.getCategory().getEmoji() + " " + issue.getTitle())
                    .snippet(issue.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));

            if (marker != null) {
                markerIssueMap.put(marker, issue);
            }
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 13f));

        googleMap.setOnMarkerClickListener(marker -> {
            CivicIssue issue = markerIssueMap.get(marker);
            if (issue != null) {
                showBottomSheet(issue);
            }
            return false;
        });
    }

    private void showBottomSheet(CivicIssue issue) {
        this.selectedIssue = issue;
        binding.bottomSheetCard.setVisibility(View.VISIBLE);
        binding.sheetTitle.setText(issue.getTitle());
        binding.sheetAddress.setText("📍 " + issue.getAddress());
        binding.sheetCategoryBadge.setText(issue.getCategory().getEmoji() + " " + issue.getCategory().getDisplayName());

        try {
            binding.sheetSeverityBadge.setBackgroundColor(Color.parseColor(issue.getSeverity().getHexColor()));
        } catch (Exception ignored) {}

        binding.sheetSeverityBadge.setText(issue.getSeverity().getLabel() + " SEVERITY");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        if (binding != null) binding.mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.mapView.onDestroy();
        binding = null;
        super.onDestroyView();
    }
}
