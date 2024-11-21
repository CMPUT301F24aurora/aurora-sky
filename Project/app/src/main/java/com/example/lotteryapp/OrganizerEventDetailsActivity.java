package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrganizerEventDetailsActivity extends AppCompatActivity {
    private TextView eventTitleTextView;
    private TextView eventDateTextView;
    private TextView eventDescriptionTextView;
    private Button viewEntrantsButton;
    private ImageView eventPosterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_details);

        // Initialize views
        eventTitleTextView = findViewById(R.id.event_title);
        eventDateTextView = findViewById(R.id.event_date);
        eventDescriptionTextView = findViewById(R.id.event_description);
        viewEntrantsButton = findViewById(R.id.view_entrants_button);
        eventPosterView = findViewById(R.id.poster);

        // Get event data from the intent
        Event selectedEvent = (Event) getIntent().getSerializableExtra("event_data");

        if (selectedEvent != null) {
            // Populate views with event details
            eventTitleTextView.setText(selectedEvent.getEventName());
            eventDateTextView.setText("Date: " + selectedEvent.getEventDate());
            eventDescriptionTextView.setText(selectedEvent.getDescription());

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
