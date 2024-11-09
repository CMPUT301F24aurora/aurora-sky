package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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

    // US 01.02.01 As an entrant, I want to provide my personal information such as name, email and
    // optional phone number in the app
    @Test
    public void testConfirmChangesRedirectsToEntrantsEventsActivity() {
        // Step 1: Click the "I am Entrant" button
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

        // Step 5: Wait for the redirection to be processed
        try {
            Thread.sleep(2000); // Wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Step 6: Verify the intent to open EntrantsEventsActivity
        Intents.intended(IntentMatchers.hasComponent(EntrantsEventsActivity.class.getName()));
    }

    @Test
    public void testEntrantLoginRedirectsToEventsPageWhenAlreadyRegistered() {
        // Step 1: Click the "I am Entrant" button
        onView(withId(R.id.entrantButton)).perform(click());

        // Step 2: Wait for the redirection to be processed
        try {
            Thread.sleep(2000); // Wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Step 3: Verify the intent to open EntrantsEventsActivity directly
        Intents.intended(IntentMatchers.hasComponent(EntrantsEventsActivity.class.getName()));
    }



    @After
    public void tearDown() {
        Intents.release();  // Release intents to avoid any interference with other tests
        scenario.close();   // Close the ActivityScenario
    }
}
