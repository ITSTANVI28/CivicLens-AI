package com.example.civiclensai.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.civiclensai.R;
import com.example.civiclensai.databinding.FragmentMapBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.repository.IssueRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private final Map<Marker, CivicIssue> markerIssueMap = new HashMap<>();
    private List<CivicIssue> allIssues = new ArrayList<>();
    private IssueCategory currentFilter = null;
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

        try {
            binding.mapView.onCreate(savedInstanceState);
            binding.mapView.getMapAsync(this);
        } catch (Exception e) {
            binding.mapView.setVisibility(View.GONE);
            binding.layoutFallbackMap.setVisibility(View.VISIBLE);
        }

        binding.btnViewDetails.setOnClickListener(v -> {
            if (selectedIssue != null) {
                Intent intent = new Intent(requireContext(), IssueDetailActivity.class);
                intent.putExtra("issue", selectedIssue);
                startActivity(intent);
            }
        });

        // Setup Map Filters
        binding.mapChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                currentFilter = null;
            } else if (checkedIds.contains(R.id.chipPotholes)) {
                currentFilter = IssueCategory.POTHOLE;
            } else if (checkedIds.contains(R.id.chipGarbage)) {
                currentFilter = IssueCategory.GARBAGE;
            } else if (checkedIds.contains(R.id.chipWater)) {
                currentFilter = IssueCategory.WATER_LEAK;
            } else if (checkedIds.contains(R.id.chipLights)) {
                currentFilter = IssueCategory.STREETLIGHT;
            }
            renderMapMarkers(allIssues);
        });

        // Observe issues list
        IssueRepository.getInstance().getIssues().observe(getViewLifecycleOwner(), issues -> {
            this.allIssues = issues != null ? issues : new ArrayList<>();
            renderMapMarkers(allIssues);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        renderMapMarkers(allIssues);
    }

    private void renderMapMarkers(List<CivicIssue> issues) {
        if (binding == null || issues == null) return;

        List<CivicIssue> filtered = new ArrayList<>();
        for (CivicIssue issue : issues) {
            if (currentFilter == null || issue.getCategory() == currentFilter) {
                filtered.add(issue);
            }
        }

        // Render on Google Map SDK if ready
        if (googleMap != null) {
            googleMap.clear();
            markerIssueMap.clear();
            LatLng defaultCenter = new LatLng(12.9716, 77.5946);

            for (CivicIssue issue : filtered) {
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

        // Render pins on Interactive Overlay container
        renderOverlayPinButtons(filtered);
    }

    private void renderOverlayPinButtons(List<CivicIssue> filtered) {
        if (binding == null) return;
        binding.layoutPinsContainer.removeAllViews();

        for (CivicIssue issue : filtered) {
            Button pinBtn = new Button(requireContext());
            pinBtn.setText(issue.getCategory().getEmoji() + " " + issue.getSeverity().getLabel());
            try {
                pinBtn.setBackgroundColor(Color.parseColor(issue.getSeverity().getHexColor()));
            } catch (Exception ignored) {}
            pinBtn.setTextColor(Color.WHITE);
            pinBtn.setTextSize(11f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            pinBtn.setLayoutParams(params);

            pinBtn.setOnClickListener(v -> showBottomSheet(issue));
            binding.layoutPinsContainer.addView(pinBtn);
        }
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
