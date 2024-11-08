package com.example.lotteryapp;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseSelectEvents {

    @Rule
    public ActivityScenarioRule<AdminViewEditEventsActivity> activityScenarioRule =
            new ActivityScenarioRule<>(AdminViewEditEventsActivity.class);

    @Test
    public void testEventsLoadedAndFilter() {
        // Check if the RecyclerView is displayed
        onView(withId(R.id.admin_ev_list))
                .check(matches(isDisplayed()));

        // Check if the SearchView is displayed
        onView(withId(R.id.admin_search_ev))
                .check(matches(isDisplayed()));

        // Input text into the SearchView and check if the adapter is filtered
        onView(withId(R.id.admin_search_ev))
                .perform(typeText("Event Name"), closeSoftKeyboard());

        // Add more assertions based on the UI changes after filtering
    }
}
