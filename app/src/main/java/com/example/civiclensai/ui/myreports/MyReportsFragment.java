package com.example.civiclensai.ui.myreports;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.civiclensai.databinding.FragmentMyReportsBinding;
import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.repository.IssueRepository;
import com.example.civiclensai.ui.IssueAdapter;
import com.example.civiclensai.utils.SessionManager;

import java.util.List;

public class MyReportsFragment extends Fragment implements IssueAdapter.OnIssueClickListener {

    private FragmentMyReportsBinding binding;
    private IssueAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        adapter = new IssueAdapter(this);
        binding.rvMyReports.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMyReports.setAdapter(adapter);

        IssueRepository.getInstance().getIssues().observe(getViewLifecycleOwner(), issues -> loadUserIssues());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserIssues();
    }

    private void loadUserIssues() {
        if (binding == null || sessionManager == null) return;
        List<CivicIssue> userList = IssueRepository.getInstance().getUserIssues(sessionManager.getUserName());
        adapter.setIssues(userList);
    }

    @Override
    public void onIssueClick(CivicIssue issue) {
        Intent intent = new Intent(requireContext(), EditIssueActivity.class);
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
