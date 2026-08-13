package com.example.civiclensai.utils;

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public static int computeWardHealthScore(List<CivicIssue> issues, String wardKeyword) {
        if (issues == null || issues.isEmpty()) return 92;
        int wardTotal = 0;
        int wardResolved = 0;
        for (CivicIssue issue : issues) {
            if (issue.getAddress() != null && issue.getAddress().toLowerCase(Locale.ROOT).contains(wardKeyword.toLowerCase(Locale.ROOT))) {
                wardTotal++;
                if (issue.getStatus() == IssueStatus.RESOLVED) wardResolved++;
            }
        }
        if (wardTotal == 0) return 88;
        return (int) Math.max(40, Math.min(100, Math.round(((double) wardResolved / wardTotal) * 100.0) + 50));
    }

    public static String calculateContractorPenalty(CivicIssue issue) {
        if (issue == null) return "No Penalty";
        long remaining = issue.getSlaDeadline() - System.currentTimeMillis();
        if (remaining <= 0) {
            long hoursOverdue = Math.abs(remaining) / (3600 * 1000L);
            long penaltyINR = (hoursOverdue + 1) * 500L;
            return String.format(Locale.US, "🚨 Overdue by %dh (Penalty: ₹%,d)", hoursOverdue, penaltyINR);
        }
        return "✅ SLA Compliant";
    }

    public static String exportCityDataToGeoJson(List<CivicIssue> issues) {
        try {
            JSONObject root = new JSONObject();
            root.put("type", "FeatureCollection");
            JSONArray features = new JSONArray();

            if (issues != null) {
                for (CivicIssue issue : issues) {
                    JSONObject feature = new JSONObject();
                    feature.put("type", "Feature");

                    JSONObject geometry = new JSONObject();
                    geometry.put("type", "Point");
                    JSONArray coords = new JSONArray();
                    coords.put(issue.getLongitude());
                    coords.put(issue.getLatitude());
                    geometry.put("coordinates", coords);

                    JSONObject props = new JSONObject();
                    props.put("id", issue.getId());
                    props.put("title", issue.getTitle());
                    props.put("category", issue.getCategory().name());
                    props.put("severity", issue.getSeverity().name());
                    props.put("status", issue.getStatus().name());
                    props.put("address", issue.getAddress());
                    props.put("repairCost", issue.getRepairCostEstimate());
                    props.put("riskScore", issue.getHazardRiskScore());

                    feature.put("geometry", geometry);
                    feature.put("properties", props);
                    features.put(feature);
                }
            }

            root.put("features", features);
            String json = root.toString(2);
            if (json != null && !json.isEmpty()) return json;
        } catch (Exception ignored) {}

        // Reliable fallback builder for host testing environments where org.json is stubbed
        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"type\": \"FeatureCollection\",\n  \"features\": [\n");
        if (issues != null) {
            for (int i = 0; i < issues.size(); i++) {
                CivicIssue issue = issues.get(i);
                sb.append("    {\n");
                sb.append("      \"type\": \"Feature\",\n");
                sb.append("      \"geometry\": {\n");
                sb.append("        \"type\": \"Point\",\n");
                sb.append(String.format(Locale.US, "        \"coordinates\": [%.6f, %.6f]\n", issue.getLongitude(), issue.getLatitude()));
                sb.append("      },\n");
                sb.append("      \"properties\": {\n");
                sb.append(String.format(Locale.US, "        \"id\": \"%s\",\n", issue.getId() != null ? issue.getId() : ""));
                sb.append(String.format(Locale.US, "        \"title\": \"%s\",\n", issue.getTitle() != null ? issue.getTitle() : ""));
                sb.append(String.format(Locale.US, "        \"category\": \"%s\",\n", issue.getCategory() != null ? issue.getCategory().name() : ""));
                sb.append(String.format(Locale.US, "        \"severity\": \"%s\",\n", issue.getSeverity() != null ? issue.getSeverity().name() : ""));
                sb.append(String.format(Locale.US, "        \"status\": \"%s\"\n", issue.getStatus() != null ? issue.getStatus().name() : ""));
                sb.append("      }\n");
                sb.append("    }").append(i < issues.size() - 1 ? "," : "").append("\n");
            }
        }
        sb.append("  ]\n}");
        return sb.toString();
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

