package com.example.lotteryapp;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages database operations related to events using Firebase Firestore.
 */
public class DBManagerEvent {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Adds an Event to the database and returns the Event.
     *
     * @param event The Event object to be added to the database.
     * @return The added Event object if successful, null otherwise.
     */
    public Event addEventToDatabase(Event event) {
        Log.d("DBManagerEvent", "Uploading event " + event.getEventName());
        String eventId = event.getQR_code();
        Map<String, Object> eventData = new HashMap<>();
        // Populate eventData map with event details

        DocumentReference eventRef = db.collection("events").document(eventId);
        Task<Void> task = eventRef.set(eventData);
        try {
            Tasks.await(task);
            if (task.isSuccessful()) {
                System.out.println("Event added successfully.");
                return event;
            } else {
                System.out.println("Error adding event: " + task.getException());
                return null;
            }
        } catch (Exception e) {
            System.out.println("Exception while adding event: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves all events from Firestore.
     *
     * @param callback The callback to handle the result.
     */
    public void getEventsFromFirestore(GetEventsCallback callback) {
        if (callback == null) return;
        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * Retrieves events by their QR codes.
     *
     * @param qrCodes The list of QR codes to fetch events for.
     * @param callback The callback to handle the result.
     */
    public void getEventsByQRCodes(List<String> qrCodes, GetEventsCallback callback) {
        if (callback == null || qrCodes == null || qrCodes.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("Invalid input parameters"));
            return;
        }
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String qrCode : qrCodes) {
            Task<DocumentSnapshot> task = db.collection("events").document(qrCode).get();
            tasks.add(task);
        }
        Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(taskSnapshots -> {
                    List<Event> events = new ArrayList<>();
                    for (Task<DocumentSnapshot> task : tasks) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Event event = document.toObject(Event.class);
                                if (event != null) {
                                    events.add(event);
                                }
                            }
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * Retrieves an event by its QR code.
     *
     * @param qrCode The QR code of the event to retrieve.
     * @param callback The callback to handle the result.
     */
    public void getEventByQRCode(String qrCode, GetEventCallback callback) {
        if (qrCode == null || qrCode.isEmpty() || callback == null) {
            callback.onFailure(new IllegalArgumentException("Invalid QR code or callback"));
            return;
        }
        db.collection("events").document(qrCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            callback.onSuccess(event);
                        } else {
                            callback.onFailure(new Exception("Failed to parse event"));
                        }
                    } else {
                        callback.onFailure(new Exception("Event not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Removes entrants from a specified list in the event document.
     *
     * @param entrantList The list of entrants to remove.
     * @param listName The name of the list to remove entrants from.
     * @param callback The callback to handle the result.
     */
    public static void removeEntrantsFromList(List<Entrant> entrantList, String listName, EntrantsUpdateCallback callback) {
        List<String> entrantIds = entrantList.stream().map(Entrant::getId).collect(Collectors.toList());
        db.collection("events")
                .whereArrayContainsAny(listName, entrantIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        document.getReference().update(listName, FieldValue.arrayRemove(entrantIds.toArray()))
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Entrants removed from " + listName + " successfully");
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error removing entrants: ", e);
                                    callback.onFailure(e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error querying events: ", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Adds entrants to a specified list in the event document.
     *
     * @param entrantList The list of entrants to add.
     * @param listName The name of the list to add entrants to.
     * @param eventId The ID of the event.
     * @param callback The callback to handle the result.
     */
    public static void addEntrantsToList(List<Entrant> entrantList, String listName, String eventId, EntrantsUpdateCallback callback) {
        List<String> entrantIds = entrantList.stream().map(Entrant::getId).collect(Collectors.toList());
        db.collection("events")
                .document(eventId)
                .update(listName, FieldValue.arrayUnion(entrantIds.toArray()))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Entrants added to " + listName + " successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding entrants: ", e);
                    callback.onFailure(e.getMessage());
                });
    }

    /**
     * Callback interface for entrants update operations.
     */
    public interface EntrantsUpdateCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    /**
     * Callback interface for retrieving a single event.
     */
    public interface GetEventCallback {
        void onSuccess(Event event);
        void onFailure(Exception e);
    }
}