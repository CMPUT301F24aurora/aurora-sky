package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class OrganizerMainPage extends AppCompatActivity implements OrgEventAdapter.OnEventClickListener {

    private Button createEventButton;
    private Button facilityButton; // Ensure this is declared
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Organizer currentOrganizer;
    private Entrant entrant;
    private RecyclerView orgRecyclerView;
    private OrgEventAdapter orgEventAdapter;
    private List<Event> eventList;
    private DBManagerEvent dbManagerEvent;

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

        // Initialize RecyclerView and Adapter
        orgRecyclerView = findViewById(R.id.org_events_recycler_view);
        orgRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        orgEventAdapter = new OrgEventAdapter(eventList, this); // Pass 'this' as the listener
        orgRecyclerView.setAdapter(orgEventAdapter);
        // Set the adapter here

        // Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.organizer_nav) {
                Toast.makeText(OrganizerMainPage.this, "You are on the Organizer page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.entrant_nav) {
                Intent organizerIntent = new Intent(OrganizerMainPage.this, EntrantsEventsActivity.class);
                organizerIntent.putExtra("entrant_data", entrant);
                organizerIntent.putExtra("organizer_data", currentOrganizer);
                startActivity(organizerIntent);
            } else if (id == R.id.qr_code_nav) {
                // Hide the QR code navigation item
                Intent qrScannerIntent = new Intent(OrganizerMainPage.this, QRScannerActivity.class);
                qrScannerIntent.putExtra("entrant_data", entrant);
                qrScannerIntent.putExtra("organizer_data", currentOrganizer);
                startActivity(qrScannerIntent);

            } else if (id == R.id.invitation_nav) {
                Intent organizerIntent = new Intent(OrganizerMainPage.this, InvitationActivity.class);
                organizerIntent.putExtra("entrant_data", entrant);
                organizerIntent.putExtra("entrant_id", entrant.getId());
                organizerIntent.putExtra("organizer_data", currentOrganizer);
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

        dbManagerEvent = new DBManagerEvent();
        loadEvents();
    }

    private void setupFacilityButton() {
        if (currentOrganizer != null) { // Check if currentOrganizer is initialized
            Log.w("Yes", currentOrganizer.getName());

            if (currentOrganizer.getFacility_id() == null) {
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
                    intent.putExtra("organizer_data", currentOrganizer);
                    intent.putExtra("entrant_data", entrant);
                    intent.putExtra("facility_id", currentOrganizer.getFacility_id());
                    startActivity(intent);
                });
            }
        } else {
            Log.e("Error", "Current organizer is null");
        }
    }

    private void loadEvents() {
        dbManagerEvent.getEventsByQRCodes(currentOrganizer.getEventHashes(), new GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                if (eventList.isEmpty()) {
                    orgRecyclerView.setVisibility(View.GONE);
                } else {
                    orgRecyclerView.setVisibility(View.VISIBLE);
                }
                orgEventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                orgRecyclerView.setVisibility(View.GONE);
            }
        });
        orgEventAdapter.notifyDataSetChanged(); // Refresh adapter to show added events
    }

//    private void loadEvents() {
//        dbManagerEvent.getEventsByQRCodes(currentOrganizer.getEventHashes(), new DBManagerEvent.GetEventsCallback() {  // Use the full path for the callback
//            @Override
//            public void onSuccess(List<Event> events) {
//                eventList.clear();
//                eventList.addAll(events);
//                if (eventList.isEmpty()) {
//                    orgRecyclerView.setVisibility(View.GONE);
//                } else {
//                    orgRecyclerView.setVisibility(View.VISIBLE);
//                }
//                orgEventAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                orgRecyclerView.setVisibility(View.GONE);
//            }
//        });
//        orgEventAdapter.notifyDataSetChanged(); // Refresh adapter to show added events
//    }


    public void onEventClick(Event event) {
        Intent eventDetailsIntent = new Intent(this, OrganizerEventDetailsActivity.class);
        eventDetailsIntent.putExtra("event_data", event);
        eventDetailsIntent.putExtra("organizer_data", currentOrganizer);
        eventDetailsIntent.putExtra("entrant_data", entrant);
        startActivity(eventDetailsIntent);
    }

}
