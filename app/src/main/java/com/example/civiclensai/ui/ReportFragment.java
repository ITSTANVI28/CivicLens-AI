package com.example.civiclensai.ui;

import android.content.Intent;
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
import java.util.Locale;

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

    private final ActivityResultLauncher<Intent> speechLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    java.util.ArrayList<String> matches = result.getData().getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenText = matches.get(0);
                        binding.etDescription.setText(spokenText);
                        if (binding.etTitle.getText() == null || binding.etTitle.getText().toString().isEmpty()) {
                            binding.etTitle.setText("Voice Report: " + (spokenText.length() > 28 ? spokenText.substring(0, 28) + "..." : spokenText));
                        }
                        Toast.makeText(requireContext(), "🎙️ Voice Dictation Captured!", Toast.LENGTH_SHORT).show();
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

        binding.btnEmergencySos.setOnClickListener(v -> trigger1TapEmergencySos());
        binding.btnCamera.setOnClickListener(v -> createMockPhotoCaptured("Camera"));
        binding.btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnVoiceReport.setOnClickListener(v -> com.example.civiclensai.utils.VoiceTriageHelper.launchVoiceDictation(speechLauncher, requireContext()));
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
        String[] categories = {"Pothole & Road Hazard", "Garbage & Waste", "Water Leak & Drainage", "Broken Streetlight", "Open Manhole Hazard", "General Civic Issue"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void trigger1TapEmergencySos() {
        if (capturedBitmap == null) {
            createMockPhotoCaptured("Camera");
        }

        String userAddr = binding.etAddress.getText() != null && !binding.etAddress.getText().toString().trim().isEmpty() ?
                binding.etAddress.getText().toString().trim() : "FC Road, Shivajinagar, Pune";
        double[] coords = com.example.civiclensai.utils.GeoLocationResolver.resolveCoordinates(requireContext(), userAddr);

        String sosTitle = "CRITICAL HAZARD SOS: Open Manhole / Cable Line";
        String sosDesc = "Emergency 1-Tap SOS triggered by citizen on-site. Immediate municipal dispatch and safety barrier placement required.";
        String sosAddress = userAddr.contains("Pune") ? userAddr : userAddr + ", Pune";

        CivicIssue sosIssue = new CivicIssue(
                "sos_" + System.currentTimeMillis(),
                sosTitle,
                sosDesc,
                IssueCategory.MANHOLE,
                IssueSeverity.CRITICAL,
                coords[0],
                coords[1],
                sosAddress,
                "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600",
                sessionManager.getUserName(),
                "Disaster Management & Safety Dept"
        );

        IssueRepository.getInstance().addIssueWithDeduplication(sosIssue);
        sessionManager.addKarmaPoints(100);
        NotificationHelper.showStatusChangedNotification(requireContext(), sosTitle, "CRITICAL DISPATCH");
        Toast.makeText(requireContext(), "1-Tap Emergency SOS Dispatched to Pune Sector! Priority 24h SLA Activated (+100 Karma)", Toast.LENGTH_LONG).show();
    }

    private void createMockPhotoCaptured(String source) {
        capturedBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        capturedBitmap.eraseColor(Color.LTGRAY);
        binding.ivPreview.setImageBitmap(capturedBitmap);
        binding.layoutPhotoPlaceholder.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "Photo captured from " + source + "! Ready for AI triage.", Toast.LENGTH_SHORT).show();
    }

    private void autoDetectGpsLocation() {
        double[] coords = com.example.civiclensai.utils.GeoLocationResolver.resolveCoordinates(requireContext(), "FC Road, Shivajinagar, Pune");
        binding.etAddress.setText("FC Road, Shivajinagar, Pune (" + String.format(Locale.ROOT, "%.4f, %.4f", coords[0], coords[1]) + ")");
        Toast.makeText(requireContext(), "GPS Location Resolved: Pune Sector!", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(requireContext(), "Gemini AI Vision Triage Complete!", Toast.LENGTH_SHORT).show();
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
        String address = binding.etAddress.getText() != null && !binding.etAddress.getText().toString().trim().isEmpty() ?
                binding.etAddress.getText().toString().trim() : "FC Road, Shivajinagar, Pune";

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an issue title or run AI triage.", Toast.LENGTH_SHORT).show();
            return;
        }

        double[] resolvedCoords = com.example.civiclensai.utils.GeoLocationResolver.resolveCoordinates(requireContext(), address);

        IssueCategory category = IssueCategory.values()[Math.min(binding.spinnerCategory.getSelectedItemPosition(), IssueCategory.values().length - 1)];
        IssueSeverity severity = currentTriageResult != null ? currentTriageResult.severity : IssueSeverity.HIGH;
        String dept = currentTriageResult != null ? currentTriageResult.department : category.getDefaultDepartment();

        CivicIssue newIssue = new CivicIssue(
                "iss_" + System.currentTimeMillis(),
                title,
                desc,
                category,
                severity,
                resolvedCoords[0],
                resolvedCoords[1],
                address,
                "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600",
                sessionManager.getUserName(),
                dept
        );

        // Anonymize faces/license plates before upload
        if (capturedBitmap != null) {
            capturedBitmap = com.example.civiclensai.utils.PrivacyBlurEngine.anonymizeImage(capturedBitmap);
        }

        IssueRepository.SubmissionResult result = IssueRepository.getInstance().addIssueWithDeduplication(newIssue);
        sessionManager.addKarmaPoints(50);

        // Check proximity alert for critical hazards
        com.example.civiclensai.utils.ProximityAlertHelper.checkAndNotifyProximityAlert(requireContext(), newIssue, 12.9716, 77.5946);

        if (result.isMergedDuplicate) {
            NotificationHelper.showReportSubmittedNotification(requireContext(), "Duplicate Hazard Merged: " + result.targetIssue.getTitle());
            Toast.makeText(requireContext(), "Similar hazard detected within 50m! Upvote added to ticket #" + result.targetIssue.getId() + " (+50 Karma)", Toast.LENGTH_LONG).show();
        } else {
            NotificationHelper.showReportSubmittedNotification(requireContext(), title);
            Toast.makeText(requireContext(), "New Master Report Submitted! (+50 Karma Points)", Toast.LENGTH_LONG).show();
        }

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
