package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrants_events_page);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        profileButton = findViewById(R.id.profile_icon);  // Assuming the ImageButton has id 'profile_button'

        // Set up profile button click listener to navigate to EntrantProfileEditActivity
        profileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(EntrantsEventsActivity.this, EntrantProfileActivity.class);
            startActivity(profileIntent);
        });

        // Open drawer when menu button is clicked
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.entrant_nav) {
                Toast.makeText(EntrantsEventsActivity.this, "You are on the entrant page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.organizer_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, OrganizerMainPage.class);
                startActivity(organizerIntent);
            } else if (id == R.id.map_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, MapActivity.class);
                startActivity(organizerIntent);
            } else if (id == R.id.qr_code_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, QRScannerActivity.class);
                startActivity(organizerIntent);
            }
            drawerLayout.closeDrawers(); // Close drawer after selection
            return true;
        });

        // Initialize RecyclerView and TextView
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
        noEventsText = findViewById(R.id.no_events_text);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize event list and adapter with click listener
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this); // Pass `this` as the listener
        eventsRecyclerView.setAdapter(eventAdapter);

        // Load events into RecyclerView
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the event list when the activity is resumed
        loadEvents();
    }

    private void loadEvents() {
        // Call the Firestore retrieval method from Event class
        Event.getEventsFromFirestore(new GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);

                // Check if events were retrieved and update visibility of RecyclerView and noEventsText
                if (eventList.isEmpty()) {
                    noEventsText.setVisibility(View.VISIBLE); // Show "No events available" message
                    eventsRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
                } else {
                    noEventsText.setVisibility(View.GONE); // Hide "No events available" message
                    eventsRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView with events
                }
                eventAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onFailure(Exception e) {
                // Handle the error (you can display a Toast or log the error)
                noEventsText.setVisibility(View.VISIBLE);
                eventsRecyclerView.setVisibility(View.GONE);
                noEventsText.setText("Failed to load events. Please try again.");
            }
        });
    }

    @Override
    public void onEventClick(Event event) {
        // Navigate to the event details page
        Intent eventDetailsIntent = new Intent(EntrantsEventsActivity.this, EntrantEventDetailsActivity.class);

        // Fetch the entrant data from Firestore
        Entrant.getEntrant(this, new GetEntrantCallback() {
            @Override
            public void onEntrantFound(Entrant fetchedEntrant) {
                // Successfully retrieved entrant, put the event and entrant data into the intent
                eventDetailsIntent.putExtra("event_data", event); // Assuming Event implements Serializable
                eventDetailsIntent.putExtra("entrant_data", fetchedEntrant); // Pass the fetched entrant data

                // Start the event details activity
                startActivity(eventDetailsIntent);
            }

            @Override
            public void onEntrantNotFound(Exception e) {
                // Handle case when entrant data is not found
                //Log.w(TAG, "Entrant not found", e);
                Toast.makeText(EntrantsEventsActivity.this, "Entrant not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                // Handle error while fetching entrant data
                //Log.e(TAG, "Error fetching entrant", e);
                Toast.makeText(EntrantsEventsActivity.this, "Error fetching entrant", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
