package com.example.civiclensai.ui.myreports;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.civiclensai.databinding.ActivityEditIssueBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.repository.IssueRepository;

public class EditIssueActivity extends AppCompatActivity {

    private ActivityEditIssueBinding binding;
    private CivicIssue issue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditIssueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        issue = (CivicIssue) getIntent().getSerializableExtra("issue");

        if (issue == null) {
            Toast.makeText(this, "Error loading report details.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        renderIssue();

        binding.btnUpdateIssue.setOnClickListener(v -> {
            String title = binding.etEditTitle.getText() != null ? binding.etEditTitle.getText().toString().trim() : "";
            String desc = binding.etEditDesc.getText() != null ? binding.etEditDesc.getText().toString().trim() : "";

            if (title.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            issue.setTitle(title);
            issue.setDescription(desc);
            IssueRepository.getInstance().updateIssue(issue);
            Toast.makeText(this, "✅ Report updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });

        binding.btnDeleteIssue.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Report")
                    .setMessage("Are you sure you want to delete this report before official review?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        IssueRepository.getInstance().deleteIssue(issue.getId());
                        Toast.makeText(this, "🗑️ Report deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void renderIssue() {
        binding.etEditTitle.setText(issue.getTitle());
        binding.etEditDesc.setText(issue.getDescription());
        binding.tvEditStatusBadge.setText(issue.getStatus().getLabel());

        try {
            binding.tvEditStatusBadge.setBackgroundColor(Color.parseColor(issue.getStatus().getHexColor()));
        } catch (Exception ignored) {}
    }
}
