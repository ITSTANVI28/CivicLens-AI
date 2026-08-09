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
    private String parentIssueId;

    // Advanced Gemini AI Features
    private String repairCostEstimate;
    private String recommendedMaterial;
    private double hazardRiskScore;

    public CivicIssue() {
        // Default constructor for Firestore / Serialization
        this.timestamp = System.currentTimeMillis();
        this.status = IssueStatus.REPORTED;
        this.upvotesCount = 1;
        this.confirmationsCount = 1;
        this.repairCostEstimate = "₹3,500 – ₹6,000";
        this.recommendedMaterial = "Cold-Mix Asphalt Patch";
        this.hazardRiskScore = 7.5;
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

        calculateAiDefaults();
    }

    private void calculateAiDefaults() {
        if (severity == IssueSeverity.CRITICAL) {
            this.repairCostEstimate = "₹12,000 – ₹25,000";
            this.recommendedMaterial = "Reinforced Cast-Iron Cover & High-Grade Concrete Ring";
            this.hazardRiskScore = 9.2;
        } else if (severity == IssueSeverity.HIGH) {
            this.repairCostEstimate = "₹4,500 – ₹8,500";
            this.recommendedMaterial = "Hot-Mix Polymer Bituminous Patch";
            this.hazardRiskScore = 8.1;
        } else if (severity == IssueSeverity.MEDIUM) {
            this.repairCostEstimate = "₹2,000 – ₹4,000";
            this.recommendedMaterial = "Heavy-Duty Municipal Polyethylene Bin Module";
            this.hazardRiskScore = 5.8;
        } else {
            this.repairCostEstimate = "₹1,200 – ₹2,500";
            this.recommendedMaterial = "IP66 LED Luminaire Fixture & Cable Wire Harness";
            this.hazardRiskScore = 3.4;
        }
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
    public void setSeverity(IssueSeverity severity) { 
        this.severity = severity; 
        calculateAiDefaults();
    }

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

    public String getParentIssueId() { return parentIssueId; }
    public void setParentIssueId(String parentIssueId) { this.parentIssueId = parentIssueId; }

    public String getRepairCostEstimate() { return repairCostEstimate; }
    public void setRepairCostEstimate(String repairCostEstimate) { this.repairCostEstimate = repairCostEstimate; }

    public String getRecommendedMaterial() { return recommendedMaterial; }
    public void setRecommendedMaterial(String recommendedMaterial) { this.recommendedMaterial = recommendedMaterial; }

    public double getHazardRiskScore() { return hazardRiskScore; }
    public void setHazardRiskScore(double hazardRiskScore) { this.hazardRiskScore = hazardRiskScore; }

    public long getSlaDeadline() {
        long duration = 14 * 24 * 3600 * 1000L;
        if (severity == IssueSeverity.CRITICAL) {
            duration = 24 * 3600 * 1000L;
        } else if (severity == IssueSeverity.HIGH) {
            duration = 72 * 3600 * 1000L;
        } else if (severity == IssueSeverity.MEDIUM) {
            duration = 7 * 24 * 3600 * 1000L;
        }
        return timestamp + duration;
    }

    public String getFormattedSlaRemaining() {
        long remaining = getSlaDeadline() - System.currentTimeMillis();
        if (remaining <= 0) return "🚨 SLA Breached";
        long hours = remaining / (3600 * 1000L);
        long minutes = (remaining % (3600 * 1000L)) / (60 * 1000L);
        return hours + "h " + minutes + "m remaining";
    }
}


