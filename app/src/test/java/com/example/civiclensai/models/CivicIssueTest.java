package com.example.civiclensai.models;

import org.junit.Test;
import static org.junit.Assert.*;

public class CivicIssueTest {

    @Test
    public void testCivicIssueDefaultConstructor() {
        CivicIssue issue = new CivicIssue();
        assertNotNull(issue);
        assertEquals(IssueStatus.REPORTED, issue.getStatus());
        assertEquals(1, issue.getUpvotesCount());
        assertEquals(1, issue.getConfirmationsCount());
        assertTrue(issue.getTimestamp() > 0);
        assertNotNull(issue.getRepairCostEstimate());
        assertNotNull(issue.getRecommendedMaterial());
        assertTrue(issue.getHazardRiskScore() > 0);
    }

    @Test
    public void testCivicIssueParameterizedConstructorAndSeverityDefaults() {
        CivicIssue criticalIssue = new CivicIssue(
                "iss_001",
                "Open Manhole",
                "Dangerous deep open manhole",
                IssueCategory.MANHOLE,
                IssueSeverity.CRITICAL,
                18.5204, 73.8567,
                "FC Road, Pune",
                "http://example.com/img.jpg",
                "Citizen Alex",
                "Public Works"
        );

        assertEquals("iss_001", criticalIssue.getId());
        assertEquals("Open Manhole", criticalIssue.getTitle());
        assertEquals(IssueCategory.MANHOLE, criticalIssue.getCategory());
        assertEquals(IssueSeverity.CRITICAL, criticalIssue.getSeverity());
        assertEquals(9.2, criticalIssue.getHazardRiskScore(), 0.1);
        assertTrue(criticalIssue.getRepairCostEstimate().contains("12,000"));

        // Change severity to LOW and test recalculation of AI defaults
        criticalIssue.setSeverity(IssueSeverity.LOW);
        assertEquals(IssueSeverity.LOW, criticalIssue.getSeverity());
        assertEquals(3.4, criticalIssue.getHazardRiskScore(), 0.1);
    }

    @Test
    public void testUpvoteAndConfirmationCounts() {
        CivicIssue issue = new CivicIssue();
        issue.setUpvotesCount(10);
        assertEquals(10, issue.getUpvotesCount());

        issue.setConfirmationsCount(3);
        assertEquals(3, issue.getConfirmationsCount());

        issue.setDuplicate(true);
        assertTrue(issue.isDuplicate());

        issue.setParentIssueId("parent_123");
        assertEquals("parent_123", issue.getParentIssueId());
    }
}
