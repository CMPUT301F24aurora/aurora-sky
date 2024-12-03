package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code Sampling} class manages the entrant sampling process for an event.
 * It allows loading entrants from a waiting list, randomly sampling them based on event capacity,
 * and updating the Firebase Firestore database with selected entrants.
 */
public class Sampling extends AppCompatActivity {

    private RecyclerView entrantsRecyclerView;
    private List<Entrant> entrantsList;
    private List<Entrant> sampledEntrants;
    private EntrantWaitlistAdapter adapter;
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
        sampledEntrants = new ArrayList<>();
        sampleButton = findViewById(R.id.sample_button);

        event = (Event) getIntent().getSerializableExtra("event_data");
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        eventId = event.getQR_code();
        loadEntrants(eventId);

        adapter = new EntrantWaitlistAdapter(this, entrantsList);
        entrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantsRecyclerView.setAdapter(adapter);

        eventCapacity = event.getNumPeople();

        sampleButton.setOnClickListener(v -> onSampleButtonClick(eventCapacity));
    }

    private void loadEntrants(String eventId) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        List<String> waitingList = (List<String>) eventDoc.get("waitingList");

                        if (waitingList != null && !waitingList.isEmpty()) {
                            entrantsList.clear();
                            for (String entrantId : waitingList) {
                                db.collection("entrants").document(entrantId)
                                        .get()
                                        .addOnSuccessListener(entrantDoc -> {
                                            Entrant entrant = entrantDoc.toObject(Entrant.class);
                                            if (entrant != null) {
                                                entrantsList.add(entrant);
                                                adapter.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Sampling.this, "Failed to load entrant: " + entrantId, Toast.LENGTH_SHORT).show();
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

            if (entrantsList.size() > numPeople) {
                Collections.shuffle(entrantsList);
                selectedEntrants.addAll(entrantsList.subList(0, numPeople));
            } else {
                selectedEntrants.addAll(entrantsList);
            }

            List<String> selectedEntrantIds = new ArrayList<>();
            for (Entrant entrant : selectedEntrants) {
                selectedEntrantIds.add(entrant.getId());
                db.collection("entrants")
                        .document(entrant.getId())
                        .update("selected_event", FieldValue.arrayUnion(eventId))
                        .addOnSuccessListener(aVoid -> Log.d("Sampling", "Updated entrant: " + entrant.getId()))
                        .addOnFailureListener(e -> Log.e("Sampling", "Failed to update entrant: " + entrant.getId(), e));
            }

            db.collection("events")
                    .document(eventId)
                    .update(
                            "waitingList", FieldValue.arrayRemove(selectedEntrantIds.toArray()),
                            "selectedEntrants", selectedEntrantIds
                    )
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Sampling", "Updated event with selected entrants and removed from waiting list.");
                        Toast.makeText(Sampling.this, "Sampling results saved successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Sampling", "Failed to update event document.", e);
                        Toast.makeText(Sampling.this, "Failed to save sampling results.", Toast.LENGTH_SHORT).show();
                    });

            // Navigate to AfterSampling Activity
            Intent intent = new Intent(Sampling.this, AfterSampling.class);
            intent.putExtra("event_data", event);
            intent.putExtra("entrant_data", entrant);
            intent.putExtra("organizer_data", organizer);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No entrants available to sample.", Toast.LENGTH_SHORT).show();
        }
    }
}
