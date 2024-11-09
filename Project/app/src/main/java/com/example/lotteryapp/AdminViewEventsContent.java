package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminViewEventsContent extends AppCompatActivity {

    private TextView eventNameTextView;
    private Button adminEvRemove;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view_events_content);

        eventNameTextView = findViewById(R.id.admin_event_name);

        // Get the intent extras and set the text views
        String eventName = getIntent().getStringExtra("eventName");
        //String eventDate = getIntent().getStringExtra("eventDate");
        //String eventDescription = getIntent().getStringExtra("eventDescription");

        eventNameTextView.setText(eventName);

        adminEvRemove =findViewById(R.id.admin_ev_remove);

        adminEvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvent();
                }
        });
        db = FirebaseFirestore.getInstance();
    }

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
