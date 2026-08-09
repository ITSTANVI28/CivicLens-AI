package com.example.civiclensai.utils;

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CrewRouteOptimizer {

    public static class OptimizedRouteResult {
        public final List<CivicIssue> orderedRoute;
        public final double totalDistanceKm;
        public final double estimatedDrivingTimeMinutes;

        public OptimizedRouteResult(List<CivicIssue> orderedRoute, double totalDistanceKm, double estimatedDrivingTimeMinutes) {
            this.orderedRoute = orderedRoute;
            this.totalDistanceKm = totalDistanceKm;
            this.estimatedDrivingTimeMinutes = estimatedDrivingTimeMinutes;
        }

        public String getSummaryText() {
            return String.format(Locale.US, "🚒 Optimal Truck Route: %d Stops • %.1f km • ~%.0f mins drive time",
                    orderedRoute.size(), totalDistanceKm, estimatedDrivingTimeMinutes);
        }
    }

    /**
     * Computes nearest-neighbor geographic route optimization for municipal maintenance trucks.
     */
    public static OptimizedRouteResult computeOptimalCrewRoute(List<CivicIssue> allIssues, double startLat, double startLng) {
        List<CivicIssue> openIssues = new ArrayList<>();
        if (allIssues != null) {
            for (CivicIssue issue : allIssues) {
                if (issue.getStatus() != IssueStatus.RESOLVED) {
                    openIssues.add(issue);
                }
            }
        }

        if (openIssues.isEmpty()) {
            return new OptimizedRouteResult(new ArrayList<>(), 0.0, 0.0);
        }

        List<CivicIssue> unvisited = new ArrayList<>(openIssues);
        List<CivicIssue> route = new ArrayList<>();

        double currLat = startLat;
        double currLng = startLng;
        double totalDistMeters = 0.0;

        while (!unvisited.isEmpty()) {
            CivicIssue nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (CivicIssue issue : unvisited) {
                double dist = GeoUtils.calculateHaversineDistance(currLat, currLng, issue.getLatitude(), issue.getLongitude());
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = issue;
                }
            }

            if (nearest != null) {
                route.add(nearest);
                unvisited.remove(nearest);
                totalDistMeters += minDistance;
                currLat = nearest.getLatitude();
                currLng = nearest.getLongitude();
            }
        }

        double totalKm = totalDistMeters / 1000.0;
        double estMins = (totalKm / 30.0) * 60.0; // 30km/h city truck speed
        return new OptimizedRouteResult(route, totalKm, estMins);
    }
}
