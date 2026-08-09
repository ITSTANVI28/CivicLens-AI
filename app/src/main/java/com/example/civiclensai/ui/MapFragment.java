package com.example.civiclensai.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.civiclensai.databinding.FragmentMapBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.repository.IssueRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private CivicIssue selectedIssue;
    private List<CivicIssue> currentIssues = new ArrayList<>();
    private IssueCategory currentCategoryFilter = null; // null means ALL
    private GeoPoint myLocationPoint = null;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean fineGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    fetchRealTimeLocationAndCenter(true);
                } else {
                    Toast.makeText(requireContext(), "Location permission denied. Cannot show self location.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // High-performance OpenStreetMap CartoDB Voyager tile source (CDN served, 0 blocks)
    public static final OnlineTileSourceBase CARTO_VOYAGER = new XYTileSource(
            "CartoDB_Voyager",
            0, 19, 256, ".png",
            new String[]{
                    "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                    "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                    "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
            },
            "© OpenStreetMap contributors © CARTO"
    ) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            int zoom = org.osmdroid.util.MapTileIndex.getZoom(pMapTileIndex);
            int x = org.osmdroid.util.MapTileIndex.getX(pMapTileIndex);
            int y = org.osmdroid.util.MapTileIndex.getY(pMapTileIndex);
            return getBaseUrl() + zoom + "/" + x + "/" + y + ".png";
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = requireContext().getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue("CivicLensAI_SmartTriage_App/1.0 (Android; contact@civiclens.ai)");

        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Configure OpenStreetMap View using CartoDB Voyager tiles (fast, beautiful, no 403 errors)
        binding.mapView.setTileSource(CARTO_VOYAGER);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        binding.mapView.getController().setZoom(14.0);

        // Default Center on Pune City Center
        GeoPoint defaultCenter = new GeoPoint(com.example.civiclensai.utils.GeoLocationResolver.PUNE_LAT, com.example.civiclensai.utils.GeoLocationResolver.PUNE_LNG);
        binding.mapView.getController().setCenter(defaultCenter);

        // Filter chips logic
        setupFilterChips();

        binding.fabMyLocation.setOnClickListener(v -> fetchRealTimeLocationAndCenter(true));

        binding.btnViewDetails.setOnClickListener(v -> {
            if (selectedIssue != null) {
                Intent intent = new Intent(requireContext(), IssueDetailActivity.class);
                intent.putExtra("issue", selectedIssue);
                startActivity(intent);
            }
        });

        // Observe issues from repository
        IssueRepository.getInstance().getIssues().observe(getViewLifecycleOwner(), issues -> {
            if (issues != null) {
                this.currentIssues = issues;
                renderMapMarkers();
            }
        });

        // Check if location permission is already granted and mark self location silently
        fetchRealTimeLocationAndCenter(false);
    }

    private void fetchRealTimeLocationAndCenter(boolean shouldCenter) {
        if (getContext() == null) return;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldCenter) {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
            return;
        }

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null && binding != null) {
                myLocationPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                renderMapMarkers();
                if (shouldCenter && binding.mapView != null) {
                    binding.mapView.getController().setZoom(16.0);
                    binding.mapView.getController().animateTo(myLocationPoint);
                    Toast.makeText(requireContext(), "📍 Centered on your real-time GPS location!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            currentCategoryFilter = null;
            renderMapMarkers();
        });
        binding.chipPotholes.setOnClickListener(v -> {
            currentCategoryFilter = IssueCategory.POTHOLE;
            renderMapMarkers();
        });
        binding.chipGarbage.setOnClickListener(v -> {
            currentCategoryFilter = IssueCategory.GARBAGE;
            renderMapMarkers();
        });
        binding.chipWater.setOnClickListener(v -> {
            currentCategoryFilter = IssueCategory.WATER_LEAK;
            renderMapMarkers();
        });
        binding.chipLights.setOnClickListener(v -> {
            currentCategoryFilter = IssueCategory.STREETLIGHT;
            renderMapMarkers();
        });
    }

    private void renderMapMarkers() {
        if (binding == null || binding.mapView == null) return;

        // Clear existing overlays
        binding.mapView.getOverlays().clear();

        // Render Self Location Marker if present
        if (myLocationPoint != null) {
            Marker selfMarker = new Marker(binding.mapView);
            selfMarker.setPosition(myLocationPoint);
            selfMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            selfMarker.setTitle("📍 My Location (Self)");
            selfMarker.setSnippet("You are currently here");
            binding.mapView.getOverlays().add(selfMarker);
        }

        if (currentIssues == null || currentIssues.isEmpty()) {
            binding.mapView.invalidate();
            return;
        }

        GeoPoint firstMatchingPoint = null;

        for (CivicIssue issue : currentIssues) {
            // Apply category filter if set
            if (currentCategoryFilter != null && issue.getCategory() != currentCategoryFilter) {
                continue;
            }

            GeoPoint point = new GeoPoint(issue.getLatitude(), issue.getLongitude());
            if (firstMatchingPoint == null) {
                firstMatchingPoint = point;
            }

            Marker marker = new Marker(binding.mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(issue.getTitle());
            marker.setSnippet(issue.getAddress());

            // Marker click callback
            marker.setOnMarkerClickListener((m, mapView) -> {
                showBottomSheet(issue);
                return true;
            });

            binding.mapView.getOverlays().add(marker);
        }

        if (firstMatchingPoint != null && myLocationPoint == null) {
            binding.mapView.getController().animateTo(firstMatchingPoint);
        }

        binding.mapView.invalidate();
    }

    private void showBottomSheet(CivicIssue issue) {
        this.selectedIssue = issue;
        binding.bottomSheetCard.setVisibility(View.VISIBLE);
        binding.sheetTitle.setText(issue.getTitle());
        binding.sheetAddress.setText(issue.getAddress());
        binding.sheetCategoryBadge.setText(issue.getCategory().getDisplayName());

        try {
            binding.sheetSeverityBadge.setBackgroundColor(Color.parseColor(issue.getSeverity().getHexColor()));
        } catch (Exception ignored) {}

        binding.sheetSeverityBadge.setText(issue.getSeverity().getLabel() + " SEVERITY");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && binding.mapView != null) {
            binding.mapView.onResume();
        }
        fetchRealTimeLocationAndCenter(false);
    }

    @Override
    public void onPause() {
        if (binding != null && binding.mapView != null) {
            binding.mapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (binding != null && binding.mapView != null) {
            binding.mapView.onDetach();
        }
        binding = null;
        super.onDestroyView();
    }
}

