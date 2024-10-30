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
