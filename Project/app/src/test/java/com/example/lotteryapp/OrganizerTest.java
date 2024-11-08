package com.example.lotteryapp;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class OrganizerTest {

    private Organizer organizer;

    @Before
    public void setUp() {
        // Initialize Organizer object
        organizer = new Organizer("O123", "Event Organizer", "organizer@example.com");
    }

    @Test
    public void testAddEventHash() {
        // Simulate adding an event hash
        String eventHash = "event123";
        organizer.addEventHash(eventHash, new Organizer.AddEventCallback() {
            @Override
            public void onEventAdded(String eventHash) {
                assertTrue(organizer.getEventHashes().contains(eventHash));
            }

            @Override
            public void onError(Exception e) {
                fail("Should not fail when adding a valid event hash.");
            }
        });
    }

    @Test
    public void testRemoveEventHash() {
        // Simulate removing an event hash
        String eventHash = "event123";
        organizer.getEventHashes().add(eventHash); // Pre-add the hash
        organizer.removeEventHash(eventHash);
        assertFalse(organizer.getEventHashes().contains(eventHash));
    }

    @Test
    public void testGetAndSetEventHashes() {
        // Test getting and setting event hashes
        List<String> eventHashes = Arrays.asList("event1", "event2", "event3");
        organizer.setEventHashes(eventHashes);
        assertEquals(eventHashes, organizer.getEventHashes());
    }

    @Test
    public void testSaveToFirestore() {
        // Test saving organizer to Firestore (mock example)
        organizer.saveToFirestore(new Organizer.SaveOrganizerCallback() {
            @Override
            public void onSuccess() {
                assertTrue(true); // Save succeeded
            }

            @Override
            public void onFailure(Exception e) {
                fail("Save should not fail.");
            }
        });
    }

    @Test
    public void testGetOrganizerByDeviceId() {
        // Test fetching an organizer by device ID (mock example)
        Organizer.getOrganizerByDeviceId("device123", new Organizer.GetOrganizerCallback() {
            @Override
            public void onOrganizerFound(Organizer organizer) {
                assertNotNull(organizer);
                assertEquals("O123", organizer.getId());
            }

            @Override
            public void onOrganizerNotFound() {
                fail("Organizer should be found.");
            }

            @Override
            public void onError(Exception e) {
                fail("Should not throw an error when fetching organizer.");
            }
        });
    }

    @Test
    public void testFacilityIdManagement() {
        // Test setting and getting the facility ID
        organizer.setFacility_id("facility123");
        assertEquals("facility123", organizer.getFacility_id());
    }

    @Test
    public void testDisplayUserInfo() {
        // Test displaying user info
        organizer.displayUserInfo(); // Manually verify console output
    }

    @Test
    public void testHasOrganizerPermissions() {
        // Test checking organizer permissions
        organizer.setRole("organizer");
        assertTrue(organizer.hasOrganizerPermissions());

        organizer.setRole("user");
        assertFalse(organizer.hasOrganizerPermissions());
    }
}
