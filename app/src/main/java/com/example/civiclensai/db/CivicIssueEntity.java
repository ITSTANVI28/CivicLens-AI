package com.example.civiclensai.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "civic_issues")
public class CivicIssueEntity {

    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String description;
    private String category;
    private String severity;
    private double latitude;
    private double longitude;
    private String address;
    private String imageUrl;
    private String reporterName;
    private String department;
    private int upvotesCount;
    private long timestamp;
    private boolean isSynced;

    public CivicIssueEntity(@NonNull String id, String title, String description, String category,
                            String severity, double latitude, double longitude, String address,
                            String imageUrl, String reporterName, String department,
                            int upvotesCount, long timestamp, boolean isSynced) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.imageUrl = imageUrl;
        this.reporterName = reporterName;
        this.department = department;
        this.upvotesCount = upvotesCount;
        this.timestamp = timestamp;
        this.isSynced = isSynced;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

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

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
