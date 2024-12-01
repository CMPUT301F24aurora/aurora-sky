package com.example.lotteryapp;


import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;


public class OrganizerTest {


    private Organizer organizer;


    @Before
    public void setUp() {
        organizer = new Organizer("1", "Test Organizer", "test@organizer.com");
    }


    @Test
    public void testOrganizerDetails() {
        assertEquals("Test Organizer", organizer.getName());
        assertEquals("test@organizer.com", organizer.getEmail());
    }
}
