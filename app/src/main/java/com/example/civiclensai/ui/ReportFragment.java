package com.example.civiclensai.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.civiclensai.ai.GeminiTriageService;
import com.example.civiclensai.databinding.FragmentReportBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.models.IssueSeverity;
import com.example.civiclensai.repository.IssueRepository;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;
    private Bitmap capturedBitmap;
    private GeminiTriageService.TriageResult currentTriageResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Photo Card Click Handler
        binding.cardPhotoPicker.setOnClickListener(v -> createMockPhotoCaptured());

        // Run Gemini AI Triage Button
        binding.btnAiAnalyze.setOnClickListener(v -> {
            if (capturedBitmap == null) {
                createMockPhotoCaptured();
            }
            runAiTriage();
        });

        // Submit Issue Button
        binding.btnSubmitIssue.setOnClickListener(v -> submitIssueReport());
    }

    private void createMockPhotoCaptured() {
        // Create a 400x400 sample bitmap for testing
        capturedBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        capturedBitmap.eraseColor(Color.LTGRAY);
        binding.ivPreview.setImageBitmap(capturedBitmap);
        binding.layoutPhotoPlaceholder.setVisibility(View.GONE);
        Toast.makeText(requireContext(), "📷 Photo captured! Ready for Gemini AI analysis.", Toast.LENGTH_SHORT).show();
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

                binding.cardAiResult.setVisibility(View.VISIBLE);
                binding.tvAiCategory.setText(result.category.getEmoji() + " " + result.category.getDisplayName());
                binding.tvAiSeverity.setText(result.severity.getLabel() + " SEVERITY");

                try {
                    binding.tvAiSeverity.setBackgroundColor(Color.parseColor(result.severity.getHexColor()));
                } catch (Exception ignored) {}

                binding.etTitle.setText(result.title);
                binding.etDescription.setText(result.description);
                binding.tvDepartment.setText("Routing to: " + result.department);

                Toast.makeText(requireContext(), "✨ Gemini AI Triage Complete!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Please run Gemini AI analysis or enter a title.", Toast.LENGTH_SHORT).show();
            return;
        }

        IssueCategory category = currentTriageResult != null ? currentTriageResult.category : IssueCategory.POTHOLE;
        IssueSeverity severity = currentTriageResult != null ? currentTriageResult.severity : IssueSeverity.HIGH;
        String dept = currentTriageResult != null ? currentTriageResult.department : "Public Works Dept";

        CivicIssue newIssue = new CivicIssue(
                "iss_" + System.currentTimeMillis(),
                title,
                desc,
                category,
                severity,
                12.9730 + (Math.random() - 0.5) * 0.02, // Random coordinate near city center
                77.5950 + (Math.random() - 0.5) * 0.02,
                address,
                "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600",
                "You (Citizen)",
                dept
        );

        IssueRepository.getInstance().addIssue(newIssue);
        Toast.makeText(requireContext(), "🎉 Civic Issue Published to Live City Map!", Toast.LENGTH_LONG).show();

        // Reset form fields
        binding.cardAiResult.setVisibility(View.GONE);
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
