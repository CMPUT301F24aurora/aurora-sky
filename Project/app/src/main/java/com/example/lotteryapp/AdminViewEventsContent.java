package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The AdminViewEventsContent class displays the details of a selected event and allows admin users to delete it.
 *
 * @see AppCompatActivity
 * @see FirebaseFirestore
 * @see AdminViewEditEventsActivity
 * @version v1
 *
 * Author: Team Aurora
 */
public class AdminViewEventsContent extends AppCompatActivity {

    private TextView eventNameTextView, eventDescriptionTextView, eventCapacityTextView, eventDateTextView;
    private ImageView eventPosterImageView;
    private Button adminEvRemove;
    private FirebaseFirestore db;
    private Event event;

    /**
     * Called when the activity is first created.
     * This method sets up the layout and initializes views and Firestore.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view_events_content);

        // Initialize views
        eventNameTextView = findViewById(R.id.admin_event_name);
        eventDescriptionTextView = findViewById(R.id.admin_event_description);
        eventCapacityTextView = findViewById(R.id.admin_event_capacity);
        eventDateTextView = findViewById(R.id.admin_event_date);
        eventPosterImageView = findViewById(R.id.admin_event_poster);
        adminEvRemove = findViewById(R.id.admin_ev_remove);
        event = (Event) getIntent().getSerializableExtra("event_data");

        // Get the intent extras and set the text views
        String eventName = event.getEventName();
        String eventDescription = event.getDescription();
        String eventCapacity = String.valueOf(event.getNumPeople());
        String eventDate = event.getEventDate();
        String eventImageUrl = event.getImage_url();

        // Set values to TextViews
        eventNameTextView.setText(eventName);
        eventDescriptionTextView.setText(eventDescription);
        eventCapacityTextView.setText("Capacity: " + eventCapacity);
        eventDateTextView.setText("Date: " + eventDate);

        // Load the poster image if URL is not null or empty
        if (eventImageUrl != null && !eventImageUrl.isEmpty()) {
            eventPosterImageView.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(eventImageUrl)
                    .placeholder(R.drawable.ic_profile_photo)  // Optional placeholder image
                    .error(R.drawable.ic_profile_photo)        // Optional error image
                    .into(eventPosterImageView);
        } else {
            eventPosterImageView.setVisibility(View.GONE);
        }

        // Set up the remove button
        adminEvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvent();
            }
        });

        db = FirebaseFirestore.getInstance();
    }

    /**
     * Deletes the event from the database and updates the UI.
     * Retrieves the event ID and hash from the intent and deletes the event document from Firestore.
     * If successful, navigates back to the event list and removes the event hash from the organizer.
     */
    private void deleteEvent() {
        CollectionReference eventsRef = db.collection("events");
        String eventId = getIntent().getStringExtra("eventId");
        String eventHash = getIntent().getStringExtra("eventHash");

        eventsRef.document(eventId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    adminEvRemove.setVisibility(View.GONE);
                    removeEventHashFromOrganizer(eventHash);
                    Toast.makeText(AdminViewEventsContent.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();

                    // Navigate back to Events List
                    Intent intent = new Intent(AdminViewEventsContent.this, AdminViewEditEventsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AdminViewEventsContent.this, "Error deleting event: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Removes the event hash from the organizer's document in the database.
     * Queries the "organizers" collection for documents containing the specified event hash and removes it.
     *
     * @param eventHash the hash of the event to be removed
     */
    private void removeEventHashFromOrganizer(String eventHash) {
        db.collection("organizers")
                .whereArrayContains("eventHashes", eventHash)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first document (assuming unique eventHashes per organizer)
                        DocumentSnapshot organizerDocument = queryDocumentSnapshots.getDocuments().get(0);
                        String organizerId = organizerDocument.getId();

                        // Remove the eventHash from the eventHashes array
                        db.collection("organizers").document(organizerId)
                                .update("eventHashes", FieldValue.arrayRemove(eventHash))
                                .addOnSuccessListener(aVoid -> Log.d("OrganizerEventHash", "Event hash removed successfully"))
                                .addOnFailureListener(e -> Log.e("OrganizerEventHash", "Error removing event hash", e));
                    } else {
                        Log.d("OrganizerEventHash", "No organizer found with the specified event hash");
                    }
                })
                .addOnFailureListener(e -> Log.e("OrganizerEventHash", "Error querying organizers", e));
    }
}
