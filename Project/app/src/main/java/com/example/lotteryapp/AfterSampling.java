package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AfterSampling extends AppCompatActivity {
    private String eventId;
    private List<Entrant> cancelledEntrants;
    private List<Entrant> selectedEntrants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_sampling_list);

        // Get event ID from Intent
        eventId = getIntent().getStringExtra("eventId");
        selectedEntrants = (List<Entrant>) getIntent().getSerializableExtra("selectedEntrants");
        cancelledEntrants = (List<Entrant>) getIntent().getSerializableExtra("cancelledEntrants");
        // Initialize buttons
        Button waitlistButton = findViewById(R.id.buttonWaitlistEntrants);
        Button selectedButton = findViewById(R.id.buttonSelectedEntrants);
        Button cancelledButton = findViewById(R.id.buttonCancelledEntrants);
        Button chosenButton = findViewById(R.id.buttonChosenEntrants);

        // Set onClickListeners for navigation
        waitlistButton.setOnClickListener(v -> navigateToRecyclerList("Entrants in the waitlist", "waitingList"));
        selectedButton.setOnClickListener(v -> navigateToRecyclerList("Selected entrants", "selectedEntrants"));
        cancelledButton.setOnClickListener(v -> navigateToRecyclerList("Cancelled Entrants", "cancelledEntrants"));
        chosenButton.setOnClickListener(v -> navigateToRecyclerList("Final chosen entrants", "finalEntrants"));
    }

    private void navigateToRecyclerList(String title, String collection) {
        Intent intent = new Intent(AfterSampling.this, RecyclerListActivity.class);
        Log.d("AfterSampling","done");
        intent.putExtra("title", title);
        intent.putExtra("collection", collection);
        intent.putExtra("eventId", eventId);
        intent.putExtra("selectedEntrants", (Serializable) selectedEntrants);
        intent.putExtra("cancelledEntrants", (Serializable) cancelledEntrants);
        startActivity(intent);
    }
}
