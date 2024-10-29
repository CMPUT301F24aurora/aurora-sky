package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerCreateEvent extends AppCompatActivity {

    private static final String TAG = "OrganizerCreateEvent";

    private EditText eventDateTime, eventName, eventNumberOfPeople, eventDescription;
    private Button organizerCreateEvent;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_create_event);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize EditText and Buttons
        eventName = findViewById(R.id.editTextEventName);
        eventDateTime = findViewById(R.id.editTextDateTime);
        eventNumberOfPeople = findViewById(R.id.editNumberOfMembers);
        eventDescription = findViewById(R.id.editTextEventDescription);
        organizerCreateEvent = findViewById(R.id.buttonCreateEvent);

        organizerCreateEvent.setOnClickListener(v -> {
            saveEventDetails();
        });
    }

    private void saveEventDetails() {
        String name = eventName.getText().toString().trim();
        String dateTime = eventDateTime.getText().toString().trim();
        String numofPeople = eventNumberOfPeople.getText().toString().trim();
        String description = eventDescription.getText().toString().trim();

        if (name.isEmpty() || dateTime.isEmpty() || numofPeople.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add input validation
        Integer numPeople;
        try {
            numPeople = Integer.parseInt(numofPeople);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number of people", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(name, dateTime, numPeople, description);

        event.saveToFirestore(new SaveEventCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(OrganizerCreateEvent.this, "Event created successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Event created successfully.");

                // Navigate to another activity if necessary
                Intent intent = new Intent(OrganizerCreateEvent.this, OrganizerMainPage.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerCreateEvent.this, "Error saving event", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error writing document", e);
            }
        });

    }
}
