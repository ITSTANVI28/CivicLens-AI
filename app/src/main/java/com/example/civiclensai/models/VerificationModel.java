package com.example.civiclensai.models;

import java.io.Serializable;

public class VerificationModel implements Serializable {
    private String id;
    private String issueId;
    private String userName;
    private String statusVote; // STILL_EXISTS, FIXED, IN_PROGRESS
    private String comment;
    private long timestamp;

    public VerificationModel() {
        this.timestamp = System.currentTimeMillis();
    }

    public VerificationModel(String id, String issueId, String userName, String statusVote, String comment) {
        this.id = id;
        this.issueId = issueId;
        this.userName = userName;
        this.statusVote = statusVote;
        this.comment = comment;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIssueId() { return issueId; }
    public void setIssueId(String issueId) { this.issueId = issueId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStatusVote() { return statusVote; }
    public void setStatusVote(String statusVote) { this.statusVote = statusVote; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
