package com.example.civiclensai.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.civiclensai.ai.GeminiTriageService;
import com.example.civiclensai.databinding.FragmentReportBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.models.IssueSeverity;
import com.example.civiclensai.repository.IssueRepository;
import com.example.civiclensai.utils.NotificationHelper;
import com.example.civiclensai.utils.SessionManager;

import java.io.IOException;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;
    private Bitmap capturedBitmap;
    private GeminiTriageService.TriageResult currentTriageResult;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null && getActivity() != null) {
                    try {
                        capturedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                        binding.ivPreview.setImageBitmap(capturedBitmap);
                        binding.layoutPhotoPlaceholder.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "🖼️ Image selected from gallery!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(requireContext(), "Error loading image from gallery.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        setupCategorySpinner();

        binding.btnCamera.setOnClickListener(v -> createMockPhotoCaptured("Camera"));
        binding.btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.cardPhotoPicker.setOnClickListener(v -> createMockPhotoCaptured("Camera"));

        binding.btnAutoLocation.setOnClickListener(v -> autoDetectGpsLocation());

        binding.btnAiAnalyze.setOnClickListener(v -> {
            if (capturedBitmap == null) {
                createMockPhotoCaptured("Camera");
            }
            runAiTriage();
        });

        binding.btnSubmitIssue.setOnClickListener(v -> submitIssueReport());
    }

    private void setupCategorySpinner() {
        String[] categories = {"🕳️ Pothole & Road Hazard", "🧹 Garbage & Waste", "💧 Water Leak & Drainage", "💡 Broken Streetlight", "⚠️ Open Manhole Hazard", "🏛️ General Civic Issue"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void createMockPhotoCaptured(String source) {
        capturedBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        capturedBitmap.eraseColor(Color.LTGRAY);
        binding.ivPreview.setImageBitmap(capturedBitmap);
        binding.layoutPhotoPlaceholder.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "📷 Photo captured from " + source + "! Ready for AI triage.", Toast.LENGTH_SHORT).show();
    }

    private void autoDetectGpsLocation() {
        // Simulates FusedLocationProviderClient GPS Auto Detect
        double lat = 12.9716 + (Math.random() - 0.5) * 0.02;
        double lng = 77.5946 + (Math.random() - 0.5) * 0.02;
        binding.etAddress.setText("📍 GPS Auto-Detected: " + String.format("%.4f, %.4f", lat, lng) + " (MG Road Sector)");
        Toast.makeText(requireContext(), "📍 GPS Location Auto-Detected!", Toast.LENGTH_SHORT).show();
    }

    private void runAiTriage() {
        binding.pbAiProgress.setVisibility(View.VISIBLE);
        binding.btnAiAnalyze.setEnabled(false);

        GeminiTriageService.analyzeIssueImage(capturedBitmap, new GeminiTriageService.TriageCallback() {
            @Override
            public void onSuccess(GeminiTriageService.TriageResult result) {
                if (binding == null) return;
                binding.pbAiProgress.setVisibility(View.GONE);
                binding.btnAiAnalyze.setEnabled(true);
                currentTriageResult = result;

                binding.etTitle.setText(result.title);
                binding.etDescription.setText(result.description);
                binding.spinnerCategory.setSelection(result.category.ordinal());

                Toast.makeText(requireContext(), "✨ Gemini AI Vision Triage Complete!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                if (binding == null) return;
                binding.pbAiProgress.setVisibility(View.GONE);
                binding.btnAiAnalyze.setEnabled(true);
                Toast.makeText(requireContext(), "AI Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitIssueReport() {
        String title = binding.etTitle.getText() != null ? binding.etTitle.getText().toString().trim() : "";
        String desc = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "MG Road Sector";

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an issue title or run AI triage.", Toast.LENGTH_SHORT).show();
            return;
        }

        IssueCategory category = IssueCategory.values()[Math.min(binding.spinnerCategory.getSelectedItemPosition(), IssueCategory.values().length - 1)];
        IssueSeverity severity = currentTriageResult != null ? currentTriageResult.severity : IssueSeverity.HIGH;
        String dept = currentTriageResult != null ? currentTriageResult.department : category.getDefaultDepartment();

        CivicIssue newIssue = new CivicIssue(
                "iss_" + System.currentTimeMillis(),
                title,
                desc,
                category,
                severity,
                12.9716 + (Math.random() - 0.5) * 0.02,
                77.5946 + (Math.random() - 0.5) * 0.02,
                address,
                "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600",
                sessionManager.getUserName(),
                dept
        );

        IssueRepository.getInstance().addIssue(newIssue);
        sessionManager.addKarmaPoints(50);

        // Show System Notification
        NotificationHelper.showReportSubmittedNotification(requireContext(), title);
        Toast.makeText(requireContext(), "🎉 Report Submitted Successfully! (+50 Karma Points)", Toast.LENGTH_LONG).show();

        // Reset Form
        binding.etTitle.setText("");
        binding.etDescription.setText("");
        binding.layoutPhotoPlaceholder.setVisibility(View.VISIBLE);
        binding.ivPreview.setImageDrawable(null);
        capturedBitmap = null;
        currentTriageResult = null;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
