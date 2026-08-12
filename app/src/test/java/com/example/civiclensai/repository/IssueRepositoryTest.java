package com.example.civiclensai.repository;

import com.example.civiclensai.models.CivicIssue;
import com.example.civiclensai.models.IssueCategory;
import com.example.civiclensai.models.IssueSeverity;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class IssueRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private IssueRepository repository;

    @Before
    public void setUp() {
        repository = IssueRepository.getInstance();
    }

    @Test
    public void testRepositorySeededSampleData() {
        List<CivicIssue> issues = repository.getIssues().getValue();
        assertNotNull(issues);
        assertTrue("Repository should have seeded initial sample issues", issues.size() >= 4);
    }

    @Test
    public void testAddUniqueIssue() {
        int initialCount = repository.getIssues().getValue().size();

        // Far away coordinates (e.g., in a completely different area)
        CivicIssue uniqueIssue = new CivicIssue(
                "iss_unique_99",
                "New Water Leakage Hazard",
                "Fresh water pipe bursts on outskirts road",
                IssueCategory.WATER_LEAK,
                IssueSeverity.HIGH,
                18.7000, 73.9900,
                "Outskirts Highway, Pune",
                "http://example.com/leak.jpg",
                "Test User",
                "Water Board"
        );

        IssueRepository.SubmissionResult result = repository.addIssueWithDeduplication(uniqueIssue);
        assertNotNull(result);
        assertFalse("Unique issue should NOT be merged as duplicate", result.isMergedDuplicate);
        assertEquals(uniqueIssue.getId(), result.targetIssue.getId());

        int newCount = repository.getIssues().getValue().size();
        assertEquals(initialCount + 1, newCount);
    }

    @Test
    public void testAddDuplicateIssueWithin50mMergesAndIncrementsUpvotes() {
        // Create initial master pothole issue
        CivicIssue masterPothole = new CivicIssue(
                "iss_master_pothole",
                "Central Avenue Pothole",
                "Deep crater in middle of road",
                IssueCategory.POTHOLE,
                IssueSeverity.HIGH,
                18.520400, 73.856700,
                "Central Avenue, Pune",
                "http://example.com/pothole1.jpg",
                "Reporter 1",
                "Public Works Dept"
        );
        masterPothole.setUpvotesCount(5);

        repository.addIssueWithDeduplication(masterPothole);

        // Create duplicate report within 10 meters of same category
        CivicIssue duplicatePothole = new CivicIssue(
                "iss_dup_pothole",
                "Pothole on Central Ave",
                "Severe road hole causing traffic delay",
                IssueCategory.POTHOLE,
                IssueSeverity.HIGH,
                18.520480, 73.856700, // ~8 meters away
                "Central Avenue, Pune",
                "http://example.com/pothole2.jpg",
                "Reporter 2",
                "Public Works Dept"
        );

        IssueRepository.SubmissionResult result = repository.addIssueWithDeduplication(duplicatePothole);

        assertNotNull(result);
        assertTrue("Issue within 50m radius should be merged as duplicate", result.isMergedDuplicate);
        assertEquals("iss_master_pothole", result.targetIssue.getId());

        // Upvotes count should increase from 5 to 6
        assertTrue("Upvotes count on master ticket should increment", result.targetIssue.getUpvotesCount() > 5);
    }
}
