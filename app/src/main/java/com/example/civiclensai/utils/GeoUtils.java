package com.example.civiclensai.utils;

public class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6371000.0; // Mean radius of Earth

    /**
     * Calculates the surface distance in meters between two geographical points using the Haversine formula.
     *
     * @param lat1 Latitude of point 1 in degrees
     * @param lon1 Longitude of point 1 in degrees
     * @param lat2 Latitude of point 2 in degrees
     * @param lon2 Longitude of point 2 in degrees
     * @return Distance between points in meters
     */
    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) +
                   Math.cos(radLat1) * Math.cos(radLat2) *
                   Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Checks if two geographic locations are within a specified distance threshold.
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @param thresholdMeters Maximum distance threshold in meters (e.g. 50m)
     * @return True if locations are within thresholdMeters
     */
    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double thresholdMeters) {
        return calculateHaversineDistance(lat1, lon1, lat2, lon2) <= thresholdMeters;
    }
}
