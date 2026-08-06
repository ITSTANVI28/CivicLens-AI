package com.example.civiclensai.models;

import java.io.Serializable;

public class CivicIssue implements Serializable {
    private String id;
    private String title;
    private String description;
    private IssueCategory category;
    private IssueSeverity severity;
    private IssueStatus status;
    private double latitude;
    private double longitude;
    private String address;
    private String imageUrl;
    private String reporterName;
    private String department;
    private int upvotesCount;
    private int confirmationsCount;
    private long timestamp;
    private boolean isDuplicate;

    public CivicIssue() {
        // Default constructor for Firestore / Serialization
        this.timestamp = System.currentTimeMillis();
        this.status = IssueStatus.REPORTED;
        this.upvotesCount = 1;
        this.confirmationsCount = 1;
    }

    public CivicIssue(String id, String title, String description, IssueCategory category,
                      IssueSeverity severity, double latitude, double longitude,
                      String address, String imageUrl, String reporterName, String department) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.status = IssueStatus.REPORTED;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.imageUrl = imageUrl;
        this.reporterName = reporterName;
        this.department = department;
        this.upvotesCount = 1;
        this.confirmationsCount = 1;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public IssueCategory getCategory() { return category; }
    public void setCategory(IssueCategory category) { this.category = category; }

    public IssueSeverity getSeverity() { return severity; }
    public void setSeverity(IssueSeverity severity) { this.severity = severity; }

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getUpvotesCount() { return upvotesCount; }
    public void setUpvotesCount(int upvotesCount) { this.upvotesCount = upvotesCount; }

    public int getConfirmationsCount() { return confirmationsCount; }
    public void setConfirmationsCount(int confirmationsCount) { this.confirmationsCount = confirmationsCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isDuplicate() { return isDuplicate; }
    public void setDuplicate(boolean duplicate) { isDuplicate = duplicate; }
}
