package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantEventDetailsActivity extends AppCompatActivity {

    private TextView eventTitle, eventDescription, eventDate, eventCapacity;
    private Button registerButton;
    private Event event;
    private Entrant entrant;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_event_details);

        initializeViews();
        getIntentData();
        displayEventDetails();
        setupRegisterButton();

        // Check if we should automatically register
        boolean shouldSignUp = getIntent().getBooleanExtra("sign_up", false);
        if (shouldSignUp) {
            registerForEvent();
        }
    }

    private void initializeViews() {
        eventTitle = findViewById(R.id.event_title);
        eventDescription = findViewById(R.id.event_description);
        eventDate = findViewById(R.id.event_date);
        eventCapacity = findViewById(R.id.event_capacity);
        registerButton = findViewById(R.id.register_button);
    }

    private void getIntentData() {
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
    }

    private void displayEventDetails() {
        if (event != null) {
            eventTitle.setText(event.getName());
            eventDescription.setText(event.getDescription());
            eventDate.setText("Date: " + event.getEventDate());
            eventCapacity.setText("Capacity: " + event.getNumPeople());
        } else {
            Toast.makeText(this, "Event data is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRegisterButton() {
        if (event != null && event.isEntrantRegistered(entrant)) {
            registerButton.setText("Registered");
            registerButton.setEnabled(false);
        } else {
            registerButton.setOnClickListener(v -> registerForEvent());
        }
    }

    private void registerForEvent() {
        if (entrant == null) {
            Toast.makeText(this, "Entrant data is missing. Please log in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event == null) {
            Toast.makeText(this, "Event data is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the register button to prevent multiple clicks
        registerButton.setEnabled(false);

        event.addEntrantToWaitingList(entrant, new WaitingListCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(EntrantEventDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                    registerButton.setText("Registered");
                    registerButton.setEnabled(false);
                    navigateToWaitingList();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(EntrantEventDetailsActivity.this, "Failed to register: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    registerButton.setEnabled(true);
                });
            }
        });
    }

    private void navigateToWaitingList() {
        Intent intent = new Intent(EntrantEventDetailsActivity.this, EntrantWaitingListActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", entrant);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshEventData();
    }

    private void refreshEventData() {
        if (event != null) {
            String eventHash = event.getQR_code();
            db.collection("events").document(eventHash).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            event = documentSnapshot.toObject(Event.class);
                            displayEventDetails();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching event data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("EntrantEventDetails", "Error fetching event", e);
                    });
        }
    }
}