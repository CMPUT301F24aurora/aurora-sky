package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
    private Organizer currentOrganizer;

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

        // Fetch the current organizer based on device ID
        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        // Assume getDeviceId() returns the device ID
        Organizer.getOrganizerByDeviceId(deviceId, new GetOrganizerCallback() {
            @Override
            public void onOrganizerFound(Organizer organizer) {
                currentOrganizer = organizer;
                //Toast.makeText(OrganizerMainPage.this, "Organizer found: " + organizer.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOrganizerNotFound() {
                Toast.makeText(OrganizerMainPage.this, "Organizer not found.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(OrganizerMainPage.this, "Error fetching organizer.", Toast.LENGTH_SHORT).show();
            }
        });

        // Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.organizer_nav) {
                Toast.makeText(OrganizerMainPage.this, "You are on the Organizer page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.entrant_nav) {
                Intent organizerIntent = new Intent(OrganizerMainPage.this, EntrantsEventsActivity.class);
                startActivity(organizerIntent);
            } else if (id == R.id.map_nav) {
                Intent organizerIntent = new Intent(OrganizerMainPage.this, MapActivity.class);
                startActivity(organizerIntent);
            } else if (id == R.id.qr_code_nav) {
                Intent organizerIntent = new Intent(OrganizerMainPage.this, QRScannerActivity.class);
                startActivity(organizerIntent);
            }
            drawerLayout.closeDrawers(); // Close drawer after selection
            return true;
        });

        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerCreateEvent.class);
            intent.putExtra("organizer", currentOrganizer); // Pass the organizer data if needed
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
