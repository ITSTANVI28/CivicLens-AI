package com.example.civiclensai.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
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
import com.google.android.gms.location.Priority;

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
                    Toast.makeText(requireContext(), "Location permission denied. Showing default city view.", Toast.LENGTH_SHORT).show();
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
        binding.mapView.getController().setZoom(15.0);

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
        if (getContext() == null || getActivity() == null) return;

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

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            handleLocationResult(location, shouldCenter);
                        } else {
                            fallbackLocationCheck(shouldCenter);
                        }
                    })
                    .addOnFailureListener(e -> fallbackLocationCheck(shouldCenter));
        } catch (SecurityException se) {
            fallbackLocationCheck(shouldCenter);
        }
    }

    private void fallbackLocationCheck(boolean shouldCenter) {
        if (getContext() == null || getActivity() == null) return;

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    handleLocationResult(location, shouldCenter);
                } else {
                    Location nativeLocation = getNativeLocation();
                    if (nativeLocation != null) {
                        handleLocationResult(nativeLocation, shouldCenter);
                    } else if (shouldCenter) {
                        Toast.makeText(requireContext(), "📍 Using City Center View (Pune)", Toast.LENGTH_SHORT).show();
                        if (binding != null && binding.mapView != null) {
                            binding.mapView.getController().setCenter(new GeoPoint(com.example.civiclensai.utils.GeoLocationResolver.PUNE_LAT, com.example.civiclensai.utils.GeoLocationResolver.PUNE_LNG));
                        }
                    }
                }
            });
        } catch (SecurityException ignored) {}
    }

    private Location getNativeLocation() {
        if (getContext() == null) return null;
        try {
            LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (gpsLoc != null) return gpsLoc;
                    Location netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (netLoc != null) return netLoc;
                    Location passLoc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    if (passLoc != null) return passLoc;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void handleLocationResult(@NonNull Location location, boolean shouldCenter) {
        if (binding == null || binding.mapView == null) return;
        myLocationPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        renderMapMarkers();
        if (shouldCenter) {
            binding.mapView.getController().setZoom(16.5);
            binding.mapView.getController().animateTo(myLocationPoint);
            Toast.makeText(requireContext(), "📍 Centered on your real-time GPS location!", Toast.LENGTH_SHORT).show();
        }
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

    private Drawable createCustomMarkerIcon(String emoji, int colorHex, boolean isSelf) {
        int width = isSelf ? 110 : 96;
        int height = isSelf ? 110 : 96;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (isSelf) {
            // Self Location pulsing blue ring
            paint.setColor(Color.parseColor("#38BDF8"));
            paint.setAlpha(80);
            canvas.drawCircle(width / 2f, height / 2f, width / 2.2f, paint);

            paint.setColor(Color.WHITE);
            paint.setAlpha(255);
            canvas.drawCircle(width / 2f, height / 2f, width / 3f, paint);

            paint.setColor(Color.parseColor("#0284C7"));
            canvas.drawCircle(width / 2f, height / 2f, width / 3.8f, paint);
        } else {
            // Pin bubble shadow & background
            paint.setColor(Color.parseColor("#33000000"));
            canvas.drawRoundRect(new RectF(8, 8, width - 8, height - 8), 24, 24, paint);

            paint.setColor(colorHex);
            canvas.drawRoundRect(new RectF(4, 4, width - 12, height - 12), 24, 24, paint);

            // Inner white border
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(4f);
            canvas.drawRoundRect(new RectF(6, 6, width - 14, height - 14), 22, 22, paint);

            // Emoji / Text
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(36f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(emoji, width / 2f - 4, height / 2f + 10, paint);
        }

        return new BitmapDrawable(getResources(), bitmap);
    }

    private void renderMapMarkers() {
        if (binding == null || binding.mapView == null || getContext() == null) return;

        // Clear existing overlays
        binding.mapView.getOverlays().clear();

        // Render Self Location Marker if present
        if (myLocationPoint != null) {
            Marker selfMarker = new Marker(binding.mapView);
            selfMarker.setPosition(myLocationPoint);
            selfMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            selfMarker.setIcon(createCustomMarkerIcon("📍", Color.parseColor("#0284C7"), true));
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
            
            // Dynamic category pin styling
            int pinColor = Color.parseColor("#E11D48"); // Default Red
            String emoji = "🕳️";

            if (issue.getCategory() == IssueCategory.GARBAGE) {
                pinColor = Color.parseColor("#D97706");
                emoji = "🗑️";
            } else if (issue.getCategory() == IssueCategory.WATER_LEAK) {
                pinColor = Color.parseColor("#0284C7");
                emoji = "💧";
            } else if (issue.getCategory() == IssueCategory.STREETLIGHT) {
                pinColor = Color.parseColor("#CA8A04");
                emoji = "💡";
            } else if (issue.getCategory() == IssueCategory.MANHOLE) {
                pinColor = Color.parseColor("#DC2626");
                emoji = "🚨";
            }

            marker.setIcon(createCustomMarkerIcon(emoji, pinColor, false));
            marker.setTitle(issue.getTitle());
            marker.setSnippet(issue.getAddress());

            // Marker click callback
            marker.setOnMarkerClickListener((m, mapView) -> {
                showBottomSheet(issue);
                mapView.getController().animateTo(point);
                return true;
            });

            binding.mapView.getOverlays().add(marker);
        }

        // If newly opened or refreshed, animate to first active issue if self location not yet locked
        if (firstMatchingPoint != null && myLocationPoint == null) {
            binding.mapView.getController().setZoom(15.0);
            binding.mapView.getController().setCenter(firstMatchingPoint);
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

