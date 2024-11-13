package com.example.lotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Event event;
    private Entrant entrant;
    private Organizer organizer;
    private Button enter_waiting;
    private Button leave_waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);

        initializeViews();
        getIntentData();
        displayEventDetails();
        setupEnterWaiting();
    }

    private void initializeViews() {
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDate = findViewById(R.id.event_date);
        eventCapacity = findViewById(R.id.event_capacity);
        enter_waiting = findViewById(R.id.enter_waiting);
    }

    private void getIntentData() {
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
    }

    private void displayEventDetails() {
        if (event != null) {
            eventTitle.setText(event.getEventName());
            eventDescription.setText(event.getDescription());
            eventDate.setText("Date: " + event.getEventDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());
        } else {
            Toast.makeText(this, "Event data is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupEnterWaiting(){
        enter_waiting.setOnClickListener(view -> {
            // Show a toast message when the button is clicked
            Toast.makeText(EntrantEventDetailsActivity.this, "Entered Waiting List", Toast.LENGTH_SHORT).show();
        });
    }
}
