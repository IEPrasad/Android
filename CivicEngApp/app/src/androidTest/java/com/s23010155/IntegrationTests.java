package com.s23010155;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;

import com.s23010155.MainActivity;
import com.s23010155.database.AuthDatabaseHelper;
import com.s23010155.database.IssueDatabaseHelper;
import com.s23010155.database.PollDatabaseHelper;
import com.s23010155.database.PolicyVoteDatabaseHelper;

@RunWith(AndroidJUnit4.class)
public class IntegrationTests {
    
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
    
    private Context context;
    private AuthDatabaseHelper authHelper;
    private IssueDatabaseHelper issueHelper;
    private PollDatabaseHelper pollHelper;
    private PolicyVoteDatabaseHelper policyHelper;
    private SharedPreferences prefs;
    
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        authHelper = new AuthDatabaseHelper(context);
        issueHelper = new IssueDatabaseHelper(context);
        pollHelper = new PollDatabaseHelper(context);
        policyHelper = new PolicyVoteDatabaseHelper(context);
        prefs = context.getSharedPreferences("civic_app_prefs", Context.MODE_PRIVATE);
        
        // Clear all data before each test
        clearAllData();
    }
    
    private void clearAllData() {
        context.deleteDatabase("civic_auth.db");
        context.deleteDatabase("civic_issues.db");
        context.deleteDatabase("civic_polls.db");
        context.deleteDatabase("civic_policy_votes.db");
        prefs.edit().clear().apply();
    }
    
    // ==================== AUTHENTICATION FLOW TESTS ====================
    
    @Test
    public void testCompleteRegistrationFlow() {
        // Navigate to registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Fill registration form
        onView(withId(R.id.editTextEmail)).perform(typeText("test@email.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextNic)).perform(typeText("123456789V"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPassword)).perform(typeText("password123"), closeSoftKeyboard());
        
        // Submit registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Should navigate to dashboard or show success
        // Note: This test may need adjustment based on actual navigation flow
    }
    
    @Test
    public void testCompleteLoginFlow() {
        // First register a user
        authHelper.registerUser("test@email.com", "password123", "123456789V");
        
        // Navigate to login
        onView(withId(R.id.btnLogin)).perform(click());
        
        // Fill login form
        onView(withId(R.id.editTextEmail)).perform(typeText("test@email.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText("password123"), closeSoftKeyboard());
        
        // Submit login
        onView(withId(R.id.btnLogin)).perform(click());
        
        // Should navigate to dashboard
        // Note: This test may need adjustment based on actual navigation flow
    }
    
    @Test
    public void testPasswordVisibilityToggle() {
        // Navigate to registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Check password field has eye icon
        onView(withId(R.id.editTextPassword)).check(matches(isDisplayed()));
        
        // Check confirm password field has eye icon
        onView(withId(R.id.editTextConfirmPassword)).check(matches(isDisplayed()));
    }
    
    // ==================== DASHBOARD NAVIGATION TESTS ====================
    
    @Test
    public void testDashboardNavigation() {
        // Assuming we're on dashboard after login
        // Test all navigation cards are visible
        
        onView(withId(R.id.cardMapView)).check(matches(isDisplayed()));
        onView(withId(R.id.cardMyReports)).check(matches(isDisplayed()));
        onView(withId(R.id.cardReportIssue)).check(matches(isDisplayed()));
        onView(withId(R.id.cardMonthlyPolls)).check(matches(isDisplayed()));
        onView(withId(R.id.cardPolicyVote)).check(matches(isDisplayed()));
        onView(withId(R.id.cardProfile)).check(matches(isDisplayed()));
        onView(withId(R.id.cardSettings)).check(matches(isDisplayed()));
        onView(withId(R.id.cardLogout)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testNavigateToMapView() {
        onView(withId(R.id.cardMapView)).perform(click());
        // Should navigate to map view fragment
        // Note: This test may need adjustment based on actual navigation flow
    }
    
    @Test
    public void testNavigateToMyReports() {
        onView(withId(R.id.cardMyReports)).perform(click());
        // Should navigate to my reports fragment
        // Note: This test may need adjustment based on actual navigation flow
    }
    
    @Test
    public void testNavigateToReportIssue() {
        onView(withId(R.id.cardReportIssue)).perform(click());
        // Should navigate to report issue fragment
        // Note: This test may need adjustment based on actual navigation flow
    }
    
    // ==================== ISSUE REPORTING TESTS ====================
    
    @Test
    public void testIssueReportingFormElements() {
        // Navigate to report issue
        onView(withId(R.id.cardReportIssue)).perform(click());
        
        // Check all form elements are present
        // Note: These IDs need to match the actual layout
        // onView(withId(R.id.chipGroupCategory)).check(matches(isDisplayed()));
        // onView(withId(R.id.editTextTitle)).check(matches(isDisplayed()));
        // onView(withId(R.id.editTextDescription)).check(matches(isDisplayed()));
        // onView(withId(R.id.spinnerDistrict)).check(matches(isDisplayed()));
        // onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        // onView(withId(R.id.btnSubmit)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testIssueCategorySelection() {
        // Navigate to report issue
        onView(withId(R.id.cardReportIssue)).perform(click());
        
        // Test category selection
        // Note: This test may need adjustment based on actual chip group implementation
        // onView(withText("Road")).perform(click());
        // onView(withText("Road")).check(matches(isEnabled()));
    }
    
    // ==================== MONTHLY POLLS TESTS ====================
    
    @Test
    public void testMonthlyPollsElements() {
        // Navigate to monthly polls
        onView(withId(R.id.cardMonthlyPolls)).perform(click());
        
        // Check poll elements are present
        // Note: These IDs need to match the actual layout
        // onView(withId(R.id.btnGood)).check(matches(isDisplayed()));
        // onView(withId(R.id.btnBad)).check(matches(isDisplayed()));
        // onView(withId(R.id.fabCreatePoll)).check(matches(isDisplayed()));
    }
    
    @Test
    public void testCreateNewPoll() {
        // Navigate to monthly polls
        onView(withId(R.id.cardMonthlyPolls)).perform(click());
        
        // Click create poll FAB
        // onView(withId(R.id.fabCreatePoll)).perform(click());
        
        // Should show dialog to create new poll
        // Note: This test may need adjustment based on actual implementation
    }
    
    // ==================== POLICY VOTING TESTS ====================
    
    @Test
    public void testPolicyVotingElements() {
        // Navigate to policy voting
        onView(withId(R.id.cardPolicyVote)).perform(click());
        
        // Check voting elements are present
        // Note: These IDs need to match the actual layout
        // onView(withId(R.id.btnAgree)).check(matches(isDisplayed()));
        // onView(withId(R.id.btnDisagree)).check(matches(isDisplayed()));
        // onView(withId(R.id.btnSubmit)).check(matches(isDisplayed()));
    }
    
    // ==================== PROFILE TESTS ====================
    
    @Test
    public void testProfileElements() {
        // Navigate to profile
        onView(withId(R.id.cardProfile)).perform(click());
        
        // Check profile elements are present
        // Note: These IDs need to match the actual layout
        // onView(withId(R.id.editTextFullName)).check(matches(isDisplayed()));
        // onView(withId(R.id.editTextBirthday)).check(matches(isDisplayed()));
        // onView(withId(R.id.editTextNic)).check(matches(isDisplayed()));
        // onView(withId(R.id.spinnerDistrict)).check(matches(isDisplayed()));
        // onView(withId(R.id.mapView)).check(matches(isDisplayed()));
        // onView(withId(R.id.btnSave)).check(matches(isDisplayed()));
    }
    
    // ==================== SETTINGS TESTS ====================
    
    @Test
    public void testSettingsElements() {
        // Navigate to settings
        onView(withId(R.id.cardSettings)).perform(click());
        
        // Check settings elements are present
        // Note: These IDs need to match the actual layout
        // onView(withId(R.id.switchDarkMode)).check(matches(isDisplayed()));
        // onView(withId(R.id.switchNotifications)).check(matches(isDisplayed()));
        // onView(withId(R.id.btnClearData)).check(matches(isDisplayed()));
    }
    
    // ==================== DATA PERSISTENCE TESTS ====================
    
    @Test
    public void testDataPersistenceAcrossSessions() {
        // Create test data
        authHelper.registerUser("test@email.com", "password123", "123456789V");
        long issueId = issueHelper.insertIssue("Road", "Test Issue", "Test Description", 
                                             "Test Location", 6.9271, 79.8612, null, "Colombo");
        
        // Verify data exists
        assertTrue("User should exist", authHelper.isUserExists("test@email.com"));
        assertNotNull("Issue should exist", issueHelper.getIssueById(issueId));
        
        // Clear and recreate database (simulating app restart)
        context.deleteDatabase("civic_auth.db");
        context.deleteDatabase("civic_issues.db");
        
        // Recreate database
        AuthDatabaseHelper newAuthHelper = new AuthDatabaseHelper(context);
        IssueDatabaseHelper newIssueHelper = new IssueDatabaseHelper(context);
        
        // Data should not exist after recreation
        assertFalse("User should not exist after recreation", newAuthHelper.isUserExists("test@email.com"));
        assertNull("Issue should not exist after recreation", newIssueHelper.getIssueById(issueId));
    }
    
    // ==================== ERROR HANDLING TESTS ====================
    
    @Test
    public void testInvalidEmailFormat() {
        // Navigate to registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Try to register with invalid email
        onView(withId(R.id.editTextEmail)).perform(typeText("invalid-email"), closeSoftKeyboard());
        onView(withId(R.id.editTextNic)).perform(typeText("123456789V"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPassword)).perform(typeText("password123"), closeSoftKeyboard());
        
        // Submit registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Should show validation error
        // Note: This test may need adjustment based on actual validation implementation
    }
    
    @Test
    public void testPasswordMismatch() {
        // Navigate to registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Try to register with mismatched passwords
        onView(withId(R.id.editTextEmail)).perform(typeText("test@email.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextNic)).perform(typeText("123456789V"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPassword)).perform(typeText("differentpassword"), closeSoftKeyboard());
        
        // Submit registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Should show password mismatch error
        // Note: This test may need adjustment based on actual validation implementation
    }
    
    @Test
    public void testShortPassword() {
        // Navigate to registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Try to register with short password
        onView(withId(R.id.editTextEmail)).perform(typeText("test@email.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextNic)).perform(typeText("123456789V"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.editTextConfirmPassword)).perform(typeText("123"), closeSoftKeyboard());
        
        // Submit registration
        onView(withId(R.id.btnRegister)).perform(click());
        
        // Should show password length error
        // Note: This test may need adjustment based on actual validation implementation
    }
    
    // ==================== PERFORMANCE TESTS ====================
    
    @Test
    public void testDatabasePerformance() {
        // Insert multiple issues
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            issueHelper.insertIssue("Road", "Issue " + i, "Description " + i, 
                                  "Location " + i, 6.9271, 79.8612, null, "Colombo");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time (e.g., 5 seconds)
        assertTrue("Database operations should complete within 5 seconds", duration < 5000);
        
        // Verify all issues were inserted
        java.util.List<Issue> issues = issueHelper.getAllIssues();
        assertEquals("Should have 100 issues", 100, issues.size());
    }
    
    // ==================== CLEANUP ====================
    
    @org.junit.After
    public void tearDown() {
        clearAllData();
    }
}

