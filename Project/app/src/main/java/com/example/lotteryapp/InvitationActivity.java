package com.example.lotteryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

/**
 * Activity for displaying event invitations to the user.
 * This activity shows a list of events the user has been invited to and allows navigation to other app features.
 */
public class InvitationActivity extends AppCompatActivity implements EventInvitationAdapter.OnEventClickListener {

    private RecyclerView eventsRecyclerView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Entrant entrant;
    private Organizer organizer;
    private ImageButton profileIcon;
    private RefreshDataManager refreshDataManager;
    private DBManagerEvent dbManagerEvent;
    private EventInvitationAdapter eventInvitationAdapter;
    private List<Event> eventList;
    private TextView noEventsText;

    /**
     * Initializes the activity, sets up UI components, and loads event data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle).
     */
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
            } else if (id == R.id.qr_code_nav) {
                Intent qrScannerIntent = new Intent(InvitationActivity.this, QRScannerActivity.class);
                qrScannerIntent.putExtra("entrant_data", entrant);
                qrScannerIntent.putExtra("organizer_data", organizer);
                startActivity(qrScannerIntent);
            } else if (id == R.id.main_nav) {
                Intent mainIntent = new Intent(InvitationActivity.this, MainActivity.class);
//                mainIntent.putExtra("entrant_data", entrant);
//                mainIntent.putExtra("organizer_data", currentOrganizer);
                startActivity(mainIntent);

            }else if (id == R.id.entrant_nav) {
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
        eventInvitationAdapter = new EventInvitationAdapter(eventList, this);
        eventsRecyclerView.setAdapter(eventInvitationAdapter);

        profileIcon = findViewById(R.id.profile_icon);
        setupProfileIcon();
        loadEvents();
    }

    /**
     * Loads events from the database and updates the UI accordingly.
     */
    private void loadEvents() {
        dbManagerEvent.getEventsByQRCodes(entrant.getSelected_event(), new GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                if (eventList.isEmpty()) {
                    eventsRecyclerView.setVisibility(View.GONE);
                    noEventsText.setVisibility(View.VISIBLE);
                } else {
                    eventsRecyclerView.setVisibility(View.VISIBLE);
                    noEventsText.setVisibility(View.GONE);
                }
                eventInvitationAdapter.updateData(events);
            }

            @Override
            public void onFailure(Exception e) {
                eventsRecyclerView.setVisibility(View.GONE);
            }
        });
        eventInvitationAdapter.notifyDataSetChanged(); // Refresh adapter to show added events
    }

    /**
     * Sets up the profile icon with the user's image and click listener.
     */
    private void setupProfileIcon() {
        if (profileIcon != null && entrant != null && entrant.getImage_url() != null) {
            Glide.with(this)
                    .load(entrant.getImage_url())
                    .placeholder(R.drawable.ic_profile_photo)
                    .error(R.drawable.ic_profile_photo)
                    .circleCrop()
                    .into(profileIcon);

            profileIcon.setOnClickListener(v -> {
                Intent profileIntent = new Intent(InvitationActivity.this, EntrantProfileActivity.class);
                profileIntent.putExtra("entrant_data", entrant);
                profileIntent.putExtra("organizer_data", organizer);
                startActivity(profileIntent);
            });
        }
    }

    /**
     * Handles click events on individual events in the list.
     *
     * @param event The Event object that was clicked.
     */
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