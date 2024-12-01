package com.example.lotteryapp;


import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;


public class EntrantTest {


    private Entrant entrant;


    @Before
    public void setUp() {
        entrant = new Entrant("1", "Test Entrant", "test@entrant.com");
    }


    @Test
    public void testDisplayUserInfo() {
        entrant.displayUserInfo(); // Verify no exceptions
    }


    @Test
    public void testImageUrl() {
        entrant.setImage_url("http://example.com/image.jpg");
        assertEquals("http://example.com/image.jpg", entrant.getImage_url());
    }
}
