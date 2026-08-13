package com.example.civiclensai.utils;

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.models.IssueSeverity;
import com.example.civiclensai.models.IssueStatus;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CivicAnalyticsEngineTest {

    @Test
    public void testComputeCityHealthNullOrEmpty() {
        CivicAnalyticsEngine.CivicHealthMetrics metricsNull = CivicAnalyticsEngine.computeCityHealth(null);
        assertNotNull(metricsNull);
        assertEquals(0, metricsNull.totalIssues);
        assertEquals(0, metricsNull.resolvedIssues);
        assertEquals(0, metricsNull.openCriticalHazards);
        assertEquals(100, metricsNull.healthScorePercent);

        CivicAnalyticsEngine.CivicHealthMetrics metricsEmpty = CivicAnalyticsEngine.computeCityHealth(Collections.emptyList());
        assertNotNull(metricsEmpty);
        assertEquals(0, metricsEmpty.totalIssues);
        assertEquals(100, metricsEmpty.healthScorePercent);
    }

    @Test
    public void testComputeCityHealthWithMixedIssues() {
        List<CivicIssue> issues = new ArrayList<>();

        CivicIssue issue1 = new CivicIssue("1", "Pothole", "Desc", IssueCategory.POTHOLE, IssueSeverity.HIGH, 18.5, 73.8, "FC Road, Pune", null, "Alex", "PWD");
        issue1.setStatus(IssueStatus.RESOLVED);

        CivicIssue issue2 = new CivicIssue("2", "Water Leak", "Desc", IssueCategory.WATER_LEAK, IssueSeverity.CRITICAL, 18.5, 73.8, "FC Road, Pune", null, "Alex", "PWD");
        issue2.setStatus(IssueStatus.REPORTED);

        CivicIssue issue3 = new CivicIssue("3", "Garbage", "Desc", IssueCategory.GARBAGE, IssueSeverity.MEDIUM, 18.5, 73.8, "Kothrud, Pune", null, "Alex", "PWD");
        issue3.setStatus(IssueStatus.REPORTED);

        issues.add(issue1);
        issues.add(issue2);
        issues.add(issue3);

        CivicAnalyticsEngine.CivicHealthMetrics metrics = CivicAnalyticsEngine.computeCityHealth(issues);
        assertNotNull(metrics);
        assertEquals(3, metrics.totalIssues);
        assertEquals(1, metrics.resolvedIssues);
        assertEquals(1, metrics.openCriticalHazards);
        assertTrue(metrics.healthScorePercent >= 25 && metrics.healthScorePercent <= 100);
        assertNotNull(metrics.statusRating);
    }

    @Test
    public void testComputeWardHealthScore() {
        List<CivicIssue> issues = new ArrayList<>();
        CivicIssue issue1 = new CivicIssue("1", "Pothole", "Desc", IssueCategory.POTHOLE, IssueSeverity.HIGH, 18.5, 73.8, "Kothrud Sector 4, Pune", null, "Alex", "PWD");
        issue1.setStatus(IssueStatus.RESOLVED);

        CivicIssue issue2 = new CivicIssue("2", "Garbage", "Desc", IssueCategory.GARBAGE, IssueSeverity.MEDIUM, 18.5, 73.8, "Kothrud Sector 2, Pune", null, "Alex", "PWD");
        issue2.setStatus(IssueStatus.REPORTED);

        issues.add(issue1);
        issues.add(issue2);

        int kothrudScore = CivicAnalyticsEngine.computeWardHealthScore(issues, "Kothrud");
        assertTrue("Ward score should be between 40 and 100", kothrudScore >= 40 && kothrudScore <= 100);

        int unknownWardScore = CivicAnalyticsEngine.computeWardHealthScore(issues, "UnknownWard");
        assertEquals(88, unknownWardScore);
    }

    @Test
    public void testCalculateContractorPenalty() {
        assertNotNull(CivicAnalyticsEngine.calculateContractorPenalty(null));

        CivicIssue compliantIssue = new CivicIssue("1", "Title", "Desc", IssueCategory.POTHOLE, IssueSeverity.HIGH, 18.5, 73.8, "Address", null, "User", "Dept");
        String resultCompliant = CivicAnalyticsEngine.calculateContractorPenalty(compliantIssue);
        assertTrue(resultCompliant.contains("SLA Compliant"));
    }

    @Test
    public void testExportCityDataToGeoJson() {
        List<CivicIssue> issues = new ArrayList<>();
        CivicIssue issue1 = new CivicIssue("iss_1", "Pothole", "Desc", IssueCategory.POTHOLE, IssueSeverity.HIGH, 18.5204, 73.8567, "Pune", null, "User", "Dept");
        issues.add(issue1);

        String jsonStr = CivicAnalyticsEngine.exportCityDataToGeoJson(issues);
        assertNotNull(jsonStr);
        assertTrue(jsonStr.contains("FeatureCollection"));
        assertTrue(jsonStr.contains("features"));
    }

    @Test
    public void testGetBadgeTitleForKarma() {
        assertEquals("🌱 ACTIVE CITIZEN", CivicAnalyticsEngine.getBadgeTitleForKarma(50));
        assertEquals("🏛️ CIVIC GUARDIAN", CivicAnalyticsEngine.getBadgeTitleForKarma(150));
        assertEquals("🚗 ROAD SAFETY CHAMPION", CivicAnalyticsEngine.getBadgeTitleForKarma(300));
        assertEquals("🛡️ ECO SENTINEL", CivicAnalyticsEngine.getBadgeTitleForKarma(450));
        assertEquals("👑 MASTER CIVIC AUDITOR", CivicAnalyticsEngine.getBadgeTitleForKarma(750));
    }
}
