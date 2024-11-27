package com.example.lotteryapp;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class EntrantsEventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private TextView noEventsText;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Entrant entrant;
    private Organizer organizer;
    private ImageButton profileIcon;
    private RefreshDataManager refreshDataManager;
    private DBManagerEvent dbManagerEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrants_events_page);
        refreshDataManager = new RefreshDataManager(this);
        dbManagerEvent = new DBManagerEvent();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.menu_button);

        // Retrieve Entrant data
        Intent oldIntent = getIntent();
        entrant = (Entrant) oldIntent.getSerializableExtra("entrant_data");
        organizer = (Organizer) oldIntent.getSerializableExtra("organizer_data");

        // Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.entrant_nav) {
                Toast.makeText(EntrantsEventsActivity.this, "You are on the entrant page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.organizer_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, OrganizerMainPage.class);
                organizerIntent.putExtra("entrant_data", entrant);
                organizerIntent.putExtra("organizer_data", organizer);
                startActivity(organizerIntent);
            } else if (id == R.id.map_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, MapActivity.class);
                startActivity(organizerIntent);
            } else if (id == R.id.qr_code_nav) {
                Intent qrScannerIntent = new Intent(EntrantsEventsActivity.this, QRScannerActivity.class);
                qrScannerIntent.putExtra("entrant_data", entrant);
                qrScannerIntent.putExtra("organizer_data", organizer);
                startActivity(qrScannerIntent);
            } else if (id == R.id.invitation_nav) {
                Intent qrScannerIntent = new Intent(EntrantsEventsActivity.this, InvitationActivity.class);
                qrScannerIntent.putExtra("entrant_data", entrant);
                qrScannerIntent.putExtra("organizer_data", organizer);
                startActivity(qrScannerIntent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Initialize RecyclerView and TextView
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
        noEventsText = findViewById(R.id.no_events_text);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize event list and adapter with click listener
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this);
        eventsRecyclerView.setAdapter(eventAdapter);

        profileIcon = findViewById(R.id.profile_icon);
        setupProfileIcon();
        // Load events into RecyclerView
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the event list when the activity is resumed
        refreshData();
    }

    private void refreshData() {
        if (refreshDataManager == null) {
            Toast.makeText(EntrantsEventsActivity.this, "Refresh manager not initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (entrant != null) {
            refreshDataManager.refreshData(entrant.getId(), new RefreshDataManager.DataRefreshListener() {
                @Override
                public void onDataRefreshed(Entrant refreshedEntrant, Organizer refreshedOrganizer) {
                    entrant = refreshedEntrant;
                    organizer = refreshedOrganizer;
                    //Toast.makeText(EntrantsEventsActivity.this, "Data refreshed successfully", Toast.LENGTH_SHORT).show();
                    loadEvents(); // Reload events if necessary
                }

                @Override
                public void onError(Exception e) {
                    //Toast.makeText(EntrantsEventsActivity.this, "Error refreshing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(EntrantsEventsActivity.this, "No entrant data to refresh.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEvents() {
        dbManagerEvent.getEventsFromFirestore(new GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                if (eventList.isEmpty()) {
                    noEventsText.setVisibility(View.VISIBLE);
                    eventsRecyclerView.setVisibility(View.GONE);
                } else {
                    noEventsText.setVisibility(View.GONE);
                    eventsRecyclerView.setVisibility(View.VISIBLE);
                }
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                noEventsText.setVisibility(View.VISIBLE);
                eventsRecyclerView.setVisibility(View.GONE);
                noEventsText.setText("Failed to load events. Please try again.");
            }
        });
    }

    private void setupProfileIcon() {
        if (profileIcon != null && entrant != null && entrant.getImage_url() != null) {
            Glide.with(this)
                    .load(entrant.getImage_url()) // Load the URL from the entrant object
                    .placeholder(R.drawable.ic_profile_photo) // Optional: Placeholder while loading
                    .error(R.drawable.ic_profile_photo) // Optional: Fallback image on error
                    .circleCrop() // Makes the image circular
                    .into(profileIcon);

            profileIcon.setOnClickListener(v -> {
                Intent profileIntent = new Intent(EntrantsEventsActivity.this, EntrantProfileActivity.class);
                profileIntent.putExtra("entrant_data", entrant);
                profileIntent.putExtra("organizer_data", organizer);
                startActivity(profileIntent);
            });
        }
    }

    @Override
    public void onEventClick(Event event) {
        Intent eventDetailsIntent = new Intent(EntrantsEventsActivity.this, EntrantEventDetailsActivity.class);
        eventDetailsIntent.putExtra("eventId", event.getQR_code());
        eventDetailsIntent.putExtra("entrantId", entrant.getId());
        eventDetailsIntent.putExtra("organizer_data", organizer);
        eventDetailsIntent.putExtra("sign_up", false);
        startActivity(eventDetailsIntent);
    }


}