package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class EntrantsEventsActivity extends AppCompatActivity {

    private ImageButton profileIcon;
    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private TextView noEventsText;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrants_events_page);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.menu_button);

        //Set up the toggle for the navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.entrant_nav) {
                    Toast.makeText(EntrantsEventsActivity.this, "You are on the entrant page", Toast.LENGTH_SHORT).show();
                    // Add your navigation logic here
                } else if (id == R.id.organizer_nav) {
                    Intent intent = new Intent(EntrantsEventsActivity.this, OrganizerMainPage.class);
                    startActivity(intent);
                }
                drawerLayout.closeDrawers(); // Close drawer after selection
                return true;
            }
        });

        // Initialize RecyclerView and TextView
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
        noEventsText = findViewById(R.id.no_events_text);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize event list and adapter
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList);
        eventsRecyclerView.setAdapter(eventAdapter);

        // Load events into RecyclerView
        loadEvents();

        // Initialize profile icon button
        profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(v -> {
            Intent oldIntent = getIntent();
            Entrant entrant = (Entrant) oldIntent.getSerializableExtra("entrant_data");
            Intent intent = new Intent(EntrantsEventsActivity.this, EntrantProfileActivity.class);
            intent.putExtra("entrant_data", entrant);
            startActivity(intent);
        });
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

}
