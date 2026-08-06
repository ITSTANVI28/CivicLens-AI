package com.example.civiclensai.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.civiclensai.databinding.ItemIssueCardBinding;
import com.example.civiclensai.models.CivicIssue;

import java.util.ArrayList;
import java.util.List;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.IssueViewHolder> {

    public interface OnIssueClickListener {
        void onIssueClick(CivicIssue issue);
        void onUpvoteClick(CivicIssue issue);
    }

    private final List<CivicIssue> issuesList = new ArrayList<>();
    private final OnIssueClickListener listener;

    public IssueAdapter(OnIssueClickListener listener) {
        this.listener = listener;
    }

    public void setIssues(List<CivicIssue> issues) {
        this.issuesList.clear();
        if (issues != null) {
            this.issuesList.addAll(issues);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IssueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIssueCardBinding binding = ItemIssueCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new IssueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IssueViewHolder holder, int position) {
        holder.bind(issuesList.get(position));
    }

    @Override
    public int getItemCount() {
        return issuesList.size();
    }

    class IssueViewHolder extends RecyclerView.ViewHolder {
        private final ItemIssueCardBinding binding;

        public IssueViewHolder(ItemIssueCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CivicIssue issue) {
            binding.tvIssueTitle.setText(issue.getTitle());
            binding.tvIssueDesc.setText(issue.getDescription());
            binding.tvIssueAddress.setText("📍 " + issue.getAddress());
            binding.tvUpvotes.setText("👍 " + issue.getUpvotesCount() + " Upvotes");
            binding.tvCategoryTag.setText(issue.getCategory().getEmoji() + " " + issue.getCategory().getDisplayName());
            binding.tvSeverityTag.setText(issue.getSeverity().getLabel() + " SEVERITY");

            try {
                binding.tvSeverityTag.setBackgroundColor(Color.parseColor(issue.getSeverity().getHexColor()));
            } catch (Exception ignored) {}

            Glide.with(itemView.getContext())
                    .load(issue.getImageUrl())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivIssuePhoto);

            itemView.setOnClickListener(v -> listener.onIssueClick(issue));
            binding.btnUpvote.setOnClickListener(v -> listener.onUpvoteClick(issue));
        }
    }
}
