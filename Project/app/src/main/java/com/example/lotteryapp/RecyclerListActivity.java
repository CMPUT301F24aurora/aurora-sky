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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
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
            }
        });
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
        } else if(Objects.equals(collection, "finalEntrants")){
            entrantIds = event.getFinalEntrants();
        }
    }

    private void manageEntrantListVisibility() {
        if (!entrantIds.isEmpty()) {
            getData(entrantIds);
        } else {
            updateUiState(); // Initially set the state
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
        String title = "Congratulations";
        String message = "You have been chosen as a finalist!";
        sendNotificationsToEntrants(entrantIds, title, message);
    }

    private void showCustomNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Send Custom Notification");

        // Inflate a custom layout for the dialog (with input fields for title and message)
        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_notifications, null);
        builder.setView(customView);

        EditText titleInput = customView.findViewById(R.id.notificationTitle);
        EditText messageInput = customView.findViewById(R.id.notificationMessage);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String customTitle = titleInput.getText().toString().trim();
            String customMessage = messageInput.getText().toString().trim();

            if (customTitle.isEmpty() || customMessage.isEmpty()) {
                Toast.makeText(this, "Title or message cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("id: ",""+eventId);

            // Fetch entrants in the waitlist from the database
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events")
                    .document(eventId) // Use event ID to find the event document
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Log.d("id2: ",""+eventId);
                        if (documentSnapshot.exists() && documentSnapshot.contains("waitingList")) {
                            List<String> waitlistEntrants =
                                    (List<String>) documentSnapshot.get("waitingList");
                            Log.d("waitlist", "Entrants in waitlist: " + waitlistEntrants);

                            if (waitlistEntrants != null && !waitlistEntrants.isEmpty()) {
                                for (String entrantId : waitlistEntrants) {
                                    // Fetch the entrant document from Firestore
                                    Log.d("entrants in waitist", " "+ entrantId);
                                    db.collection("entrants")
                                            .document(entrantId)
                                            .get()
                                            .addOnSuccessListener(entrantDoc -> {
                                                if (entrantDoc.exists()) {
                                                    String deviceId = entrantDoc.getString("id");
                                                    String name = entrantDoc.getString("name");

                                                    if (deviceId != null && !deviceId.isEmpty()) {
                                                        Log.e("Notification", "Sending notification to: " + name);
                                                        addNotificationToEntrant(deviceId, customTitle, customMessage);
                                                    } else {
                                                        Log.e("Notification", "Device ID is null or empty for entrant: " + name);
                                                    }
                                                } else {
                                                    Log.e("Firestore", "Entrant document not found for ID: " + entrantId);
                                                }
                                            })
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error fetching entrant document: " + e.getMessage()));
                                }
                                Toast.makeText(this, "Custom notifications sent to waitlist entrants!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "No entrants found in the waitlist.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Waitlist field not found in the database.", Toast.LENGTH_SHORT).show();
                        }

                    })
                    .addOnFailureListener(e -> {
                        Log.e("DatabaseError", "Error fetching waitlist: ", e);
                        Toast.makeText(this, "Failed to retrieve waitlist entrants.", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateUiState() {
            recyclerView.setVisibility(View.GONE);
            noEntrantsText.setVisibility(View.VISIBLE);
            cancelButton.setEnabled(false);
    }

    private void setupCancelButton() {

        cancelButton.setOnClickListener(v -> {
            List<Entrant> selectedEntrants = adapter.getSelectedEntrants();
            DBManagerEvent.removeEntrantsFromList(selectedEntrants, collection, new DBManagerEvent.EntrantsUpdateCallback() {
                @Override
                public void onSuccess() {
                    if (!selectedEntrants.isEmpty()) {
                        // Remove selected entrants from the adapter
                        Toast.makeText(getApplicationContext(), "Entrant Removed!", Toast.LENGTH_SHORT).show();
                        DBManagerEvent.addEntrantsToList(selectedEntrants, "cancelledEntrants", eventId, new DBManagerEvent.EntrantsUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("","works");
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Log.d("", "does not work");
                            }
                        });
                        adapter.removeEntrants(selectedEntrants);
                        if (adapter.getItemCount() == 0){
                            updateUiState();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No entrants selected", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {

                }
            });

        });
    }

    private void addNotificationToEntrant(String deviceId, String title, String message) {
        DocumentReference entrantDocRef = db.collection("entrants").document(deviceId);

        // Create the notification object
        Map<String, String> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", String.valueOf(System.currentTimeMillis()));

        // Add notification to the "notifications" list field in the Firestore document
        entrantDocRef.update("notifications", FieldValue.arrayUnion(notification))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Notification added to entrant: " + deviceId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding notification: " + e.getMessage());
                });
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
