package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerMainPageTest {

    private void sleep(){
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
    public void testOrganizerButtonNavigatesToOrganizerMainPage() {
        // Click the "I am Organizer" button
        onView(withId(R.id.organizerButton)).perform(click());
        sleep();
        // Verify that the OrganizerMainPage is displayed
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed())); // Adjust this ID as needed
    }

    @Test
    public void testCreateEventButtonNavigatesToOrganizerCreateEvent() {
        // First, ensure we are on the Organizer Main Page
        onView(withId(R.id.organizerButton)).perform(click()); // Navigate to Organizer Main Page first
        sleep();
        // Click the "Create Event" button
        onView(withId(R.id.create_event_button)).perform(click());
        sleep();
        // Verify that the OrganizerCreateEvent activity is displayed
        onView(withId(R.id.editTextEventName)).check(matches(isDisplayed())); // Adjust this ID as needed
    }

    @Test
    public void testInputFieldsInOrganizerCreateEvent() {
        // First, ensure we are on the Organizer Main Page and navigate to Create Event
        onView(withId(R.id.organizerButton)).perform(click()); // Navigate to Organizer Main Page first
        sleep();
        onView(withId(R.id.create_event_button)).perform(click()); // Click Create Event button
        sleep();

        // Input event details into fields directly
        onView(withId(R.id.editTextEventName)).perform(setText("Sample Event"));

        // Directly set the date and time in the EditText
        String predefinedDateTime = "2023-12-31 10:00"; // Adjust this format as needed
        onView(withId(R.id.editTextDateTime)).perform(setText(predefinedDateTime));

        // Input number of people and description directly
        onView(withId(R.id.editNumberOfMembers)).perform(setText("100"));
        onView(withId(R.id.editTextEventDescription)).perform(setText("This is a sample event description."));

        // Click the "Create Event" button to submit the form
        onView(withId(R.id.buttonCreateEvent)).perform(click());
        sleep();

        // Verify that a success message appears (you may want to check for a specific view or toast)
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));    }

    private ViewAction setText(final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(EditText.class)); // Ensure it's an EditText
            }

            @Override
            public String getDescription() {
                return "Set text on EditText";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                ((EditText) view).setText(text); // Directly set the text
            }
        };
    }

    @After
    public void tearDown() {
        // Any necessary cleanup can go here
    }
}
