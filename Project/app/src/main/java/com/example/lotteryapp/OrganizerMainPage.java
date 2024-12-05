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

/**
 * The main activity for the organizer's page that allows managing events, facilities, and navigating to other sections.
 * <p>
 * This activity includes the ability to view and create events, manage facilities, and navigate to different pages such as the Entrant events, QR code scanner, and invitations.
 * It uses a navigation drawer for navigation and a RecyclerView for displaying a list of events.
 * </p>
 *
 * @see AppCompatActivity
 * @see OrgEventAdapter
 * @see Organizer
 * @see Entrant
 */
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

    /**
     * Called when the activity is first created.
     * <p>
     * This method initializes the views, sets up the event adapter for the RecyclerView, handles navigation drawer items,
     * and loads the events for the current organizer.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down,
     *                           this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState(Bundle)}.
     *                           Otherwise, it is null.
     * @return void
     * @see #setupFacilityButton()
     * @see #loadEvents()
     */
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

            } else if (id == R.id.main_nav) {
                Intent mainIntent = new Intent(OrganizerMainPage.this, MainActivity.class);
                mainIntent.putExtra("entrant_data", entrant);
                mainIntent.putExtra("organizer_data", currentOrganizer);
                startActivity(mainIntent);

            }else if (id == R.id.invitation_nav) {
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

    /**
     * Sets up the facility button to either create or manage a facility based on the current organizer's data.
     * <p>
     * If the organizer does not have a facility, the button allows creating a new facility.
     * If the organizer already has a facility, the button allows managing the existing facility.
     * </p>
     *
     * @return void
     * @see OrganizerFacilityActivity
     */
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

    /**
     * Loads the events associated with the current organizer from the database.
     * This method fetches the events using the organizer's event hashes and updates the RecyclerView with the results.
     * <p>
     * If the events list is empty, the RecyclerView is hidden. Otherwise, the events are displayed.
     * </p>
     *
     * @return void
     * @see DBManagerEvent
     * @see Event
     */
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


    /**
     * Called when an event item is clicked in the RecyclerView.
     * <p>
     * This method starts the {@link OrganizerEventDetailsActivity} and passes the event details to display in the new activity.
     * </p>
     *
     * @param event The {@link Event} object that was clicked.
     * @return void
     * @see OrganizerEventDetailsActivity
     */
    public void onEventClick(Event event) {
        Intent eventDetailsIntent = new Intent(this, OrganizerEventDetailsActivity.class);
        eventDetailsIntent.putExtra("event_data", event);
        eventDetailsIntent.putExtra("organizer_data", currentOrganizer);
        eventDetailsIntent.putExtra("entrant_data", entrant);
        startActivity(eventDetailsIntent);
    }

}
