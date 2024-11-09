package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminBrowseSelectProfiles {
    private void sleep() {
        // Sleep for assurance (not recommended for production tests)
        try {
            Thread.sleep(2000); // Wait for 2 seconds to ensure transition completes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        // Launch MainActivity before each test
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        activityRule.launchActivity(intent);
    }

    @Test
    public void testAdminLinkNavigatesToAdminView() {
        // Click the admin_link button in MainActivity
        onView(withId(R.id.admin_link)).perform(click());
        sleep();

        // Verify navigation to AdminHomepageActivity
        onView(withId(R.id.admin_v_pro)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminProButtonNavigatesToAdminEventEntrants() {
        // Navigate to AdminHomepageActivity
        onView(withId(R.id.admin_link)).perform(click());
        sleep();

        // Click the admin_v_pro button
        onView(withId(R.id.admin_v_pro)).perform(click());
        sleep();

        // Verify navigation to AdminViewEditProfilesActivity
        onView(withId(R.id.entrants_search_view)).check(matches(isDisplayed()));
    }
}
