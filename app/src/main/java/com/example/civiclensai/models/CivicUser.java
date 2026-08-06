package com.example.civiclensai.models;

import java.io.Serializable;

public class CivicUser implements Serializable {
    private String uid;
    private String name;
    private String email;
    private int karmaPoints;
    private String badgeTitle;
    private int reportsCount;
    private int verificationsCount;

    public CivicUser() {}

    public CivicUser(String uid, String name, String email, int karmaPoints, String badgeTitle, int reportsCount, int verificationsCount) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.karmaPoints = karmaPoints;
        this.badgeTitle = badgeTitle;
        this.reportsCount = reportsCount;
        this.verificationsCount = verificationsCount;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getKarmaPoints() { return karmaPoints; }
    public void setKarmaPoints(int karmaPoints) { this.karmaPoints = karmaPoints; }

    public String getBadgeTitle() { return badgeTitle; }
    public void setBadgeTitle(String badgeTitle) { this.badgeTitle = badgeTitle; }

    public int getReportsCount() { return reportsCount; }
    public void setReportsCount(int reportsCount) { this.reportsCount = reportsCount; }

    public int getVerificationsCount() { return verificationsCount; }
    public void setVerificationsCount(int verificationsCount) { this.verificationsCount = verificationsCount; }
}
