package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EntrantWaitingListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_waiting_list);

        TextView eventDetailsTextView = findViewById(R.id.event_details_textview);
        Button leaveEventButton = findViewById(R.id.leave_event_button);

        // Get the Event object from the Intent
        Intent intent = getIntent();
        Event event = (Event) intent.getSerializableExtra("event_data");

        // Get the Entrant object from the Intent (assuming it's passed from the previous activity)
        Entrant entrant = (Entrant) intent.getSerializableExtra("entrant_data");

        if (event != null) {
            String eventName = event.getName();
            String message = "You are registered for the " + eventName;
            eventDetailsTextView.setText(message);
        } else {
            eventDetailsTextView.setText("Event details not found.");
        }

        leaveEventButton.setOnClickListener(v -> {
            if (event != null && entrant != null) {
                event.removeEntrantFromWaitingList(entrant, new WaitingListCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(EntrantWaitingListActivity.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EntrantWaitingListActivity.this, EntrantsEventsActivity.class);
                        intent.putExtra("event_data", event);
                        intent.putExtra("entrant_data", entrant);
                        startActivity(intent);

                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(EntrantWaitingListActivity.this, "Failed to leave event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(EntrantWaitingListActivity.this, "Event or entrant data is missing", Toast.LENGTH_SHORT).show();
            }
        });
    }
}