package com.example.civiclensai.utils;

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.models.IssueSeverity;
import com.example.civiclensai.models.IssueStatus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CrewRouteOptimizerTest {

    @Test
    public void testComputeOptimalCrewRouteNullOrEmpty() {
        CrewRouteOptimizer.OptimizedRouteResult resultNull = CrewRouteOptimizer.computeOptimalCrewRoute(null, 18.5204, 73.8567);
        assertNotNull(resultNull);
        assertTrue(resultNull.orderedRoute.isEmpty());
        assertEquals(0.0, resultNull.totalDistanceKm, 0.001);

        CrewRouteOptimizer.OptimizedRouteResult resultEmpty = CrewRouteOptimizer.computeOptimalCrewRoute(Collections.emptyList(), 18.5204, 73.8567);
        assertNotNull(resultEmpty);
        assertTrue(resultEmpty.orderedRoute.isEmpty());
    }

    @Test
    public void testComputeOptimalCrewRouteOrdersByProximityAndFiltersResolved() {
        List<CivicIssue> issues = new ArrayList<>();

        // Resolved issue - should be ignored
        CivicIssue resolved = new CivicIssue("r1", "Resolved Hazard", "Desc", IssueCategory.POTHOLE, IssueSeverity.LOW, 18.5204, 73.8567, "Loc", null, "User", "Dept");
        resolved.setStatus(IssueStatus.RESOLVED);

        // Near open issue (~1.5 km away)
        CivicIssue near = new CivicIssue("open_near", "Near Hazard", "Desc", IssueCategory.POTHOLE, IssueSeverity.HIGH, 18.5300, 73.8500, "Near Loc", null, "User", "Dept");

        // Far open issue (~10 km away)
        CivicIssue far = new CivicIssue("open_far", "Far Hazard", "Desc", IssueCategory.WATER_LEAK, IssueSeverity.CRITICAL, 18.6000, 73.9000, "Far Loc", null, "User", "Dept");

        issues.add(resolved);
        issues.add(far);
        issues.add(near);

        double startLat = 18.5204;
        double startLng = 73.8567;

        CrewRouteOptimizer.OptimizedRouteResult result = CrewRouteOptimizer.computeOptimalCrewRoute(issues, startLat, startLng);

        assertNotNull(result);
        assertEquals(2, result.orderedRoute.size());
        assertEquals("open_near", result.orderedRoute.get(0).getId());
        assertEquals("open_far", result.orderedRoute.get(1).getId());
        assertTrue("Total distance should be greater than zero", result.totalDistanceKm > 0.0);
        assertTrue("Estimated driving time should be greater than zero", result.estimatedDrivingTimeMinutes > 0.0);

        String summary = result.getSummaryText();
        assertNotNull(summary);
        assertTrue(summary.contains("2 Stops"));
    }
}
