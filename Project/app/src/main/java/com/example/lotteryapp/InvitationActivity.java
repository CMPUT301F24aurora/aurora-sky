package com.example.lotteryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class InvitationActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener{

    private RecyclerView eventsRecyclerView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Entrant entrant;
    private Organizer organizer;
    private ImageButton profileIcon;
    private RefreshDataManager refreshDataManager;
    private DBManagerEvent dbManagerEvent;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private TextView noEventsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.menu_button);
        noEventsText = findViewById(R.id.no_events_text);
        refreshDataManager = new RefreshDataManager(this);
        dbManagerEvent = new DBManagerEvent();

        // Retrieve Entrant data
        Intent oldIntent = getIntent();
        entrant = (Entrant) oldIntent.getSerializableExtra("entrant_data");
        organizer = (Organizer) oldIntent.getSerializableExtra("organizer_data");

        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.invitation_nav) {
                Toast.makeText(InvitationActivity.this, "You are on the invitation page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.organizer_nav) {
                Intent organizerIntent = new Intent(InvitationActivity.this, OrganizerMainPage.class);
                organizerIntent.putExtra("entrant_data", entrant);
                organizerIntent.putExtra("organizer_data", organizer);
                startActivity(organizerIntent);
            } else if (id == R.id.map_nav) {
                Intent organizerIntent = new Intent(InvitationActivity.this, MapActivity.class);
                startActivity(organizerIntent);
            } else if (id == R.id.qr_code_nav) {
                Intent qrScannerIntent = new Intent(InvitationActivity.this, QRScannerActivity.class);
                qrScannerIntent.putExtra("entrant_data", entrant);
                qrScannerIntent.putExtra("organizer_data", organizer);
                startActivity(qrScannerIntent);
            } else if (id == R.id.entrant_nav) {
                Intent entrantIntent = new Intent(InvitationActivity.this, EntrantsEventsActivity.class);
                entrantIntent.putExtra("entrant_data", entrant);
                entrantIntent.putExtra("organizer_data", organizer);
                startActivity(entrantIntent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Initialize RecyclerView and TextView
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
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

    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Step 1: Fetch entrant's selected event
        db.collection("entrants")
                .whereEqualTo("id", entrant.getId())
                .get()
                .addOnSuccessListener(entrantQuerySnapshot -> {
                    if (!entrantQuerySnapshot.isEmpty()) {
                        DocumentSnapshot entrantDoc = entrantQuerySnapshot.getDocuments().get(0);
                        String selectedEventQrCode = entrantDoc.getString("selected_event");

                        if (selectedEventQrCode == null || selectedEventQrCode.isEmpty()) {
                            noEventsText.setVisibility(View.VISIBLE);
                            eventsRecyclerView.setVisibility(View.GONE);
                            noEventsText.setText("No event selected.");
                            return;
                        }

                        // Step 2: Fetch event details based on the selected event's QR code
                        db.collection("events")
                                .whereEqualTo("qr_code", selectedEventQrCode)
                                .get()
                                .addOnSuccessListener(eventQuerySnapshot -> {
                                    List<Event> matchedEvents = new ArrayList<>();
                                    for (DocumentSnapshot eventDoc : eventQuerySnapshot.getDocuments()) {
                                        Event event = eventDoc.toObject(Event.class); // Assuming you have an Event model
                                        if (event != null) {
                                            matchedEvents.add(event);
                                        }
                                    }

                                    // Step 3: Update UI with the matched event
                                    eventList.clear();
                                    eventList.addAll(matchedEvents);

                                    if (eventList.isEmpty()) {
                                        noEventsText.setVisibility(View.VISIBLE);
                                        eventsRecyclerView.setVisibility(View.GONE);
                                        noEventsText.setText("No matching event found.");
                                    } else {
                                        noEventsText.setVisibility(View.GONE);
                                        eventsRecyclerView.setVisibility(View.VISIBLE);
                                    }

                                    eventAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    noEventsText.setVisibility(View.VISIBLE);
                                    eventsRecyclerView.setVisibility(View.GONE);
                                    noEventsText.setText("Failed to load event details. Please try again.");
                                });
                    } else {
                        noEventsText.setVisibility(View.VISIBLE);
                        eventsRecyclerView.setVisibility(View.GONE);
                        noEventsText.setText("Entrant not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    noEventsText.setVisibility(View.VISIBLE);
                    eventsRecyclerView.setVisibility(View.GONE);
                    noEventsText.setText("Failed to load entrant details. Please try again.");
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
                Intent profileIntent = new Intent(InvitationActivity.this, EntrantProfileActivity.class);
                profileIntent.putExtra("entrant_data", entrant);
                profileIntent.putExtra("organizer_data", organizer);
                startActivity(profileIntent);
            });
        }
    }

    @Override
    public void onEventClick(Event event) {
        Intent eventDetailsIntent = new Intent(InvitationActivity.this, AcceptDeclineActivity.class);
        eventDetailsIntent.putExtra("event_data", event);
        eventDetailsIntent.putExtra("entrant_data", entrant);
        eventDetailsIntent.putExtra("organizer_data", organizer);
        eventDetailsIntent.putExtra("sign_up", false);
        startActivity(eventDetailsIntent);
    }
}