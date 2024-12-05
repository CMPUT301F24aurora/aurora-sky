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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private TextView eventNameTextView, eventDescriptionTextView, eventCapacityTextView, eventDateTextView, eventEndDate, registrationDeadline, price;
    private ImageView eventPosterImageView;
    private Button adminEvRemove;
    private Button adminQrRemove;
    private Button adminPosterRemove;
    private FirebaseFirestore db;
    private Event event;
    private String eventHash;

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
        eventEndDate = findViewById(R.id.end_event_date);
        registrationDeadline = findViewById(R.id.registration_event_date);
        price = findViewById(R.id.price_event);
        adminEvRemove = findViewById(R.id.admin_ev_remove);
        adminQrRemove = findViewById(R.id.admin_ev_remove_qr);
        adminPosterRemove = findViewById(R.id.admin_ev_remove_poster);
        event = (Event) getIntent().getSerializableExtra("event_data");

        // Get the intent extras and set the text views
        String eventName = event.getEventName();
        String eventDescription = event.getDescription();
        String eventCapacity = String.valueOf(event.getNumPeople());
        String eventDate = event.getEventStartDate();
        String eventImageUrl = event.getImage_url();
        eventHash = event.getQR_code();

        // Set values to TextViews
        eventNameTextView.setText(eventName);
        eventDescriptionTextView.setText("Description:\n" + eventDescription);
        eventCapacityTextView.setText("Capacity: " + eventCapacity);
        eventEndDate.setText("End Date: " + event.getEventEndDate());
        registrationDeadline.setText("Registration Deadline: " + event.getRegistrationDeadline());
        price.setText("Price: " + event.getEventPrice());
        eventDateTextView.setText("Start Date: " + eventDate);

        // Load the poster image if URL is not null or empty
        if (eventImageUrl != null && !eventImageUrl.isEmpty()) {
            eventPosterImageView.setVisibility(View.VISIBLE);
            adminPosterRemove.setVisibility(View.VISIBLE);
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

        adminQrRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteQrHash(eventHash);
            }
        });

        if (adminPosterRemove.getVisibility() == View.VISIBLE){
            adminPosterRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteEventPoster(eventHash, eventImageUrl);
                }
            });
        }

        db = FirebaseFirestore.getInstance();
    }

    public void deleteEventPoster(String eventId, String eventPosterUrl){
        DocumentReference docRef = db.collection("events").document(eventId);

        docRef.update("image_url", null)
                .addOnSuccessListener(aVoid -> {
                    // Successfully deleted the image_url field
                    Log.i("AdminDeletePoster", "image_url deleted successfully!");

                    // Delete the Image from Firebase Storage
                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(eventPosterUrl); // Replace with your image URL

                    storageRef.delete().addOnSuccessListener(aVoid1 -> {
                        // Successfully deleted the image from Firebase Storage
                        Toast.makeText(AdminViewEventsContent.this, "Event Poster deleted successfully!",  Toast.LENGTH_SHORT).show();

                        // Refresh page with no poster image and no remove poster button
                        Intent intent = new Intent(this, AdminViewEventsContent.class);
                        intent.putExtra("event_data", event);
                        finish();
                        startActivity(intent);
                    }).addOnFailureListener(exception -> {
                        // Handle any errors that occurred
                        Log.e("AdminDeletePoster","Error deleting image from Firebase Storage: " + exception.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to delete the URL
                    Log.e("AdminDeletePoster","Error deleting image_url: " + e.getMessage());
                });

    }
    public void deleteQrHash(final String currentQrHash) {
        DocumentReference oldDocRef = db.collection("events").document(currentQrHash);
        oldDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                String newQrHash = event.generateQRHash();
                Map<String, Object> docData = document.getData();
                docData.put("qr_code", newQrHash);
                createNewDocumentWithNewId(oldDocRef, docData, newQrHash, currentQrHash);

                // Update the current Event Hash
                event.setQR_code(newQrHash);
                eventHash = newQrHash;
                Toast.makeText(AdminViewEventsContent.this, "Event QR deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("AdminDeleteQrHash", "Error fetching event with that qrHash!");
            }
        });
    }

    private void createNewDocumentWithNewId(DocumentReference oldDocRef, Map<String, Object> data, String newDocId, String currentQrHash) {
        oldDocRef.delete().addOnSuccessListener(aVoid -> {
                    // Successfully deleted the old document
                    Log.i("AdminDeleteQrHash", "Successfully deleted old Event doc");
                })
                .addOnFailureListener(e -> {
                    // Handle the failure
                    Log.e("AdminDeleteQrHash", "Couldn't delete old Event doc!");
                });
        DocumentReference newDocRef = db.collection("events").document(newDocId);

        newDocRef.set(data).addOnSuccessListener(aVoid -> {
            // Successfully created the new document
            updateOrganizers(currentQrHash, newDocId);
            Log.i("AdminDeleteQrHash", "Successfully created new Event doc");
        }).addOnFailureListener(e -> {
            // Handle the failure
            Log.e("AdminDeleteQrHash", "Couldn't create new Event doc!");
        });
    }


    private void updateOrganizers(String oldQrHash, String newQrHash) {

        // Query the organizers collection
        db.collection("organizers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    List<String> eventHashes = (List<String>) document.get("eventHashes");

                    if (eventHashes != null && eventHashes.contains(oldQrHash)) {
                        // Replace the old QR hash with the new one
                        eventHashes.remove(oldQrHash);
                        eventHashes.add(newQrHash);

                        // Update the document with the new list
                        document.getReference().update("eventHashes", eventHashes)
                                .addOnFailureListener(e -> {
                                    // Handle failure to update organizer document
                                    Log.e("AdminDeleteQrHash", "Couldn't update the eventHashes list!");
                                });
                    }
                }
            } else {
                // Handle failure to query organizers collection
                Log.e("AdminDeleteQrHash", "Couldn't fetch organizers!");
            }
        });
    }

    /**
     * Deletes the event from the database and updates the UI.
     * Retrieves the event ID and hash from the intent and deletes the event document from Firestore.
     * If successful, navigates back to the event list and removes the event hash from the organizer.
     */
    private void deleteEvent() {
        CollectionReference eventsRef = db.collection("events");
        String eventId = event.getQR_code();

        eventsRef.document(eventId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    adminEvRemove.setVisibility(View.GONE);
                    removeEventHashFromOrganizer(eventId);
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
