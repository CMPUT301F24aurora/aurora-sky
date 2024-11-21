package com.example.lotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sampling extends AppCompatActivity {

    private RecyclerView entrantsRecyclerView;
    //private RecyclerView sampledRecyclerView;
    private List<Entrant> entrantsList;
    private List<Entrant> sampledEntrants;
    private EntrantWaitlistAdapter adapter;
    private EntrantWaitlistAdapter sampledAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sampling);


        entrantsRecyclerView = findViewById(R.id.entrantsRecyclerView);
        entrantsList = new ArrayList<>(); // Initialize with data

        // Example: Populate entrantList with sample data
        String eventId = getIntent().getStringExtra("eventId");
        loadEntrants(eventId);

        // Set up RecyclerView
        adapter = new EntrantWaitlistAdapter(this, entrantsList);
        entrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantsRecyclerView.setAdapter(adapter);
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private void loadEntrants(String eventId) {
        db.collection("events")
                .document(eventId)  // Access the specific event document
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        // Retrieve the event details
                        String eventName = eventDoc.getString("eventName");
                        String eventDate = eventDoc.getString("eventDate");
                        int numPeople = eventDoc.getLong("numPeople").intValue();
                        List<String> waitingList = (List<String>) eventDoc.get("waitingList");

                        // Log event details
                        Log.d("EventDetails", "Event Name: " + eventName + ", Event Date: " + eventDate + ", Number of People: " + numPeople);

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


//    private void loadEntrants() {
//        db.collection("events")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
//                        Entrant entrant = doc.toObject(Entrant.class);
//                        entrantsList.add(entrant);
//                    }
//                    adapter.notifyDataSetChanged(); // Refresh RecyclerView
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Failed to load entrants.", Toast.LENGTH_SHORT).show();
//                });
//    }

        //sampledRecyclerView = findViewById(R.id.after_sampling);
        //sampledEntrants = new ArrayList<>();

        // Add sample entrants to the list
//        entrantsList.add(new Entrant("Entrant donn"));
//        entrantsList.add(new Entrant("Entrant 2"));
//        entrantsList.add(new Entrant("Entrant 3"));
//        entrantsList.add(new Entrant("Entrant 4"));
//        entrantsList.add(new Entrant("Entrant 5"));
//        entrantsList.add(new Entrant("Entrant 6"));
//        entrantsList.add(new Entrant("Entrant 7"));
//        entrantsList.add(new Entrant("Entrant 8"));
//        entrantsList.add(new Entrant("Entrant 9"));
//        entrantsList.add(new Entrant("Entrant 10"));

//        // Set up RecyclerView for all entrants
//        entrantAdapter = new EntrantAdapter(entrantsList, entrant -> {
//            Toast.makeText(Sampling.this, "Clicked: " + entrant.getName(), Toast.LENGTH_SHORT).show();
//        });
//        entrantsRecyclerView.setAdapter(entrantAdapter);

        // Set up RecyclerView for sampled entrants
        //sampledRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        sampledAdapter = new EntrantWaitlistAdapter(sampledEntrants, entrant -> {
//            Toast.makeText(Sampling.this, "Sampled Click: " + entrant.getName(), Toast.LENGTH_SHORT).show();
//        });
        //sampledRecyclerView.setAdapter(sampledAdapter);





    // Called when the "Sample" button is pressed
//    public void onSampleButtonClick(View view) {
//        sampledEntrants.clear(); // Clear previous samples
//        List<Entrant> newSamples = sampleEntrants(entrantsList, 1);
//
//        if (newSamples != null && !newSamples.isEmpty()) {
//            sampledEntrants.addAll(newSamples);
//            Log.d("Sampling", "Sampled Entrants: " + sampledEntrants.toString());
//            sampledAdapter.notifyDataSetChanged(); // Refresh sampled RecyclerView
//        } else {
//            Log.d("Sampling", "Not enough entrants to sample");
//            Toast.makeText(this, "Not enough entrants to sample", Toast.LENGTH_SHORT).show();
//        }
//    }

    // Sample a specified number of entrants from the list
//    private List<Entrant> sampleEntrants(List<Entrant> entrants, int sampleSize) {
//        if (entrants.size() < sampleSize) {
//            return null; // Not enough entrants to sample
//        }
//        Collections.shuffle(entrants); // Randomize the list
//        return new ArrayList<>(entrants.subList(0, sampleSize)); // Get a sublist of sampled entrants
//    }
}
