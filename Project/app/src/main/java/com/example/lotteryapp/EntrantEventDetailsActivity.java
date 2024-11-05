package com.example.lotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Button registerButton;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);

        // Initialize views
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDate = findViewById(R.id.event_date);
        eventCapacity = findViewById(R.id.event_capacity);
        registerButton = findViewById(R.id.register_button);

        // Get the event data from the intent
        Event event = (Event) getIntent().getSerializableExtra("event_data");

        // Assuming "entrant" is passed from a previous activity
        Entrant entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");

        // Set event data to views
        if (event != null) {
            eventTitle.setText(event.getName());
            eventDescription.setText(event.getDescription());
            eventDate.setText("Date: " + event.getEventDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());

            // Set up register button action
            registerButton.setOnClickListener(v -> registerForEvent(entrant, event));
        } else {
            Toast.makeText(this, "Event data is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerForEvent(Entrant entrant, Event event) {
        if (entrant != null && event != null) {
            event.addEntrantToWaitingList(entrant, new WaitingListCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(EntrantEventDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EntrantEventDetailsActivity.this, "Failed to register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Entrant data is missing", Toast.LENGTH_SHORT).show();
        }
    }
}
