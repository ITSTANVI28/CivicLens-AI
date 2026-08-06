package com.example.civiclensai.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.civiclensai.databinding.ActivityIssueDetailBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.VerificationModel;
import com.example.civiclensai.repository.IssueRepository;

public class IssueDetailActivity extends AppCompatActivity {

    private ActivityIssueDetailBinding binding;
    private CivicIssue issue;

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
            binding.btnDetailUpvote.setText("👍 Upvoted (" + issue.getUpvotesCount() + ")");
            Toast.makeText(this, "👍 Upvote Recorded!", Toast.LENGTH_SHORT).show();
        });

        binding.btnVerifyStatus.setOnClickListener(v -> showVerificationDialog());
    }

    private void renderDetails() {
        binding.tvDetailTitle.setText(issue.getTitle());
        binding.tvDetailDesc.setText(issue.getDescription());
        binding.tvDetailAddress.setText("📍 " + issue.getAddress());
        binding.tvDetailCategory.setText(issue.getCategory().getEmoji() + " " + issue.getCategory().getDisplayName());
        binding.tvDetailSeverity.setText(issue.getSeverity().getLabel() + " SEVERITY");
        binding.tvSlaWindow.setText(issue.getSeverity().getSlaDescription());
        binding.tvDetailDepartment.setText("Assigned Department: " + issue.getDepartment());
        binding.btnDetailUpvote.setText("👍 Upvote (" + issue.getUpvotesCount() + ")");

        try {
            binding.tvDetailSeverity.setBackgroundColor(Color.parseColor(issue.getSeverity().getHexColor()));
        } catch (Exception ignored) {}

        Glide.with(this)
                .load(issue.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivDetailPhoto);
    }

    private void showVerificationDialog() {
        String[] options = {"✅ Fixed / Resolved", "⚠️ Issue Still Exists", "🚧 Work In Progress"};

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
                    Toast.makeText(this, "✅ Community Verification Audit Recorded! (+20 Karma Points)", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
