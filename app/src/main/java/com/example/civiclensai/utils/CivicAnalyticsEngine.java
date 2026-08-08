package com.example.civiclensai.utils;

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueStatus;

import java.util.List;

public class CivicAnalyticsEngine {

    public static class CivicHealthMetrics {
        public final int totalIssues;
        public final int resolvedIssues;
        public final int openCriticalHazards;
        public final int healthScorePercent;
        public final String statusRating;

        public CivicHealthMetrics(int totalIssues, int resolvedIssues, int openCriticalHazards, int healthScorePercent, String statusRating) {
            this.totalIssues = totalIssues;
            this.resolvedIssues = resolvedIssues;
            this.openCriticalHazards = openCriticalHazards;
            this.healthScorePercent = healthScorePercent;
            this.statusRating = statusRating;
        }
    }

    public static CivicHealthMetrics computeCityHealth(List<CivicIssue> issues) {
        if (issues == null || issues.isEmpty()) {
            return new CivicHealthMetrics(0, 0, 0, 100, "EXCELLENT (No Active Hazards)");
        }

        int total = issues.size();
        int resolved = 0;
        int critical = 0;

        for (CivicIssue issue : issues) {
            if (issue.getStatus() == IssueStatus.RESOLVED) {
                resolved++;
            }
            if (issue.getSeverity() != null && "CRITICAL".equalsIgnoreCase(issue.getSeverity().name())
                    && issue.getStatus() != IssueStatus.RESOLVED) {
                critical++;
            }
        }

        int score = (int) Math.round(((double) resolved / (double) total) * 100.0);
        // Base score adjustment for active issues
        score = Math.max(25, Math.min(100, score + 45));

        String rating;
        if (score >= 85) {
            rating = "🟢 EXCELLENT INFRASTRUCTURE";
        } else if (score >= 70) {
            rating = "🟡 GOOD (ROUTINE MAINTENANCE)";
        } else if (score >= 50) {
            rating = "🟠 MODERATE HAZARD DENSITY";
        } else {
            rating = "🔴 CRITICAL REPAIR NEEDED";
        }

        return new CivicHealthMetrics(total, resolved, critical, score, rating);
    }

    public static String getBadgeTitleForKarma(int karmaPoints) {
        if (karmaPoints >= 600) {
            return "👑 MASTER CIVIC AUDITOR";
        } else if (karmaPoints >= 400) {
            return "🛡️ ECO SENTINEL";
        } else if (karmaPoints >= 250) {
            return "🚗 ROAD SAFETY CHAMPION";
        } else if (karmaPoints >= 100) {
            return "🏛️ CIVIC GUARDIAN";
        } else {
            return "🌱 ACTIVE CITIZEN";
        }
    }
}
