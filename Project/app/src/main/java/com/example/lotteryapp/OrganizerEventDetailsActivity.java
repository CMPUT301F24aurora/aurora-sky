package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrganizerEventDetailsActivity extends AppCompatActivity {

    private TextView eventDetailsTextView;
    private Button viewEntrantsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_details);

        // Initialize views
        eventDetailsTextView = findViewById(R.id.eventDetailsTextView);
        viewEntrantsButton = findViewById(R.id.view_entrants_button);

        // Get event data from the intent
        Event selectedEvent = (Event) getIntent().getSerializableExtra("event_data");

        if (selectedEvent != null) {
            // Display event details
            eventDetailsTextView.setText("Event Name: " + selectedEvent.getEventName() +
                    "\nEvent Date: " + selectedEvent.getEventDate());

            viewEntrantsButton.setOnClickListener(v -> {
                Intent entrantsIntent = new Intent(OrganizerEventDetailsActivity.this, Sampling.class);
                entrantsIntent.putExtra("event_data", selectedEvent);
                startActivity(entrantsIntent);
            });
        } else {
            // Log error or show a message
            Log.e("OrganizerEventDetails", "Selected event is null!");
            finish(); // Close the activity
        }
    }
}
