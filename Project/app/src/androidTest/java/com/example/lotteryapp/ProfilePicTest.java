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
public class ProfilePicTest {

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
    public void testProfilePicFlow() {
        onView(withId(R.id.entrantButton)).perform(click());

        // Step 2: Wait for the EntrantProfileEditActivity to load
        try {
            Thread.sleep(2000); // Wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Step 3: Fill in the profile form
        onView(withId(R.id.edit_name))  // Replace with the actual ID
                .perform(typeText("John Doe"));
        onView(withId(R.id.edit_email))  // Replace with the actual ID
                .perform(typeText("johndoe@example.com"));
        onView(withId(R.id.edit_phone))  // Replace with the actual ID
                .perform(typeText("1234567890"));

        // Step 4: Click "Confirm Changes"
        onView(withId(R.id.confirm_changes)).perform(click());
        sleep();
        onView(withId(R.id.profile_icon)).check(matches(isDisplayed()));
    }
}