package com.example.lotteryapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details); // Ensure this matches your XML layout

        // Initialize views
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDate = findViewById(R.id.event_date);
        eventCapacity = findViewById(R.id.event_capacity);
        registerButton = findViewById(R.id.register_button);

        // Get the event data from the intent
        Event event = (Event) getIntent().getSerializableExtra("event_data");

        // Set event data to views
        if (event != null) {
            eventTitle.setText(event.getName());
            eventDescription.setText(event.getDescription());
            eventDate.setText("Date: " + event.getEventDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());

            // Set up register button action
            registerButton.setOnClickListener(v -> {
                // Add logic to register the entrant for the event
                Toast.makeText(this, "Registered for " + event.getName(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Event data is missing", Toast.LENGTH_SHORT).show();
        }
    }
}
