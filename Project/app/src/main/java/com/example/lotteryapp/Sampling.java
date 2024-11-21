package com.example.lotteryapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sampling extends AppCompatActivity {

    private RecyclerView entrantsRecyclerView;
    private RecyclerView sampledRecyclerView;
    private List<Entrant> entrantsList;
    private List<Entrant> sampledEntrants;
    private EntrantAdapter entrantAdapter;
    private EntrantAdapter sampledAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sampling);

        // Initialize RecyclerViews
        entrantsRecyclerView = findViewById(R.id.entrantsRecyclerView);
        sampledRecyclerView = findViewById(R.id.after_sampling);

        entrantsList = new ArrayList<>();
        sampledEntrants = new ArrayList<>();

        // Add sample entrants to the list
        entrantsList.add(new Entrant("Entrant 1"));
        entrantsList.add(new Entrant("Entrant 2"));
        entrantsList.add(new Entrant("Entrant 3"));
        entrantsList.add(new Entrant("Entrant 4"));
        entrantsList.add(new Entrant("Entrant 5"));
        entrantsList.add(new Entrant("Entrant 6"));
        entrantsList.add(new Entrant("Entrant 7"));
        entrantsList.add(new Entrant("Entrant 8"));
        entrantsList.add(new Entrant("Entrant 9"));
        entrantsList.add(new Entrant("Entrant 10"));

        // Set up RecyclerView for all entrants
        entrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantAdapter = new EntrantAdapter(entrantsList, entrant -> {
            Toast.makeText(Sampling.this, "Clicked: " + entrant.getName(), Toast.LENGTH_SHORT).show();
        });
        entrantsRecyclerView.setAdapter(entrantAdapter);

        // Set up RecyclerView for sampled entrants
        sampledRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sampledAdapter = new EntrantAdapter(sampledEntrants, entrant -> {
            Toast.makeText(Sampling.this, "Sampled Click: " + entrant.getName(), Toast.LENGTH_SHORT).show();
        });
        sampledRecyclerView.setAdapter(sampledAdapter);
    }

    // Called when the "Sample" button is pressed
    public void onSampleButtonClick(View view) {
        sampledEntrants.clear(); // Clear previous samples
        List<Entrant> newSamples = sampleEntrants(entrantsList, 1);

        if (newSamples != null && !newSamples.isEmpty()) {
            sampledEntrants.addAll(newSamples);
            sampledAdapter.notifyDataSetChanged(); // Refresh sampled entrants RecyclerView
        } else {
            Toast.makeText(this, "Not enough entrants to sample", Toast.LENGTH_SHORT).show();
        }
    }

    // Sample a specified number of entrants from the list
    private List<Entrant> sampleEntrants(List<Entrant> entrants, int sampleSize) {
        if (entrants.size() < sampleSize) {
            return null; // Not enough entrants to sample
        }
        Collections.shuffle(entrants); // Randomize the list
        return new ArrayList<>(entrants.subList(0, sampleSize)); // Get a sublist of sampled entrants
    }
}
