package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerFacilityCreationTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        activityRule.launchActivity(intent);
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateFacility() {
        // Navigate to Organizer Main Page
        onView(withId(R.id.organizerButton)).perform(click());
        sleep();

        // Verify we're on the Organizer Main Page
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));

        // Click the Create Facility button
        onView(withId(R.id.create_facility_button)).perform(click());
        sleep();

        // Verify we're on the Facility Creation page
        onView(withId(R.id.name_field)).check(matches(isDisplayed()));

        // Input facility details
        onView(withId(R.id.name_field)).perform(typeText("Test Facility"));
        onView(withId(R.id.location_field)).perform(typeText("123 Test Street"));
        onView(withId(R.id.email_field)).perform(typeText("test@facility.com"));

        // Click the save button
        onView(withId(R.id.save_button)).perform(click());
        sleep();

        // Verify that we're back on the Organizer Main Page
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.create_event_button)).check(matches(isDisplayed()));
    }
}