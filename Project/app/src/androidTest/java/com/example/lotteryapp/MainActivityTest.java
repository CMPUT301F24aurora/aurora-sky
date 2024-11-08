package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void setUp() {
        // Initialize Intents and launch the scenario
        Intents.init();
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        scenario = ActivityScenario.launch(intent);
    }

    @Test
    public void testEntrantButtonRedirectsToEntrantProfileEdit() {
        // Click the "Entrant" button
        onView(withId(R.id.entrantButton)).perform(click());

        // Wait for the activity to be loaded (e.g., 2 seconds)
        try {
            Thread.sleep(2000); // Wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that EntrantProfileEditActivity is displayed
        onView(withId(R.id.confirm_changes)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        Intents.release();
        scenario.close();  // Close the ActivityScenario
    }
}
