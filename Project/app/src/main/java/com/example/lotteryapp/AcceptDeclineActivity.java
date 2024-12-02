package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AcceptDeclineActivity extends AppCompatActivity {
    private Button acceptButton;
    private Button declineButton;
    private Entrant entrant;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_decline_invitation);

        acceptButton = findViewById(R.id.accept_button);
        declineButton = findViewById(R.id.decline_button);

        // Retrieve data passed from InvitationActivity
        Intent intent = getIntent();
        Intent oldIntent = getIntent();
        entrant = (Entrant) oldIntent.getSerializableExtra("entrant_data");
        event = (Event) oldIntent.getSerializableExtra("event_data");
        Log.d("AcceptDeclineActivity", "eventId"+entrant.getId()+" "+ event.getQR_code());

        // Handle "Accept" button click
        acceptButton.setOnClickListener(v -> {
            if (event.getQR_code() != null && entrant.getId() != null) {
                addEntrantToEvent(event.getQR_code(), entrant.getId());
            } else {
                Toast.makeText(this, "Event or Entrant data missing!", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle "Decline" button click
        declineButton.setOnClickListener(v -> {
            // Navigate back to InvitationActivity without updating Firebase
            if (event.getQR_code() != null && entrant.getId() != null) {
                addEntrantToCancelledEntrant(event.getQR_code(), entrant.getId());
            } else {
                Toast.makeText(this, "Event or Entrant data missing!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEntrantToEvent(String eventId, String entrantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the event document by adding entrantId to the final_entrants list
        db.collection("events")
                .document(eventId)
                .update("finalEntrants", com.google.firebase.firestore.FieldValue.arrayUnion(entrantId))
                .addOnSuccessListener(aVoid -> {
                    // Navigate back to InvitationActivity after success
                    Toast.makeText(this, "Successfully accepted the invitation!", Toast.LENGTH_SHORT).show();
                    Intent backIntent = new Intent(AcceptDeclineActivity.this, InvitationActivity.class);
                    backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(backIntent);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(this, "Failed to accept the invitation. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void addEntrantToCancelledEntrant(String eventId, String entrantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the event document by adding entrantId to the final_entrants list
        db.collection("events")
                .document(eventId)
                .update("cancelledEntrants", com.google.firebase.firestore.FieldValue.arrayUnion(entrantId),
                        "selectedEntrants", com.google.firebase.firestore.FieldValue.arrayRemove(entrantId))
                .addOnSuccessListener(aVoid -> {
                    // Navigate back to InvitationActivity after success
                    Toast.makeText(this, "Did not accept the invitation", Toast.LENGTH_SHORT).show();
                    Intent backIntent = new Intent(AcceptDeclineActivity.this, InvitationActivity.class);
                    backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(backIntent);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(this, "Failed to accept the invitation. Try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
