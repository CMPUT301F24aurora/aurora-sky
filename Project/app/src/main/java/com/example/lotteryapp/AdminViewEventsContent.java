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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    private static final String TAG = "AdminViewEventsContent";

    private TextView eventNameTextView, eventDescriptionTextView, eventCapacityTextView, eventDateTextView,
            eventEndDate, registrationDeadline, price;
    private ImageView eventPosterImageView;
    private Button adminEvRemove, adminQrRemove, adminPosterRemove;

    private FirebaseFirestore db;
    private Event event;
    private String eventHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view_events_content);
        initializeViews();
        setupButtonListeners();

    }

    /**
     * Initializes all view components.
     */
    private void initializeViews() {
        db = FirebaseFirestore.getInstance();

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

        // Retrieve event data from the intent
        event = (Event) getIntent().getSerializableExtra("event_data");
    }

    /**
     * Sets up the event details on the UI components.
     */
    private void setupEventDetails() {
        if (event == null) return;

        eventHash = event.getQR_code();
        eventNameTextView.setText(event.getEventName());
        eventDescriptionTextView.setText("Description:\n" + event.getDescription());
        eventCapacityTextView.setText("Capacity: " + event.getNumPeople());
        eventEndDate.setText("End Date: " + event.getEventEndDate());
        registrationDeadline.setText("Registration Deadline: " + event.getRegistrationDeadline());
        price.setText("Price: " + event.getEventPrice());
        eventDateTextView.setText("Start Date: " + event.getEventStartDate());

        loadPosterImage();
    }

    /**
     * Loads the event poster image if available.
     */
    private void loadPosterImage() {
        String eventImageUrl = event.getImage_url();
        if (eventImageUrl != null && !eventImageUrl.isEmpty()) {
            eventPosterImageView.setVisibility(View.VISIBLE);
            adminPosterRemove.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(eventImageUrl)
                    .placeholder(R.drawable.ic_profile_photo)
                    .error(R.drawable.ic_profile_photo)
                    .into(eventPosterImageView);
        } else {
            eventPosterImageView.setVisibility(View.GONE);
            adminPosterRemove.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up the button click listeners for various actions.
     */
    private void setupButtonListeners() {
        adminEvRemove.setOnClickListener(v -> deleteEvent());
        adminQrRemove.setOnClickListener(v -> deleteQrHash(eventHash));
        adminPosterRemove.setOnClickListener(v -> {
            if (adminPosterRemove.getVisibility() == View.VISIBLE) {
                deleteEventPoster(eventHash, event.getImage_url());
            }
        });
    }

    /**
     * Deletes the event from Firestore and updates the UI.
     */
    private void deleteEvent() {
        db.collection("events").document(eventHash)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        removeEventHashFromOrganizer(eventHash);
                        Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                        navigateBackToEventList();
                    } else {
                        Toast.makeText(this, "Error deleting event: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Deletes the QR code and updates the document with a new hash.
     */
    private void deleteQrHash(final String currentQrHash) {
        DocumentReference docRef = db.collection("events").document(currentQrHash);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String newQrHash = event.generateQRHash();
                Map<String, Object> data = task.getResult().getData();
                data.put("qr_code", newQrHash);
                createNewEventDocument(docRef, data, newQrHash, currentQrHash);
            } else {
                Log.e(TAG, "Error fetching event for QR hash update.");
            }
        });
    }

    private void createNewEventDocument(DocumentReference oldDocRef, Map<String, Object> data, String newDocId, String oldQrHash) {
        oldDocRef.delete().addOnSuccessListener(aVoid -> Log.i(TAG, "Old event document deleted"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete old document."));

        db.collection("events").document(newDocId)
                .set(data)
                .addOnSuccessListener(aVoid -> updateOrganizers(oldQrHash, newDocId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to create new event document."));
    }

    private void updateOrganizers(String oldQrHash, String newQrHash) {
        db.collection("organizers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    List<String> eventHashes = (List<String>) document.get("eventHashes");
                    if (eventHashes != null && eventHashes.contains(oldQrHash)) {
                        eventHashes.remove(oldQrHash);
                        eventHashes.add(newQrHash);
                        document.getReference().update("eventHashes", eventHashes)
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to update organizer event hashes."));
                    }
                }
            }
        });
    }

    private void deleteEventPoster(String eventId, String eventPosterUrl) {
        db.collection("events").document(eventId).update("image_url", null)
                .addOnSuccessListener(aVoid -> {
                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(eventPosterUrl);
                    storageRef.delete().addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(this, "Event poster deleted.", Toast.LENGTH_SHORT).show();
                        event.setImage_url(null);
                        setupEventDetails();
                    });
                });
    }

    private void removeEventHashFromOrganizer(String eventHash) {
        db.collection("organizers").whereArrayContains("eventHashes", eventHash)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        doc.getReference().update("eventHashes", FieldValue.arrayRemove(eventHash))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove event hash from organizer."));
                    }
                });
    }

    private void refreshActivity() {
        Intent intent = new Intent(this, AdminViewEventsContent.class);
        intent.putExtra("event_data", event);
        finish();
        startActivity(intent);
    }

    private void navigateBackToEventList() {
        startActivity(new Intent(this, AdminViewEditEventsActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshEventDetails();
    }

    private void refreshEventDetails() {
        if (event != null && event.getQR_code() != null) {
            new DBManagerEvent().getEventByQRCode(event.getQR_code(), new DBManagerEvent.GetEventCallback() {
                @Override
                public void onSuccess(Event updatedEvent) {
                    event = updatedEvent;
                    initializeViews();
                    setupEventDetails();
                    setupButtonListeners();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("OrganizerEventDetails", "Error refreshing event details: " + e.getMessage());
                }
            });
        }
    }
}
