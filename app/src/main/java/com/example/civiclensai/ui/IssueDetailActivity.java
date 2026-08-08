package com.example.civiclensai.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.civiclensai.databinding.ActivityIssueDetailBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.VerificationModel;
import com.example.civiclensai.repository.IssueRepository;
import com.example.civiclensai.utils.PdfReportGenerator;

public class IssueDetailActivity extends AppCompatActivity {

    private ActivityIssueDetailBinding binding;
    private CivicIssue issue;

    private final ActivityResultLauncher<Intent> createPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri targetUri = result.getData().getData();
                    boolean success = PdfReportGenerator.writePdfTicketToUri(this, issue, targetUri);
                    if (success) {
                        Toast.makeText(this, "Official PDF Ticket saved to selected location!", Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIssueDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        issue = (CivicIssue) getIntent().getSerializableExtra("issue");

        if (issue == null) {
            Toast.makeText(this, "Error loading issue details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        renderDetails();

        binding.btnDetailUpvote.setOnClickListener(v -> {
            IssueRepository.getInstance().upvoteIssue(issue.getId());
            issue.setUpvotesCount(issue.getUpvotesCount() + 1);
            binding.btnDetailUpvote.setText("Upvoted (" + issue.getUpvotesCount() + ")");
            Toast.makeText(this, "Upvote Recorded!", Toast.LENGTH_SHORT).show();
        });

        binding.btnVerifyStatus.setOnClickListener(v -> showVerificationDialog());

        binding.btnDownloadPdf.setOnClickListener(v -> promptUserToSavePdf());
    }

    private void promptUserToSavePdf() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, "CivicLens_Ticket_" + issue.getId() + ".pdf");
        try {
            createPdfLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "File picker unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void renderDetails() {
        binding.tvDetailTitle.setText(issue.getTitle());
        binding.tvDetailDesc.setText(issue.getDescription());
        binding.tvDetailAddress.setText(issue.getAddress());
        binding.tvDetailCategory.setText(issue.getCategory().getDisplayName());
        binding.tvDetailSeverity.setText(issue.getSeverity().getLabel() + " SEVERITY");
        binding.tvSlaWindow.setText(issue.getSeverity().getSlaDescription() + " • " + issue.getFormattedSlaRemaining());
        binding.tvDetailDepartment.setText("Assigned Department: " + issue.getDepartment());
        binding.btnDetailUpvote.setText("Upvote (" + issue.getUpvotesCount() + ")");

        try {
            binding.tvDetailSeverity.setBackgroundColor(Color.parseColor(issue.getSeverity().getHexColor()));
        } catch (Exception ignored) {}

        Glide.with(this)
                .load(issue.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivDetailPhoto);

        Glide.with(this)
                .load(issue.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivBeforePhoto);

        Glide.with(this)
                .load("https://images.unsplash.com/photo-1541888946425-d0fbb186a5b3?w=600")
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivAfterPhoto);
    }

    private void showVerificationDialog() {
        String[] options = {"Fixed / Resolved", "Issue Still Exists", "Work In Progress"};

        new AlertDialog.Builder(this)
                .setTitle("Community Verification Audit")
                .setItems(options, (dialog, which) -> {
                    String vote = which == 0 ? "FIXED" : (which == 1 ? "STILL_EXISTS" : "IN_PROGRESS");
                    VerificationModel vModel = new VerificationModel(
                            "ver_" + System.currentTimeMillis(),
                            issue.getId(),
                            "You (Community Auditor)",
                            vote,
                            "Status verified by citizen on-site."
                    );
                    IssueRepository.getInstance().addVerification(vModel);
                    Toast.makeText(this, "Community Verification Audit Recorded! (+20 Karma Points)", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
