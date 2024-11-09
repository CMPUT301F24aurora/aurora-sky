package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseSelectEvents {

    private void sleep(){
        // Sleep for assurance (not recommended for production tests)
        try {
            Thread.sleep(2000); // Wait for 2 seconds to ensure transition completes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Rule
    public ActivityScenarioRule<AdminViewEditEventsActivity> activityScenarioRule =
            new ActivityScenarioRule<>(AdminViewEditEventsActivity.class);

    @Test
    public void testAddAndFilterEvent() {
        Intents.init();

        // Input text into the fields to add an event
        onView(withId(R.id.event_name_input))
                .perform(typeText("Test Event Name"), closeSoftKeyboard());

        onView(withId(R.id.event_date_input))
                .perform(typeText("2023-10-01"), closeSoftKeyboard());

        onView(withId(R.id.event_description_input))
                .perform(typeText("This is a test event description"), closeSoftKeyboard());

        // Click the button to add the event
        onView(withId(R.id.add_event_button))
                .perform(click());

        // Check if the event is displayed in the RecyclerView
        onView(withId(R.id.admin_ev_list))
                .check(matches(isDisplayed()));

        // Filter the list using the search view
        onView(withId(R.id.admin_search_ev))
                .perform(typeText("Test Event Name"), closeSoftKeyboard());

        // Verify the filtered event is displayed
        onView(withText("Test Event Name"))
                .check(matches(isDisplayed()));

        // Click on the event
        onView(withText("Test Event Name"))
                .perform(click());

        // Verify that the AdminViewEventsContent activity is started
        intended(hasComponent(AdminViewEventsContent.class.getName()));

        Intents.release();
    }
}
