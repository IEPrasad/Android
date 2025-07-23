package com.s23010155.database;

import com.s23010155.models.Issue;
import java.util.ArrayList;
import java.util.List;

public class MockIssues {

    public static List<Issue> getMockIssues() {
        List<Issue> issues = new ArrayList<>();

        Issue issue1 = new Issue();
        issue1.setCategory("Agriculture");
        issue1.setStatus("Pending");
        issue1.setLatitude(34.0522);
        issue1.setLongitude(-118.2437);
        issue1.setPhotoUri("https://example.com/image1.jpg");
        issues.add(issue1);

        Issue issue2 = new Issue();
        issue2.setCategory("Road");
        issue2.setStatus("Answered");
        issue2.setLatitude(34.0532);
        issue2.setLongitude(-118.2447);
        issue2.setPhotoUri("https://example.com/image2.jpg");
        issues.add(issue2);

        Issue issue3 = new Issue();
        issue3.setCategory("Electricity");
        issue3.setStatus("Closed");
        issue3.setLatitude(34.0542);
        issue3.setLongitude(-118.2457);
        issue3.setPhotoUri("https://example.com/image3.jpg");
        issues.add(issue3);

        return issues;
    }
} 