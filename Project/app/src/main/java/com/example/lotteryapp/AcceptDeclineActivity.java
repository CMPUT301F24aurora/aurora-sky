package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

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

        // Perform both operations: add to finalEntrants and remove from selectedEntrants
        db.collection("events")
                .document(eventId)
                .update(
                        "finalEntrants", FieldValue.arrayUnion(entrantId),        // Add to finalEntrants
                        "selectedEntrants", FieldValue.arrayRemove(entrantId)     // Remove from selectedEntrants
                )
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
                    Log.e("AcceptDeclineActivity", "Error updating entrant status", e);
                });

        db.collection("entrants")
                .document(entrantId)
                .update("selected_event", com.google.firebase.firestore.FieldValue.arrayRemove(eventId))
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


    private void addEntrantToCancelledEntrant(String eventId, String entrantId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events").document(eventId)
                .update(
                        "selectedEntrants", FieldValue.arrayRemove(entrantId), // First, remove from selectedEntrants
                        "cancelledEntrants", FieldValue.arrayUnion(entrantId)  // Then, add to cancelledEntrants
                )
                .addOnSuccessListener(aVoid -> {
                    // Proceed to sampling only after successful removal and addition
                    db.collection("events").document(eventId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    List<String> waitingList = (List<String>) documentSnapshot.get("waitingList");
                                    if (waitingList != null && !waitingList.isEmpty()) {
                                        String newEntrantId = waitingList.get(0);  // Select the first entrant from the waiting list

                                        // Update selectedEntrants and waitingList in a single call
                                        db.collection("events").document(eventId)
                                                .update(
                                                        "selectedEntrants", FieldValue.arrayUnion(newEntrantId),
                                                        "waitingList", FieldValue.arrayRemove(newEntrantId)
                                                )
                                                .addOnSuccessListener(replaceVoid -> {
                                                    Toast.makeText(this, "Invitation declined and replacement selected.", Toast.LENGTH_SHORT).show();
                                                    navigateBackToInvitation();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Failed to select replacement entrant.", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(this, "No entrants available in the waiting list.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to load event details.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update entrant status.", Toast.LENGTH_SHORT).show();
                });

        db.collection("entrants")
                .document(entrantId)
                .update("cancelled_event", com.google.firebase.firestore.FieldValue.arrayUnion(eventId),
                        "selected_event", com.google.firebase.firestore.FieldValue.arrayRemove(eventId))
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


    private void navigateBackToInvitation() {
        Intent backIntent = new Intent(AcceptDeclineActivity.this, InvitationActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(backIntent);
    }

}
