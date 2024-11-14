package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class OrganizerMainPage extends AppCompatActivity {

    private Button createEventButton;
    private Button facilityButton; // Ensure this is declared
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Organizer currentOrganizer;
    private Entrant entrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_main_page);

        currentOrganizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.menu_button);

        createEventButton = findViewById(R.id.create_event_button);
        facilityButton = findViewById(R.id.create_facility_button);
        setupFacilityButton();


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
            intent.putExtra("organizer_data", currentOrganizer);
            intent.putExtra("entrant_data", entrant);
            startActivity(intent);
        });
    }

    private void setupFacilityButton() {
        if (currentOrganizer != null) { // Check if currentOrganizer is initialized
            Log.w("Yes", currentOrganizer.getName());

            if (currentOrganizer.getFacility_id() == null || currentOrganizer.getFacility_id().isEmpty()) {
                // Organizer does not have a facility, show "Create Facility"
                facilityButton.setText("Create Facility");
                facilityButton.setOnClickListener(v -> {
                    Intent intent = new Intent(OrganizerMainPage.this, OrganizerFacilityActivity.class);
                    intent.putExtra("organizer_data", currentOrganizer);
                    intent.putExtra("entrant_data", entrant);
                    startActivity(intent);
                });
            } else {
                // Organizer has a facility, show "Manage Facility"
                facilityButton.setText("Manage Facility");
                facilityButton.setOnClickListener(v -> {
                    Intent intent = new Intent(OrganizerMainPage.this, OrganizerFacilityActivity.class);
                    intent.putExtra("organizer", currentOrganizer);
                    intent.putExtra("facility_id", currentOrganizer.getFacility_id());
                    startActivity(intent);
                });
            }
        } else {
            Log.e("Error", "Current organizer is null");
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (currentOrganizer != null) {
//            fetchCurrentOrganizer(); // Refresh organizer data
//        }
//    }
}