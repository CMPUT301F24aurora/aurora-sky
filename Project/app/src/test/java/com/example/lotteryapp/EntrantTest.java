package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntrantTest {

    private Entrant entrant;

    @Before
    public void setUp() {
        // Initialize an Entrant object before each test
        entrant = new Entrant("E123", "John Doe", "johndoe@example.com", "http://example.com/image.jpg");
    }

    @Test
    public void testGetProfileImageUrl() {
        // Test getter for profile image URL
        assertEquals("http://example.com/image.jpg", entrant.getProfileImageUrl());
    }

    @Test
    public void testSetProfileImageUrl() {
        // Test setter for profile image URL
        entrant.setProfileImageUrl("http://example.com/new_image.jpg");
        assertEquals("http://example.com/new_image.jpg", entrant.getProfileImageUrl());
    }

    @Test
    public void testRemoveProfileImage() {
        // Test removing the profile image
        entrant.removeProfileImage();
        assertNull(entrant.getProfileImageUrl());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Test equality and hashCode
        Entrant sameEntrant = new Entrant("E123", "Jane Doe", "janedoe@example.com", null);
        Entrant differentEntrant = new Entrant("E124", "John Smith", "johnsmith@example.com", null);

        assertEquals(entrant, sameEntrant);
        assertNotEquals(entrant, differentEntrant);
        assertEquals(entrant.hashCode(), sameEntrant.hashCode());
        assertNotEquals(entrant.hashCode(), differentEntrant.hashCode());
    }

    @Test
    public void testSaveToFirestore() {
        // Mock FirebaseFirestore instance
        FirebaseFirestore mockFirestore = FirebaseFirestore.getInstance();
        Entrant.setDatabase(mockFirestore);

        // Create a callback and test success case
        SaveEntrantCallback mockCallback = new SaveEntrantCallback() {
            @Override
            public void onSuccess() {
                assertTrue(true); // Success case
            }

            @Override
            public void onFailure(Exception e) {
                fail("Save to Firestore should not fail.");
            }
        };

        // Save entrant and trigger the callback
        entrant.saveToFirestore(mockCallback);
    }

    @Test
    public void testCheckEntrantExists() {
        // Mock a Firestore instance
        FirebaseFirestore mockFirestore = FirebaseFirestore.getInstance();
        Entrant.setDatabase(mockFirestore);

        // Create a callback for checking entrant existence
        EntrantCheckCallback mockCallback = new EntrantCheckCallback() {
            @Override
            public void onEntrantExists(Entrant entrant) {
                assertNotNull(entrant);
            }

            @Override
            public void onEntrantNotFound() {
                fail("Entrant should exist.");
            }

            @Override
            public void onError(Exception e) {
                fail("Error should not occur.");
            }
        };

        // Check if the entrant exists
        Entrant.checkEntrantExists("sampleDeviceId", mockCallback);
    }

    @Test
    public void testDisplayUserInfo() {
        // Test displaying user info
        entrant.displayUserInfo(); // Check console output manually if required
    }
}
