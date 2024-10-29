package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EntrantsEventsActivity extends AppCompatActivity {

    private ImageButton profileIcon;
    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private TextView noEventsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrants_events_page);

        // Initialize RecyclerView
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
        noEventsText = findViewById(R.id.no_events_text);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Temporary event list; replace with Firestore retrieval if needed
        eventList = new ArrayList<>();
        eventList.add(new Event("Music Fest", "2024-10-30", 200, "Enjoy live music from top artists!"));
        eventList.add(new Event("Food Carnival", "2024-11-05", 150, "Taste food from around the world!"));

        // Set adapter
        eventAdapter = new EventAdapter(eventList);
        eventsRecyclerView.setAdapter(eventAdapter);
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
        // Simulate loading events, e.g., from Firestore or local list
        // For demonstration, let's assume an empty list (you would replace this with real data loading logic)
        eventList.clear();

        // Update RecyclerView and TextView visibility
        if (eventList.isEmpty()) {
            noEventsText.setVisibility(View.VISIBLE); // Show "No events available" message
            eventsRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
        } else {
            noEventsText.setVisibility(View.GONE); // Hide "No events available" message
            eventsRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView with events
            eventAdapter.notifyDataSetChanged(); // Notify adapter of data change
        }
    }
}
