package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity displayed after sampling process is completed.
 * Provides navigation to different lists of entrants based on their status.
 */
public class AfterSampling extends AppCompatActivity {
    private String eventId;
    private Event event;
    private Organizer organizer;
    private Entrant entrant;

    /**
     * Initializes the activity, sets up UI components, and handles button click events.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this Bundle contains the data it most recently supplied
     *                           in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_sampling_list);

        // Get event data from Intent
        event = (Event) getIntent().getSerializableExtra("event_data");
        eventId = event.getQR_code();
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");

        Log.d("AfterSampling", "Selected Entrants: " + event.getSelectedEntrants().toString());

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
     * Navigates to the RecyclerListActivity with specified parameters.
     *
     * @param title The title to be displayed in the RecyclerListActivity.
     * @param collection The name of the collection to be displayed.
     */
    private void navigateToRecyclerList(String title, String collection) {
        Intent intent = new Intent(AfterSampling.this, RecyclerListActivity.class);
        Log.d("AfterSampling", "Navigating to RecyclerListActivity");
        intent.putExtra("title", title);
        intent.putExtra("collection", collection);
        intent.putExtra("eventId", eventId);
        intent.putExtra("event_data", event);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("entrant_data", entrant);
        startActivity(intent);
    }

    /**
     * Called when the activity resumes from a paused state.
     * Refreshes the event details to ensure up-to-date information.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshEventDetails();
    }

    /**
     * Refreshes the event details by fetching the latest data from the database.
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
                    Log.e("AfterSampling", "Error refreshing event details: " + e.getMessage());
                }
            });
        }
    }
}