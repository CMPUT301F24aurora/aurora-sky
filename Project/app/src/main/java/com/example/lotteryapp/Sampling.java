package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sampling extends AppCompatActivity {

    private RecyclerView entrantsRecyclerView;
    private RecyclerView sampledRecyclerView;
    private List<Entrant> entrantsList;
    private List<Entrant> sampledEntrants;
    private EntrantWaitlistAdapter adapter;
    private EntrantWaitlistAdapter sampledAdapter;
    private Button sampleButton;
    private Event event;
    private String eventId;
    private Integer eventCapacity;
    private Entrant entrant;
    private Organizer organizer;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sampling);

        entrantsRecyclerView = findViewById(R.id.entrantsRecyclerView);
        entrantsList = new ArrayList<>();
        sampledRecyclerView = findViewById(R.id.after_sampling);
        sampledEntrants = new ArrayList<>();
        sampleButton = findViewById(R.id.sample_button);

        // Example: Populate entrantList with sample data
        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        eventId = event.getQR_code();
        loadEntrants(eventId);

        // Set up RecyclerView
        adapter = new EntrantWaitlistAdapter(this, entrantsList);
        entrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantsRecyclerView.setAdapter(adapter);

        // Set up RecyclerView for sampled entrants
        sampledAdapter = new EntrantWaitlistAdapter(this, sampledEntrants);
        sampledRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sampledRecyclerView.setAdapter(sampledAdapter);

        // Set up Sample button listener
        eventCapacity = event.getNumPeople();

        sampleButton.setOnClickListener(v -> onSampleButtonClick(eventCapacity));
    }

    private void loadEntrants(String eventId) {
        db.collection("events")
                .document(eventId)  // Access the specific event document
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        List<String> waitingList = (List<String>) eventDoc.get("waitingList");

                        if (waitingList != null && !waitingList.isEmpty()) {
                            entrantsList.clear(); // Clear the current entrants list

                            // Fetch entrants based on the waiting list IDs
                            for (String entrantId : waitingList) {
                                db.collection("entrants").document(entrantId)
                                        .get()
                                        .addOnSuccessListener(entrantDoc -> {
                                            Entrant entrant = entrantDoc.toObject(Entrant.class); // Convert the entrant document into an Entrant object
                                            if (entrant != null) {
                                                entrantsList.add(entrant); // Add entrant to the list
                                                adapter.notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
                                                Log.d("Sampling", "Entrants list size after loading: " + entrantsList.size());
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Sampling.this, "Failed to load entrant with ID: " + entrantId, Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            Toast.makeText(this, "No entrants in the waiting list.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event.", Toast.LENGTH_SHORT).show();
                });
    }

    private void onSampleButtonClick(int numPeople) {
        if (entrantsList.size() > 0) {
            List<Entrant> selectedEntrants = new ArrayList<>();
            List<Entrant> cancelledEntrants = new ArrayList<>();

            if (entrantsList.size() > numPeople) {
                // Perform random sampling
                Collections.shuffle(entrantsList);
                selectedEntrants.addAll(entrantsList.subList(0, numPeople));
                cancelledEntrants.addAll(entrantsList.subList(numPeople, entrantsList.size()));
            } else {
                // Show all entrants as selected
                selectedEntrants.addAll(entrantsList);
            }

            // Retrieve event ID and QR code from the Intent

            String qrCode = eventId; // Retrieve the event's QR code from the Intent
            if (qrCode == null || qrCode.isEmpty()) {
                Toast.makeText(this, "Event QR code is missing.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Firebase with the selected and canceled entrants
            List<String> selectedEntrantIds = new ArrayList<>();
            for (Entrant entrant : selectedEntrants) {
                selectedEntrantIds.add(entrant.getId());
                db.collection("entrants")
                        .document(entrant.getId()) // Assuming each Entrant has a unique ID
                        .update("selected_event", FieldValue.arrayUnion(qrCode))
                        .addOnSuccessListener(aVoid -> Log.d("Sampling", "Updated entrant: " + entrant.getId()))
                        .addOnFailureListener(e -> Log.e("Sampling", "Failed to update entrant: " + entrant.getId(), e));
            }

            List<String> cancelledEntrantIds = new ArrayList<>();
            for (Entrant entrant : cancelledEntrants) {
                cancelledEntrantIds.add(entrant.getId());
                db.collection("entrants")
                        .document(entrant.getId())
                        .update("cancelled_event", FieldValue.arrayUnion(qrCode))
                        .addOnSuccessListener(aVoid -> Log.d("Sampling", "Updated entrant: " + entrant.getId()))
                        .addOnFailureListener(e -> Log.e("Sampling", "Failed to update entrant: " + entrant.getId(), e));
            }

            db.collection("events")
                    .document(eventId)
                    .update(
                            "waitingList", FieldValue.arrayRemove(selectedEntrantIds.toArray()), // Remove selected entrants
                            "selectedEntrants", selectedEntrantIds,
                            "cancelledEntrants", cancelledEntrantIds
                    )
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Sampling", "Updated event with selected and cancelled entrants, and removed from waiting list.");
                        Toast.makeText(Sampling.this, "Sampling results saved successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Sampling", "Failed to update event document.", e);
                        Toast.makeText(Sampling.this, "Failed to save sampling results.", Toast.LENGTH_SHORT).show();
                    });

            // Pass data to SamplingResultsActivity
            Intent intent = new Intent(Sampling.this, AfterSampling.class);
            intent.putExtra("event_data", event);
            intent.putExtra("entrant_data", entrant);
            intent.putExtra("organizer_data", organizer);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No entrants available to sample.", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "Sampling completed.", Toast.LENGTH_SHORT).show();
    }

}
