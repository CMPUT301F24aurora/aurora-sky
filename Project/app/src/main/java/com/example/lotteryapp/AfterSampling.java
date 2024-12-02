package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The {@code AfterSampling} class represents an activity that facilitates navigation to different lists
 * of entrants categorized based on their status (e.g., waitlist, selected, cancelled, final chosen).
 * It also provides functionality to refresh event details.
 *
 * @see AppCompatActivity
 * @see Event
 * @see Organizer
 * @see Entrant
 */
public class AfterSampling extends AppCompatActivity {
    private String eventId;
    private Event event;
    private Organizer organizer;
    private Entrant entrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_sampling_list);

        // Get event ID from Intent
        event = (Event) getIntent().getSerializableExtra("event_data");
        eventId = event.getQR_code();
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");


        Log.d("", event.getSelectedEntrants().toString());

        // Initialize buttons
        Button waitlistButton = findViewById(R.id.buttonWaitlistEntrants);
        Button selectedButton = findViewById(R.id.buttonSelectedEntrants);
        Button cancelledButton = findViewById(R.id.buttonCancelledEntrants);
        Button chosenButton = findViewById(R.id.buttonChosenEntrants);

        // Set onClickListeners for navigation
        waitlistButton.setOnClickListener(v -> navigateToRecyclerList("Entrants in the waitlist", "waitingList"));
        selectedButton.setOnClickListener(v -> navigateToRecyclerList("Selected entrants", "selectedEntrants"));
        cancelledButton.setOnClickListener(v -> navigateToRecyclerList("Cancelled Entrants", "cancelledEntrants"));
        chosenButton.setOnClickListener(v -> navigateToRecyclerList("Final chosen entrants", "finalEntrants"));
    }


    /**
     * Navigates to the {@link RecyclerListActivity} to display a list of entrants based on the specified category.
     *
     * @param title      the title of the entrant list (e.g., "Entrants in the waitlist")
     * @param collection the category of entrants to display (e.g., "waitingList", "selectedEntrants")
     * @see RecyclerListActivity
     * @see Intent
     */
    private void navigateToRecyclerList(String title, String collection) {
        Intent intent = new Intent(AfterSampling.this, RecyclerListActivity.class);
        Log.d("AfterSampling","done");
        intent.putExtra("title", title);
        intent.putExtra("collection", collection);
        intent.putExtra("eventId", eventId);
        intent.putExtra("event_data", event);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("entrant_data", entrant);
        startActivity(intent);
    }

    /**
     * Called when the activity enters the foreground. Refreshes event details from the database.
     *
     * @see #refreshEventDetails()
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshEventDetails();
    }

    /**
     * Refreshes the event details from the database by querying using the QR code associated with the event.
     *
     * @throws IllegalStateException if the event QR code is null
     * @see DBManagerEvent
     */
    private void refreshEventDetails() {
        if (event != null && event.getQR_code() != null) {
            new DBManagerEvent().getEventByQRCode(event.getQR_code(), new DBManagerEvent.GetEventCallback() {
                @Override
                public void onSuccess(Event updatedEvent) {
                    event = updatedEvent;
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AfterSampling.this, "Failed to refresh event details", Toast.LENGTH_SHORT).show();
                    Log.e("OrganizerEventDetails", "Error refreshing event details: " + e.getMessage());
                }
            });
        }
    }
}
