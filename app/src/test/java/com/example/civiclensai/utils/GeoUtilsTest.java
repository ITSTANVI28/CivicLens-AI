package com.example.civiclensai.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class GeoUtilsTest {

    @Test
    public void testSameCoordinatesZeroDistance() {
        double lat = 18.5204;
        double lon = 73.8567;
        double distance = GeoUtils.calculateHaversineDistance(lat, lon, lat, lon);
        assertEquals(0.0, distance, 0.001);
        assertTrue(GeoUtils.isWithinRadius(lat, lon, lat, lon, 50.0));
    }

    @Test
    public void testNearbyPointsWithin50mRadius() {
        double lat1 = 18.520400;
        double lon1 = 73.856700;
        // Approximately 22 meters away
        double lat2 = 18.520600;
        double lon2 = 73.856700;

        double distance = GeoUtils.calculateHaversineDistance(lat1, lon1, lat2, lon2);
        assertTrue("Distance should be less than 50m but was " + distance, distance < 50.0);
        assertTrue(GeoUtils.isWithinRadius(lat1, lon1, lat2, lon2, 50.0));
    }

    @Test
    public void testDistantPointsOutside50mRadius() {
        double lat1 = 18.5204;
        double lon1 = 73.8567;
        // Kothrud sector: ~5.5 km away
        double lat2 = 18.5074;
        double lon2 = 73.8077;

        double distance = GeoUtils.calculateHaversineDistance(lat1, lon1, lat2, lon2);
        assertTrue("Distance should be greater than 5000m but was " + distance, distance > 5000.0);
        assertFalse(GeoUtils.isWithinRadius(lat1, lon1, lat2, lon2, 50.0));
    }
}
