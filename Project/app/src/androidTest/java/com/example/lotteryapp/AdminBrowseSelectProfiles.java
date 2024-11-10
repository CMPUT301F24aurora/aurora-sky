package com.example.lotteryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AdminBrowseSelectProfiles {
    private SimpleIdlingResource idlingResource;

    private void sleep() {
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

        idlingResource = new SimpleIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);
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

    @Test
    public void testSearchEntrantCard() {
        // Navigate to AdminHomepageActivity
        onView(withId(R.id.admin_link)).perform(click());
        idlingResource.setIdleState(false); // Set to busy
        onView(withId(R.id.admin_v_pro)).perform(click());
        idlingResource.setIdleState(true); // Set to idle when the action is done

        onView(withId(R.id.entrants_search_view)).perform(setText("noname"));
        idlingResource.setIdleState(true); // Set to idle after typing

        onView(withId(R.id.ev_entrants_lv))
                .perform(CustomScrollActions.scrollToHolder(hasDescendant(withText("noname"))));

        onView(allOf(withId(R.id.profile_name_value), withText("noname"))).check(matches(isDisplayed()));
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
}
