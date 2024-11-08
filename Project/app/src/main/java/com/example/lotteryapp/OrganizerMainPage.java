package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class OrganizerMainPage extends AppCompatActivity {

    private Button createEventButton;
    private Button createFacilityButton;
    private Button manageFacilitiesButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_main_page);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.menu_button);

        createEventButton = findViewById(R.id.create_event_button);
        createFacilityButton = findViewById(R.id.create_facility_button);
        manageFacilitiesButton = findViewById(R.id.manage_facilities_button);

        //Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.organizer_nav) {
                Toast.makeText(OrganizerMainPage.this, "You are on the Organizer page", Toast.LENGTH_SHORT).show();
                // Add your navigation logic here
            } else if (id == R.id.entrant_nav) {
                Intent organizerIntent = new Intent(OrganizerMainPage.this, EntrantsEventsActivity.class);
                startActivity(organizerIntent);
            } else if (id == R.id.map_nav) {
                Intent organizerIntent = new Intent(OrganizerMainPage.this, MapActivity.class);
                startActivity(organizerIntent);
            }
            drawerLayout.closeDrawers(); // Close drawer after selection
            return true;
        });

        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerCreateEvent.class);
            startActivity(intent);
        });

        createFacilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerFacilityActivity.class);
            startActivity(intent);
        });

        manageFacilitiesButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerFacilityListActivity.class);
            startActivity(intent);
        });
    }
}


