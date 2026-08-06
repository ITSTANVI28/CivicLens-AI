package com.example.civiclensai.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.civiclensai.R;
import com.example.civiclensai.databinding.FragmentFeedBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.repository.IssueRepository;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements IssueAdapter.OnIssueClickListener {

    private FragmentFeedBinding binding;
    private IssueAdapter adapter;
    private List<CivicIssue> allIssues = new ArrayList<>();
    private IssueCategory currentFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new IssueAdapter(this);
        binding.rvIssues.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIssues.setAdapter(adapter);

        // Filter chips setup
        binding.feedChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.contains(R.id.feedChipAll)) {
                currentFilter = null;
            } else if (checkedIds.contains(R.id.feedChipPothole)) {
                currentFilter = IssueCategory.POTHOLE;
            } else if (checkedIds.contains(R.id.feedChipGarbage)) {
                currentFilter = IssueCategory.GARBAGE;
            } else if (checkedIds.contains(R.id.feedChipWater)) {
                currentFilter = IssueCategory.WATER_LEAK;
            } else if (checkedIds.contains(R.id.feedChipLights)) {
                currentFilter = IssueCategory.STREETLIGHT;
            }
            applyFilter();
        });

        // Observe repository
        IssueRepository.getInstance().getIssues().observe(getViewLifecycleOwner(), issues -> {
            this.allIssues = issues != null ? issues : new ArrayList<>();
            applyFilter();
        });
    }

    private void applyFilter() {
        if (currentFilter == null) {
            adapter.setIssues(allIssues);
        } else {
            List<CivicIssue> filtered = new ArrayList<>();
            for (CivicIssue issue : allIssues) {
                if (issue.getCategory() == currentFilter) {
                    filtered.add(issue);
                }
            }
            adapter.setIssues(filtered);
        }
    }

    @Override
    public void onIssueClick(CivicIssue issue) {
        Intent intent = new Intent(requireContext(), IssueDetailActivity.class);
        intent.putExtra("issue", issue);
        startActivity(intent);
    }

    @Override
    public void onUpvoteClick(CivicIssue issue) {
        IssueRepository.getInstance().upvoteIssue(issue.getId());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
