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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


public class RecyclerListActivity extends AppCompatActivity {
    private TextView titleTextView;
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Entrant> entrantsList;
    private EntrantWaitlistAdapter adapter;
    private Button notificationBtn, cancelButton;
    private List<Entrant> cancelledEntrants;
    private List<Entrant> selectedEntrants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }


        // Initialize views
        titleTextView = findViewById(R.id.recycler_title);
        recyclerView = findViewById(R.id.recycler_view);
        notificationBtn = findViewById(R.id.notifications_button);

        // Set layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firestore and list
        db = FirebaseFirestore.getInstance();
        entrantsList = new ArrayList<>();
        adapter = new EntrantWaitlistAdapter(this, entrantsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        Log.d("Recycler","done");

        // Get data passed through intent
        String title = getIntent().getStringExtra("title");
        String collection = getIntent().getStringExtra("collection");
        String eventId = getIntent().getStringExtra("eventId");
        selectedEntrants = (List<Entrant>) getIntent().getSerializableExtra("selectedEntrants");
        cancelledEntrants = (List<Entrant>) getIntent().getSerializableExtra("cancelledEntrants");
        Log.d("RecyclerListActivity", "Received Event ID: " + eventId);

        // Set title
        titleTextView.setText(title);

        // Fetch data from Firestore
        fetchDataFromFirebase(eventId, collection);

        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setVisibility(View.GONE); // Initially hidden

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                // Check if cancelButton and entrantsList are initialized
                if (cancelButton != null && entrantsList != null) {
                    boolean anySelected = false;
                    for (Entrant entrant : entrantsList) {
                        if (entrant.isSelected()) {
                            anySelected = true;
                            break;
                        }
                    }
                    // Set visibility based on whether any entrant is selected
                    cancelButton.setVisibility(anySelected ? View.VISIBLE : View.GONE);
                } else {
                    Log.e("RecyclerView", "cancelButton or entrantsList is not initialized.");
                }
            }
        });

        notificationBtn.setOnClickListener(v -> {
            if ("selectedEntrants".equals(collection)) {
                if (collection != null && !selectedEntrants.isEmpty()) {
                    for (Entrant entrant : selectedEntrants) {
                        String deviceId = entrant.getId();
                        String name = entrant.getName();
                        if (deviceId != null && !deviceId.isEmpty()) {
                            String messageTitle = "Congratulations, " + name + "!";
                            String message = "You have been selected in the lottery. Check your invitations for details.";
                            addNotificationToEntrant(deviceId, messageTitle, message);
                        } else {
                            Log.e("Notification", "Device ID is null or empty for entrant: " + name);
                        }
                    }
                    Toast.makeText(this, "Notifications sent to selected entrants!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No selected entrants to notify.", Toast.LENGTH_SHORT).show();
                }
            }

            if ("cancelledEntrants".equals(collection)) {
                if (collection != null && !cancelledEntrants.isEmpty()) {
                    for (Entrant entrant : cancelledEntrants) {
                        String deviceId = entrant.getId();
                        String name = entrant.getName();
                        if (deviceId != null && !deviceId.isEmpty()) {
                            String messageTitle = "Oops, " + name + "!";
                            String message = "You weren't selected in the lottery. You could still be selected if someone declines:)";
                            addNotificationToEntrant(deviceId, messageTitle, message);
                        } else {
                            Log.e("Notification", "Device ID is null or empty for entrant: " + name);
                        }
                    }
                    Toast.makeText(this, "Notifications sent to cancelled entrants!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No cancelled entrants to notify.", Toast.LENGTH_SHORT).show();
                }
            }

            if ("finalEntrants".equals(collection)) {
                db.collection("events")
                        .document(eventId) // Use the event ID to locate the event document
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && documentSnapshot.contains("finalChosenEntrants")) {
                                List<Map<String, Object>> finalChosenEntrants =
                                        (List<Map<String, Object>>) documentSnapshot.get("finalChosenEntrants");

                                if (finalChosenEntrants != null && !finalChosenEntrants.isEmpty()) {
                                    for (Map<String, Object> entrantData : finalChosenEntrants) {
                                        String deviceId = (String) entrantData.get("deviceId");
                                        String name = (String) entrantData.get("name");

                                        if (deviceId != null && !deviceId.isEmpty()) {
                                            String messageTitle = "Congratulations, " + name + "!";
                                            String message = "You have been chosen as a finalist for the event. See you there!";
                                            addNotificationToEntrant(deviceId, messageTitle, message);
                                        } else {
                                            Log.e("Notification", "Device ID is null or empty for entrant: " + name);
                                        }
                                    }
                                    Toast.makeText(this, "Notifications sent to final chosen entrants!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "No final chosen entrants to notify.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "finalChosenEntrants field not found in the database.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("DatabaseError", "Error fetching finalChosenEntrants: ", e);
                            Toast.makeText(this, "Failed to retrieve final chosen entrants.", Toast.LENGTH_SHORT).show();
                        });
            }

            if ("waitingList".equals(collection)) {
                // Show a dialog to get custom title and message for the notification
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

        });


        cancelButton.setOnClickListener(v -> {
            db.collection("events")
                    .document(eventId)  // Access the specific event document
                    .get()
                    .addOnSuccessListener(eventDoc -> {
                        if (eventDoc.exists()) {
                            // Get a list of Entrant IDs
                            List<String> chosenList = (List<String>) eventDoc.get(collection);
                            Log.d("entrantslist: ", "" + chosenList);
                            Log.d("collection: ", "" + collection);

                            // Prepare a list of entrants to cancel (fetch Entrants based on IDs)
                            List<String> toCancel = new ArrayList<>();

                            // Iterate over the chosenList to fetch entrant data
                            for (String entrantId : chosenList) {
                                db.collection("entrants") // Assuming 'entrants' collection stores entrant details
                                        .document(entrantId)
                                        .get()
                                        .addOnSuccessListener(entrantDoc -> {
                                            if (entrantDoc.exists()) {
                                                Entrant entrant = entrantDoc.toObject(Entrant.class);
                                                if (entrant != null && entrant.isSelected()) {
                                                    toCancel.add(entrant.getId());
                                                    Log.d("tocancel", "Entrant to cancel: " + entrant.getName());
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("Firestore", "Error fetching entrant data: " + e.getMessage());
                                        });
                            }
                            Log.d("cancelledentrants", "" + cancelledEntrants);
                            Log.d("entrants", "" + entrantsList);

//                            // Remove from RecyclerView and add to cancelledEntrants
//                            entrantsList.removeAll(toCancel);
//                            cancelledEntrants.addAll(toCancel);  // Ensure cancelledEntrants is not null
//                            adapter.notifyDataSetChanged();

                            // Update Firestore
//                            for (Entrant cancelledEntrant : toCancel) {
//                                db.collection("events")
//                                        .document(eventId) // Assuming eventId is available
//                                        .update(collection, FieldValue.arrayRemove(cancelledEntrant.getId())) // Remove from current list
//                                        .addOnSuccessListener(aVoid -> {
//                                            Log.d("Firestore", "Entrant removed: " + cancelledEntrant.getName());
//                                        });
//
//                                db.collection("events")
//                                        .document(eventId)
//                                        .update("cancelledEntrants", FieldValue.arrayUnion(cancelledEntrant.getId())) // Add to cancelled list
//                                        .addOnSuccessListener(aVoid -> {
//                                            Log.d("Firestore", "Entrant added to cancelled: " + cancelledEntrant.getName());
//                                        });
//                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("Firestore", "Error fetching event document: " + e.getMessage());
                    });
        });

    }

    private void fetchDataFromFirebase(String eventId, String collection) {
        db.collection("events")
                .document(eventId)  // Access the specific event document
                .get()
                .addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        List<String> waitingList = (List<String>) eventDoc.get(collection);

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
                                            Toast.makeText(RecyclerListActivity.this, "Failed to load entrant with ID: " + entrantId, Toast.LENGTH_SHORT).show();
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

    public void addNotificationToEntrant(String deviceId, String title, String message) {
        // Get the reference to the document based on deviceId (assuming deviceId is the entrant's unique ID)
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

}
