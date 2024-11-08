package com.example.lotteryapp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class EntrantTest {

    private Entrant entrant1;
    private Entrant entrant2;

    @Before
    public void setUp() {
        // Initialize Entrant objects
        entrant1 = new Entrant("1", "John Doe", "john@example.com");
        entrant2 = new Entrant("2", "Jane Doe", "jane@example.com", "123-456-7890");
    }

    @Test
    public void testEntrantInitialization() {
        assertEquals("John Doe", entrant1.getName());
        assertEquals("john@example.com", entrant1.getEmail());
    }

    @Test
    public void testEntrantEquality() {
        Entrant entrant3 = new Entrant("1", "John Doe", "john@example.com");
        assertEquals(entrant1, entrant3); // entrant1 and entrant3 have the same ID, so they should be equal
        assertNotEquals(entrant1, entrant2); // Different IDs, so they should not be equal
    }

    @Test
    public void testHashCode() {
        Entrant entrant3 = new Entrant("1", "John Doe", "john@example.com");
        assertEquals(entrant1.hashCode(), entrant3.hashCode());
        assertNotEquals(entrant1.hashCode(), entrant2.hashCode());
    }


}
