package com.example.lotteryapp;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecyclerListActivity extends AppCompatActivity {
    private TextView titleTextView, noEntrantsText;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Entrant> entrantsList;
    private EntrantWaitlistAdapter adapter;
    private Button notificationBtn, cancelButton;
    private List<String> entrantIds;
    private Event event;
    private String eventId;
    private String collection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_list);

        initializeViews();
        retrieveIntentData();
        checkNotificationPermission();
        setupRecyclerView();

        //fetchDataFromFirebase(eventId, collection);

        setupNotificationButton();
        setupCancelButton();

        determineEntrantIds();
        manageEntrantListVisibility();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void initializeViews() {
        titleTextView = findViewById(R.id.recycler_title);
        noEntrantsText = findViewById(R.id.no_entrants_text);
        recyclerView = findViewById(R.id.recycler_view);
        notificationBtn = findViewById(R.id.notifications_button);
        cancelButton = findViewById(R.id.cancel_button);
        db = FirebaseFirestore.getInstance();
        entrantsList = new ArrayList<>();
        adapter = new EntrantWaitlistAdapter(this, entrantsList);
        recyclerView.setAdapter(adapter);
    }

    private void retrieveIntentData() {
        titleTextView.setText(getIntent().getStringExtra("title"));
        collection = getIntent().getStringExtra("collection");
        eventId = getIntent().getStringExtra("eventId");
        event = (Event) getIntent().getSerializableExtra("event_data");
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateCancelButtonVisibility();
            }
        });
    }

    private void updateCancelButtonVisibility() {
        boolean anySelected = entrantsList.stream().anyMatch(Entrant::isSelected);
        cancelButton.setVisibility(anySelected ? View.VISIBLE : View.GONE);
    }

    private void setupNotificationButton() {
        notificationBtn.setOnClickListener(v -> handleNotificationClick());
    }

    private void determineEntrantIds() {
        if (Objects.equals(collection, "selectedEntrants")) {
            entrantIds = event.getSelectedEntrants();
        } else if (Objects.equals(collection, "cancelledEntrants")) {
            entrantIds = event.getCancelledEntrants();
        } else if (Objects.equals(collection, "waitingList")) {
            entrantIds = event.getWaitingList();
        }
    }

    private void manageEntrantListVisibility() {
        if (entrantIds == null || entrantIds.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noEntrantsText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            getData(entrantIds);
        }
    }

    private void handleNotificationClick() {
        switch (collection) {
            case "selectedEntrants":
                sendNotificationsToEntrants(entrantIds, "Congratulations", "You have been selected in the lottery. Check your invitations for details.");
                break;
            case "cancelledEntrants":
                sendNotificationsToEntrants(entrantIds, "Oops", "You weren't selected in the lottery. You could still be selected if someone declines:)");
                break;
            case "finalEntrants":
                fetchFinalEntrantsAndNotify();
                break;
            case "waitingList":
                showCustomNotificationDialog();
                break;
        }
    }

    private void sendNotificationsToEntrants(List<String> entrantIds, String titlePrefix, String message) {
        if (entrantIds == null || entrantIds.isEmpty()) {
            Toast.makeText(this, "No entrants to notify.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String entrantId : entrantIds) {
            db.collection("entrants").document(entrantId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    String deviceId = doc.getString("id");
                    if (deviceId != null && !deviceId.isEmpty()) {
                        addNotificationToEntrant(deviceId, titlePrefix + ", " + name + "!", message);
                    } else {
                        Log.e("Notification", "Device ID is null or empty for entrant: " + name);
                    }
                }
            });
        }
        Toast.makeText(this, "Notifications sent!", Toast.LENGTH_SHORT).show();
    }

    private void fetchFinalEntrantsAndNotify() {
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && documentSnapshot.contains("finalChosenEntrants")) {
                List<Map<String, Object>> finalChosenEntrants = (List<Map<String, Object>>) documentSnapshot.get("finalChosenEntrants");
                sendNotificationsToEntrants(finalChosenEntrants.stream().map(e -> (String) e.get("deviceId")).toList(), "Congratulations", "You have been chosen as a finalist!");
            } else {
                Toast.makeText(this, "No final chosen entrants to notify.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomNotificationDialog() {
        // Implementation remains similar to the existing code
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> {
            // Logic to handle cancel functionality
        });
    }

    private void addNotificationToEntrant(String deviceId, String title, String message) {
        // Add notification logic here
    }

    private void fetchDataFromFirebase(String eventId, String collection) {
        // Fetch data logic here
    }

    private void getData(List<String> dataList){
        entrantsList.clear();
        DatabaseManager.fetchEntrantsByIds(dataList, new DatabaseManager.EntrantsFetchCallback() {
            @Override
            public void onSuccess(List<Entrant> entrants) {
                entrantsList.addAll(entrants);
                adapter.updateList(entrantsList);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("",errorMessage);
            }
        });
    }


}
