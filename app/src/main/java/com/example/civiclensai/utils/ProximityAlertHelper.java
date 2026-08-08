package com.example.civiclensai.utils;

import android.content.Context;
import android.util.Log;

import com.example.civiclensai.models.CivicIssue;

public class ProximityAlertHelper {

    private static final String TAG = "ProximityAlertHelper";
    private static final double PROXIMITY_ALERT_RADIUS_METERS = 1000.0; // 1km hazard alert radius

    /**
     * Checks if a newly reported issue is a critical hazard within 1km of the user and triggers an emergency push notification.
     */
    public static void checkAndNotifyProximityAlert(Context context, CivicIssue issue, double userLat, double userLng) {
        if (context == null || issue == null || issue.getSeverity() == null) return;

        if ("CRITICAL".equalsIgnoreCase(issue.getSeverity().name())) {
            double distance = GeoUtils.calculateHaversineDistance(userLat, userLng, issue.getLatitude(), issue.getLongitude());

            if (distance <= PROXIMITY_ALERT_RADIUS_METERS) {
                Log.i(TAG, "Critical hazard within " + (int) distance + "m! Sending FCM proximity push alert.");
                NotificationHelper.showStatusChangedNotification(
                        context,
                        "🚨 Emergency Hazard Nearby: " + issue.getTitle(),
                        "Critical hazard reported within " + (int) distance + "m of your location. Proceed with caution."
                );
            }
        }
    }
}
