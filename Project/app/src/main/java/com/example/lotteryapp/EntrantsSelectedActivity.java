package com.example.lotteryapp;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotteryapp.Entrant;
import com.example.lotteryapp.EntrantAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntrantsSelectedActivity extends AppCompatActivity implements EntrantAdapter.EntrantClickListener {

    private TextView numberOfEntrantsTextView, selectedEntrantsTextView;
    private RecyclerView entrantRecyclerView;
    private Button randomSelectButton;

    private List<Entrant> waitlist = new ArrayList<>();
    private EntrantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_waitinglist);

        // Initialize views
        numberOfEntrantsTextView = findViewById(R.id.number_of_entrants);
        entrantRecyclerView = findViewById(R.id.entrantRecyclerView);
        randomSelectButton = findViewById(R.id.random_select_button);
        selectedEntrantsTextView = findViewById(R.id.selected_entrants);

        // Add sample entrants
        waitlist.add(new Entrant("id_1", "Alice", "alice@example.com"));
        waitlist.add(new Entrant("id_2", "Bob", "bob@example.com"));
        waitlist.add(new Entrant("id_3", "Charlie", "charlie@example.com"));
        waitlist.add(new Entrant("id_4", "Diana", "diana@example.com"));
        waitlist.add(new Entrant("id_5", "Edward", "edward@example.com"));

        // Set up RecyclerView
        adapter = new EntrantAdapter(waitlist, this); // Passing the current activity as EntrantClickListener
        entrantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantRecyclerView.setAdapter(adapter);

        // Update number of entrants
        updateEntrantsCount();

        // Handle button click
        randomSelectButton.setOnClickListener(v -> selectRandomEntrants());
    }

    @Override
    public void onEntrantClick(Entrant entrant) {
        // Handle entrant item click
        Log.d("EntrantSelected", "Entrant clicked: " + entrant.getName());
    }

    private void updateEntrantsCount() {
        numberOfEntrantsTextView.setText("Number of Entrants: " + waitlist.size());
    }

    private void selectRandomEntrants() {
        if (waitlist.isEmpty()) {
            selectedEntrantsTextView.setText("None");
            return;
        }

        // Randomly shuffle and select up to 3 entrants
        List<Entrant> randomSelection = new ArrayList<>(waitlist);
        Collections.shuffle(randomSelection);
        int count = Math.min(randomSelection.size(), 3);
        randomSelection = randomSelection.subList(0, count);

        // Display selected entrants
        StringBuilder selectedNames = new StringBuilder();
        for (Entrant entrant : randomSelection) {
            selectedNames.append(entrant.getName()).append("\n");
        }
        selectedEntrantsTextView.setText(selectedNames.toString().trim());
    }
}
