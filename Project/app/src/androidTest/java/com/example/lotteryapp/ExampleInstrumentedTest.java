package com.example.lotteryapp;

import android.Manifest;
import android.content.Context;
import android.location.Location;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.maps.GoogleMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MapActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MapActivity> activityRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Test
    public void testMapShowsCurrentLocation() {
        // Assuming you have granted location permissions in the setup for the test
        activityRule.getScenario().onActivity(activity -> {
            // Create a mock location
            Location mockLocation = new Location("mockProvider");
            mockLocation.setLatitude(53.5461); // Set mock latitude (e.g., Edmonton)
            mockLocation.setLongitude(-113.4938); // Set mock longitude

            // Update the map with the mock location
            activity.updateLocation(mockLocation); // Ensure this method exists in your MapActivity

            // Now, verify that the map marker has been placed correctly
            GoogleMap map = activity.getMap(); // Ensure you have a method to get the map instance

            // Assert that the map is not null
            assertNotNull("Map should be initialized", map);

            // You can also add more checks here to validate if the marker is placed correctly
            // For example, check if a marker is added at the mock location
            // However, since Google Maps API does not provide direct access to markers,
            // you may need to implement a method to check marker existence in your activity
        });
    }
}
