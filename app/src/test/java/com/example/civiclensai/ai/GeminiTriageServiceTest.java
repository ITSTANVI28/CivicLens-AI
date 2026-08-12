package com.example.civiclensai.ai;

import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.models.IssueSeverity;

import org.junit.Test;
import static org.junit.Assert.*;

public class GeminiTriageServiceTest {

    @Test
    public void testTriageResultConstructorAndGetters() {
        GeminiTriageService.TriageResult result = new GeminiTriageService.TriageResult(
                IssueCategory.POTHOLE,
                IssueSeverity.HIGH,
                "Asphalt Crater Hazard",
                "Deep pothole on main avenue",
                "Public Works Dept",
                false,
                "₹4,500 – ₹8,000",
                "Bituminous Asphalt",
                8.4
        );

        assertEquals(IssueCategory.POTHOLE, result.category);
        assertEquals(IssueSeverity.HIGH, result.severity);
        assertEquals("Asphalt Crater Hazard", result.title);
        assertEquals("Deep pothole on main avenue", result.description);
        assertEquals("Public Works Dept", result.department);
        assertFalse(result.isDuplicateCandidate);
        assertEquals("₹4,500 – ₹8,000", result.repairCostEstimate);
        assertEquals("Bituminous Asphalt", result.recommendedMaterial);
        assertEquals(8.4, result.hazardRiskScore, 0.01);
    }

    @Test
    public void testCategoryFromString() {
        assertEquals(IssueCategory.POTHOLE, IssueCategory.fromString("pothole"));
        assertEquals(IssueCategory.GARBAGE, IssueCategory.fromString("GARBAGE"));
        assertEquals(IssueCategory.WATER_LEAK, IssueCategory.fromString("WATER_LEAK"));
        assertEquals(IssueCategory.STREETLIGHT, IssueCategory.fromString("STREETLIGHT"));
        assertEquals(IssueCategory.MANHOLE, IssueCategory.fromString("MANHOLE"));
        assertEquals(IssueCategory.OTHER, IssueCategory.fromString("UNKNOWN_XYZ"));
    }

    @Test
    public void testSeverityFromString() {
        assertEquals(IssueSeverity.CRITICAL, IssueSeverity.fromString("CRITICAL"));
        assertEquals(IssueSeverity.HIGH, IssueSeverity.fromString("high"));
        assertEquals(IssueSeverity.MEDIUM, IssueSeverity.fromString("MEDIUM"));
        assertEquals(IssueSeverity.LOW, IssueSeverity.fromString("low"));
        assertEquals(IssueSeverity.MEDIUM, IssueSeverity.fromString("INVALID"));
    }
}
