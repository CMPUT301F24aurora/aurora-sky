package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertNotNull;

import static java.lang.Thread.sleep;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerCreateEventTest {
    private SimpleIdlingResource idlingResource;

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        // Launch MainActivity before each test
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        activityRule.launchActivity(intent);

        // Initialize and register IdlingResource
        idlingResource = new SimpleIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void testAdminLinkNavigatesToAdminView() {
        // Click the organizerButton in MainActivity
        onView(withId(R.id.organizerButton)).perform(click());

        // Set IdlingResource to not idle, indicating background work may be happening
        idlingResource.setIdleState(false);

        try {
            // Allow the activity transition or background task to finish
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Set IdlingResource to idle, indicating background work is done
        idlingResource.setIdleState(true);

        // Verify navigation to the next page (AdminHomepageActivity or similar)
        onView(withId(R.id.create_event_button)).perform(click());

        idlingResource.setIdleState(false);

        // Optionally, wait until the next screen is displayed, if needed
        onView(withId(R.id.create_event_button)).check(matches(isDisplayed()));
//        idlingResource.setIdleState(true);
//
//        idlingResource.setIdleState(false);
        // Interact with the OrganizerCreateEvent form
        onView(withId(R.id.editTextEventName)).perform(replaceText("donna"));

        idlingResource.setIdleState(true);
//        onView(withId(R.id.eventStartTime)).perform(replaceText("2024-12-10"));
//        onView(withId(R.id.eventEndTime)).perform(replaceText("2024-12-12"));
//        onView(withId(R.id.registrationDeadline)).perform(replaceText("2024-12-08"));
//        onView(withId(R.id.eventPrice)).perform(replaceText("50.00"));
//        onView(withId(R.id.editNumberOfMembers)).perform(replaceText("100"));
//        onView(withId(R.id.editTextEventDescription)).perform(replaceText("This is a test event."));
//        onView(withId(R.id.geo_toggle)).perform(click()); // Toggle geolocation switch
//        onView(withId(R.id.editWaitlistCap)).perform(replaceText("20"));
//
//        // Click on Confirm
//        onView(withId(R.id.buttonCreateEvent)).perform(click());
    }


    @Test
    public void testOrganizerLinkNavigatesToOrganizerMainPage() {
        // Verify navigation to AdminHomepageActivity
        onView(withId(R.id.create_event_button)).perform(click());
    }


    @Test
    public void testCreateEvent() {
        onView(withId(R.id.editTextEventName)).perform(click());


        // Interact with the OrganizerCreateEvent form
        onView(withId(R.id.editTextEventName)).perform(ViewActions.typeText("Sample Event"));
        onView(withId(R.id.eventStartTime)).perform(replaceText("2024-12-10"));
        onView(withId(R.id.eventEndTime)).perform(replaceText("2024-12-12"));
        onView(withId(R.id.registrationDeadline)).perform(replaceText("2024-12-08"));
        onView(withId(R.id.eventPrice)).perform(replaceText("50.00"));
        onView(withId(R.id.editNumberOfMembers)).perform(replaceText("100"));
        onView(withId(R.id.editTextEventDescription)).perform(replaceText("This is a test event."));
        onView(withId(R.id.geo_toggle)).perform(click()); // Toggle geolocation switch
        onView(withId(R.id.editWaitlistCap)).perform(replaceText("20"));

        // Click on Confirm
        onView(withId(R.id.buttonCreateEvent)).perform(click());

        // Add an assertion if needed (example: check if a confirmation message appears)
        // onView(withText("Event Created")).check(matches(isDisplayed()));
    }

}
