package com.example.lotteryapp;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.UiController;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseSelectEvents {

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

    @Ignore
    @Test
    public void testAdminLinkNavigatesToAdminView() {
        // Click the admin_link button in MainActivity
        onView(withId(R.id.admin_link)).perform(click());
        sleep();

        // Verify navigation to AdminHomepageActivity
        onView(withId(R.id.admin_v_ev)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminEvButtonNavigatesToAdminEventEntrants() {
        // Navigate to AdminHomepageActivity
        onView(withId(R.id.admin_link)).perform(click());
        sleep();

        // Click the admin_v_ev button
        onView(withId(R.id.admin_v_ev)).perform(click());
        sleep();

        // Verify navigation to AdminSearchEventActivity
        onView(withId(R.id.admin_search_ev)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchEventCard() {
        // Navigate to AdminHomepageActivity
        onView(withId(R.id.admin_link)).perform(click());
        idlingResource.setIdleState(false); // Set to busy
        onView(withId(R.id.admin_v_ev)).perform(click());
        idlingResource.setIdleState(true); // Set to idle when the action is done

        //onView(withId(R.id.admin_search_ev)).perform(setText("e1"));
        idlingResource.setIdleState(true); // Set to idle after typing

        // Perform a scroll to the item with "e1" and check if it's displayed
        onView(withId(R.id.admin_ev_list))
                .perform(CustomScrollActions.scrollToHolder(hasDescendant(withText("e1"))));

        onView(allOf(withId(R.id.event_name), withText("e1"))).check(matches(isDisplayed()));
    }

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
        // Unregister IdlingResource
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    private void sleep() {
        try {
            Thread.sleep(2000); // Wait for 2 seconds to ensure transition completes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

