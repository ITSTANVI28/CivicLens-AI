package com.example.civiclensai.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.models.IssueSeverity;
import com.example.civiclensai.models.IssueStatus;
import com.example.civiclensai.models.VerificationModel;
import com.example.civiclensai.utils.GeoUtils;

import java.util.ArrayList;
import java.util.List;

public class IssueRepository {

    public static final double DEDUPLICATION_RADIUS_METERS = 50.0;

    private static IssueRepository instance;
    private final MutableLiveData<List<CivicIssue>> issuesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<VerificationModel>> verificationsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final List<CivicIssue> issuesList = new ArrayList<>();

    public static class SubmissionResult {
        public final boolean isMergedDuplicate;
        public final CivicIssue targetIssue;

        public SubmissionResult(boolean isMergedDuplicate, CivicIssue targetIssue) {
            this.isMergedDuplicate = isMergedDuplicate;
            this.targetIssue = targetIssue;
        }
    }

    private IssueRepository() {
        seedSampleData();
    }

    public static synchronized IssueRepository getInstance() {
        if (instance == null) {
            instance = new IssueRepository();
        }
        return instance;
    }

    public LiveData<List<CivicIssue>> getIssues() {
        return issuesLiveData;
    }

    /**
     * Adds an issue with spatial deduplication checking against active issues within a 50m radius.
     */
    public SubmissionResult addIssueWithDeduplication(CivicIssue newIssue) {
        if (newIssue.getId() == null || newIssue.getId().isEmpty()) {
            newIssue.setId("iss_" + System.currentTimeMillis());
        }

        // Check spatial deduplication against existing issues
        for (CivicIssue existing : issuesList) {
            if (existing.getStatus() == IssueStatus.RESOLVED) continue;

            boolean isClose = GeoUtils.isWithinRadius(
                    newIssue.getLatitude(), newIssue.getLongitude(),
                    existing.getLatitude(), existing.getLongitude(),
                    DEDUPLICATION_RADIUS_METERS
            );

            boolean isSameCategory = existing.getCategory() == newIssue.getCategory();

            if (isClose && isSameCategory) {
                // Duplicate hazard detected within 50m radius! Merge into existing master ticket
                existing.setUpvotesCount(existing.getUpvotesCount() + 1);
                existing.setConfirmationsCount(existing.getConfirmationsCount() + 1);

                newIssue.setDuplicate(true);
                newIssue.setParentIssueId(existing.getId());

                issuesLiveData.postValue(new ArrayList<>(issuesList));
                return new SubmissionResult(true, existing);
            }
        }

        // Unique issue report: add to top of feed
        issuesList.add(0, newIssue);
        issuesLiveData.postValue(new ArrayList<>(issuesList));
        return new SubmissionResult(false, newIssue);
    }

    public void addIssue(CivicIssue issue) {
        addIssueWithDeduplication(issue);
    }

    public void upvoteIssue(String issueId) {
        for (CivicIssue issue : issuesList) {
            if (issue.getId().equals(issueId)) {
                issue.setUpvotesCount(issue.getUpvotesCount() + 1);
                break;
            }
        }
        issuesLiveData.postValue(new ArrayList<>(issuesList));
    }

    public void addVerification(VerificationModel verification) {
        List<VerificationModel> current = verificationsLiveData.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(0, verification);
        verificationsLiveData.postValue(current);

        // Update issue status if vote is FIXED
        if ("FIXED".equalsIgnoreCase(verification.getStatusVote())) {
            for (CivicIssue issue : issuesList) {
                if (issue.getId().equals(verification.getIssueId())) {
                    issue.setConfirmationsCount(issue.getConfirmationsCount() + 1);
                    if (issue.getConfirmationsCount() >= 3) {
                        issue.setStatus(IssueStatus.RESOLVED);
                    }
                    break;
                }
            }
            issuesLiveData.postValue(new ArrayList<>(issuesList));
        }
    }

    public void updateIssue(CivicIssue updatedIssue) {
        for (int i = 0; i < issuesList.size(); i++) {
            if (issuesList.get(i).getId().equals(updatedIssue.getId())) {
                issuesList.set(i, updatedIssue);
                break;
            }
        }
        issuesLiveData.postValue(new ArrayList<>(issuesList));
    }

    public void deleteIssue(String issueId) {
        for (int i = 0; i < issuesList.size(); i++) {
            if (issuesList.get(i).getId().equals(issueId)) {
                issuesList.remove(i);
                break;
            }
        }
        issuesLiveData.postValue(new ArrayList<>(issuesList));
    }

    public List<CivicIssue> getUserIssues(String reporterName) {
        List<CivicIssue> userList = new ArrayList<>();
        for (CivicIssue issue : issuesList) {
            if (issue.getReporterName() != null && 
               (issue.getReporterName().equalsIgnoreCase(reporterName) || 
                issue.getReporterName().contains("Alex") || 
                issue.getReporterName().contains("You"))) {
                userList.add(issue);
            }
        }
        return userList;
    }

    public CivicIssue getIssueById(String issueId) {
        for (CivicIssue issue : issuesList) {
            if (issue.getId().equals(issueId)) {
                return issue;
            }
        }
        return null;
    }

    private void seedSampleData() {
        CivicIssue item1 = new CivicIssue(
                "iss_101",
                "Large Asphalt Pothole near Main Junction",
                "Deep crater causing vehicle slowing and hazard for two-wheelers.",
                IssueCategory.POTHOLE,
                IssueSeverity.HIGH,
                18.5308, 73.8474,
                "FC Road, Shivajinagar, Pune",
                "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7?w=600",
                "Alex Citizen",
                "Public Works Department"
        );
        item1.setUpvotesCount(24);
        item1.setConfirmationsCount(5);

        CivicIssue item2 = new CivicIssue(
                "iss_102",
                "Overflowing Waste Bin near Park",
                "Uncollected municipal trash spilling onto public walking track.",
                IssueCategory.GARBAGE,
                IssueSeverity.MEDIUM,
                18.5074, 73.8077,
                "Karve Road, Kothrud, Pune",
                "https://images.unsplash.com/photo-1530587191325-3db32d826c18?w=600",
                "Priya Sharma",
                "Sanitation & Waste Dept"
        );
        item2.setUpvotesCount(18);

        CivicIssue item3 = new CivicIssue(
                "iss_103",
                "High-Pressure Underground Water Leak",
                "Clean water leaking from main pipeline onto road surface.",
                IssueCategory.WATER_LEAK,
                IssueSeverity.CRITICAL,
                18.5679, 73.9143,
                "Airport Road, Viman Nagar, Pune",
                "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=600",
                "Rahul Verma",
                "Water Supply & Sewage Board"
        );
        item3.setUpvotesCount(42);

        CivicIssue item4 = new CivicIssue(
                "iss_104",
                "Non-Functional Streetlight Unit",
                "Dark stretch of road due to damaged LED light pole.",
                IssueCategory.STREETLIGHT,
                IssueSeverity.LOW,
                18.5590, 73.7868,
                "Baner High Street, Baner, Pune",
                "https://images.unsplash.com/photo-1509114397022-ed747cca3f65?w=600",
                "Anita Roy",
                "Electrical Dept"
        );
        item4.setStatus(IssueStatus.RESOLVED);
        item4.setUpvotesCount(9);

        issuesList.add(item1);
        issuesList.add(item2);
        issuesList.add(item3);
        issuesList.add(item4);
        issuesLiveData.setValue(new ArrayList<>(issuesList));
    }
}
