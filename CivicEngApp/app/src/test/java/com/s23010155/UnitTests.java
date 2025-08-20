package com.s23010155;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import com.s23010155.database.AuthDatabaseHelper;
import com.s23010155.database.IssueDatabaseHelper;
import com.s23010155.database.PollDatabaseHelper;
import com.s23010155.database.PolicyVoteDatabaseHelper;
import com.s23010155.models.Issue;
import com.s23010155.models.Poll;

@RunWith(AndroidJUnit4.class)
public class UnitTests {
    
    private Context context;
    private AuthDatabaseHelper authHelper;
    private IssueDatabaseHelper issueHelper;
    private PollDatabaseHelper pollHelper;
    private PolicyVoteDatabaseHelper policyHelper;
    
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        authHelper = new AuthDatabaseHelper(context);
        issueHelper = new IssueDatabaseHelper(context);
        pollHelper = new PollDatabaseHelper(context);
        policyHelper = new PolicyVoteDatabaseHelper(context);
        
        // Clear all databases before each test
        context.deleteDatabase("civic_auth.db");
        context.deleteDatabase("civic_issues.db");
        context.deleteDatabase("civic_polls.db");
        context.deleteDatabase("civic_policy_votes.db");
    }
    
    // ==================== AUTH DATABASE TESTS ====================
    
    @Test
    public void testAuthDatabaseCreation() {
        assertNotNull(authHelper.getReadableDatabase());
        assertNotNull(authHelper.getWritableDatabase());
    }
    
    @Test
    public void testUserRegistration() {
        boolean result = authHelper.registerUser("test@email.com", "password123", "123456789V");
        assertTrue("User registration should succeed", result);
    }
    
    @Test
    public void testDuplicateEmailRegistration() {
        authHelper.registerUser("test@email.com", "password123", "123456789V");
        boolean result = authHelper.registerUser("test@email.com", "password456", "987654321X");
        assertFalse("Duplicate email registration should fail", result);
    }
    
    @Test
    public void testDuplicateNICRegistration() {
        authHelper.registerUser("test1@email.com", "password123", "123456789V");
        boolean result = authHelper.registerUser("test2@email.com", "password456", "123456789V");
        assertFalse("Duplicate NIC registration should fail", result);
    }
    
    @Test
    public void testUserExists() {
        authHelper.registerUser("test@email.com", "password123", "123456789V");
        assertTrue("User should exist", authHelper.isUserExists("test@email.com"));
        assertFalse("User should not exist", authHelper.isUserExists("nonexistent@email.com"));
    }
    
    @Test
    public void testNICExists() {
        authHelper.registerUser("test@email.com", "password123", "123456789V");
        assertTrue("NIC should exist", authHelper.isNicExists("123456789V"));
        assertFalse("NIC should not exist", authHelper.isNicExists("987654321X"));
    }
    
    @Test
    public void testValidCredentials() {
        authHelper.registerUser("test@email.com", "password123", "123456789V");
        assertTrue("Valid credentials should pass", 
                  authHelper.validateCredentials("test@email.com", "password123"));
        assertFalse("Invalid credentials should fail", 
                   authHelper.validateCredentials("test@email.com", "wrongpassword"));
    }
    
    // ==================== ISSUE DATABASE TESTS ====================
    
    @Test
    public void testIssueDatabaseCreation() {
        assertNotNull(issueHelper.getReadableDatabase());
        assertNotNull(issueHelper.getWritableDatabase());
    }
    
    @Test
    public void testIssueInsertion() {
        long id = issueHelper.insertIssue("Road", "Pothole", "Large pothole on main road", 
                                        "Main Street", 6.9271, 79.8612, null, "Colombo");
        assertTrue("Issue insertion should succeed", id > 0);
    }
    
    @Test
    public void testIssueRetrieval() {
        long id = issueHelper.insertIssue("Water", "Leak", "Water leak in pipe", 
                                        "Water Street", 6.9271, 79.8612, null, "Colombo");
        
        Issue issue = issueHelper.getIssueById(id);
        assertNotNull("Issue should be retrieved", issue);
        assertEquals("Title should match", "Leak", issue.getTitle());
        assertEquals("Category should match", "Water", issue.getCategory());
        assertEquals("District should match", "Colombo", issue.getDistrict());
    }
    
    @Test
    public void testIssueStatusUpdate() {
        long id = issueHelper.insertIssue("Road", "Pothole", "Large pothole", 
                                        "Main Street", 6.9271, 79.8612, null, "Colombo");
        
        issueHelper.updateIssueStatus(id, "Solved");
        Issue issue = issueHelper.getIssueById(id);
        assertEquals("Status should be updated", "Solved", issue.getStatus());
    }
    
    @Test
    public void testGetAllIssues() {
        issueHelper.insertIssue("Road", "Pothole", "Large pothole", 
                              "Main Street", 6.9271, 79.8612, null, "Colombo");
        issueHelper.insertIssue("Water", "Leak", "Water leak", 
                              "Water Street", 6.9271, 79.8612, null, "Colombo");
        
        java.util.List<Issue> issues = issueHelper.getAllIssues();
        assertEquals("Should have 2 issues", 2, issues.size());
    }
    
    @Test
    public void testGetIssuesByDistrict() {
        issueHelper.insertIssue("Road", "Pothole", "Large pothole", 
                              "Main Street", 6.9271, 79.8612, null, "Colombo");
        issueHelper.insertIssue("Water", "Leak", "Water leak", 
                              "Water Street", 6.9271, 79.8612, null, "Gampaha");
        
        java.util.List<Issue> colomboIssues = issueHelper.getIssuesByDistrict("Colombo");
        assertEquals("Should have 1 Colombo issue", 1, colomboIssues.size());
        
        java.util.List<Issue> gampahaIssues = issueHelper.getIssuesByDistrict("Gampaha");
        assertEquals("Should have 1 Gampaha issue", 1, gampahaIssues.size());
    }
    
    // ==================== POLL DATABASE TESTS ====================
    
    @Test
    public void testPollDatabaseCreation() {
        assertNotNull(pollHelper.getReadableDatabase());
        assertNotNull(pollHelper.getWritableDatabase());
    }
    
    @Test
    public void testGovPerformanceVote() {
        pollHelper.upsertGovPerfVote("123456789V", 2025, 1, 1, "Colombo");
        
        int userVote = pollHelper.getUserVote("123456789V", 2025, 1);
        assertEquals("User vote should be 1 (Good)", 1, userVote);
    }
    
    @Test
    public void testVoteUpdate() {
        pollHelper.upsertGovPerfVote("123456789V", 2025, 1, 1, "Colombo");
        pollHelper.upsertGovPerfVote("123456789V", 2025, 1, -1, "Colombo");
        
        int userVote = pollHelper.getUserVote("123456789V", 2025, 1);
        assertEquals("User vote should be updated to -1 (Bad)", -1, userVote);
    }
    
    @Test
    public void testMonthTotals() {
        pollHelper.upsertGovPerfVote("123456789V", 2025, 1, 1, "Colombo");
        pollHelper.upsertGovPerfVote("987654321X", 2025, 1, 1, "Colombo");
        pollHelper.upsertGovPerfVote("111111111V", 2025, 1, -1, "Colombo");
        
        int[] totals = pollHelper.getMonthTotals(2025, 1);
        assertEquals("Should have 2 Good votes", 2, totals[0]);
        assertEquals("Should have 1 Bad vote", 1, totals[1]);
    }
    
    @Test
    public void testDistrictTotals() {
        pollHelper.upsertGovPerfVote("123456789V", 2025, 1, 1, "Colombo");
        pollHelper.upsertGovPerfVote("987654321X", 2025, 1, 1, "Gampaha");
        pollHelper.upsertGovPerfVote("111111111V", 2025, 1, -1, "Colombo");
        
        int[] colomboTotals = pollHelper.getDistrictTotals("Colombo", 2025, 1);
        assertEquals("Colombo should have 1 Good vote", 1, colomboTotals[0]);
        assertEquals("Colombo should have 1 Bad vote", 1, colomboTotals[1]);
        
        int[] gampahaTotals = pollHelper.getDistrictTotals("Gampaha", 2025, 1);
        assertEquals("Gampaha should have 1 Good vote", 1, gampahaTotals[0]);
        assertEquals("Gampaha should have 0 Bad votes", 0, gampahaTotals[1]);
    }
    
    // ==================== POLICY VOTE DATABASE TESTS ====================
    
    @Test
    public void testPolicyVoteDatabaseCreation() {
        assertNotNull(policyHelper.getReadableDatabase());
        assertNotNull(policyHelper.getWritableDatabase());
    }
    
    @Test
    public void testPolicyVote() {
        policyHelper.upsertPolicyVote("policy_001", "123456789V", 1);
        
        int userVote = policyHelper.getUserPolicyVote("policy_001", "123456789V");
        assertEquals("User policy vote should be 1 (Agree)", 1, userVote);
    }
    
    @Test
    public void testPolicyVoteUpdate() {
        policyHelper.upsertPolicyVote("policy_001", "123456789V", 1);
        policyHelper.upsertPolicyVote("policy_001", "123456789V", -1);
        
        int userVote = policyHelper.getUserPolicyVote("policy_001", "123456789V");
        assertEquals("User policy vote should be updated to -1 (Disagree)", -1, userVote);
    }
    
    @Test
    public void testPolicyTotals() {
        policyHelper.upsertPolicyVote("policy_001", "123456789V", 1);
        policyHelper.upsertPolicyVote("policy_001", "987654321X", 1);
        policyHelper.upsertPolicyVote("policy_001", "111111111V", -1);
        
        int[] totals = policyHelper.getPolicyTotals("policy_001");
        assertEquals("Should have 2 Agree votes", 2, totals[0]);
        assertEquals("Should have 1 Disagree vote", 1, totals[1]);
    }
    
    // ==================== MODEL TESTS ====================
    
    @Test
    public void testIssueModel() {
        Issue issue = new Issue();
        issue.setId(1);
        issue.setCategory("Road");
        issue.setTitle("Pothole");
        issue.setDescription("Large pothole on main road");
        issue.setLocationName("Main Street");
        issue.setLatitude(6.9271);
        issue.setLongitude(79.8612);
        issue.setStatus("Pending");
        issue.setDistrict("Colombo");
        
        assertEquals("ID should match", 1, issue.getId());
        assertEquals("Category should match", "Road", issue.getCategory());
        assertEquals("Title should match", "Pothole", issue.getTitle());
        assertEquals("Description should match", "Large pothole on main road", issue.getDescription());
        assertEquals("Location should match", "Main Street", issue.getLocationName());
        assertEquals("Latitude should match", 6.9271, issue.getLatitude(), 0.001);
        assertEquals("Longitude should match", 79.8612, issue.getLongitude(), 0.001);
        assertEquals("Status should match", "Pending", issue.getStatus());
        assertEquals("District should match", "Colombo", issue.getDistrict());
    }
    
    @Test
    public void testPollModel() {
        Poll poll = new Poll();
        poll.setId(1);
        poll.setQuestion("How is the government performing?");
        poll.setGoodVotes(15);
        poll.setBadVotes(5);
        
        assertEquals("ID should match", 1, poll.getId());
        assertEquals("Question should match", "How is the government performing?", poll.getQuestion());
        assertEquals("Good votes should match", 15, poll.getGoodVotes());
        assertEquals("Bad votes should match", 5, poll.getBadVotes());
        assertEquals("Total votes should be 20", 20, poll.getTotalVotes());
    }
}

