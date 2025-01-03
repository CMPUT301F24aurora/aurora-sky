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

public class DBManagerEvent {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Method to add an Event to the database and return the Event
    public Event addEventToDatabase(Event event) {
        Log.d("", "Uploading event " + event.getEventName());
        String eventId = event.getQR_code();  // Use QR code as event ID
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", event.getEventName());
        eventData.put("eventStartDate", event.getEventStartDate());
        eventData.put("eventEndDate", event.getEventEndDate());
        eventData.put("registrationDeadline", event.getRegistrationDeadline());
        eventData.put("eventPrice", event.getEventPrice());
        eventData.put("numPeople", event.getNumPeople());
        eventData.put("description", event.getDescription());
        eventData.put("qr_code", eventId);  // Store QR code as the ID field
        eventData.put("waitingList", event.getWaitingList());  // Store waiting list IDs
        eventData.put("selectedEntrants", event.getSelectedEntrants());
        eventData.put("cancelledEntrants", event.getCancelledEntrants());
        eventData.put("finalEntrants", event.getFinalEntrants());
        eventData.put("image_url", event.getImage_url());
        eventData.put("geolocationRequired", event.getGeolocationRequired());
        eventData.put("waitlistCap", event.getWaitlistCap());


        // Add event to "events" collection in Firestore
        DocumentReference eventRef = db.collection("events").document(eventId);
        Task<Void> task = eventRef.set(eventData);

        try {
            Tasks.await(task);  // Wait for the task to complete (use in background if needed)
            if (task.isSuccessful()) {
                System.out.println("Event added successfully.");
                return event;  // Return the event if successful
            } else {
                System.out.println("Error adding event: " + task.getException());
                return null;  // Return null if there was an error
            }
        } catch (Exception e) {
            System.out.println("Exception while adding event: " + e.getMessage());
            return null;
        }
    }

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
                                    callback.onSuccess();  // Notify success
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error removing entrants: ", e);
                                    callback.onFailure(e.getMessage());  // Notify failure
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error querying events: ", e);
                    callback.onFailure(e.getMessage());  // Notify failure for query error
                });
    }


    public static void addEntrantsToList(List<Entrant> entrantList, String listName, String eventId, EntrantsUpdateCallback callback) {
        List<String> entrantIds = entrantList.stream().map(Entrant::getId).collect(Collectors.toList());
        db.collection("events")
                .document(eventId)  // Target a specific event by ID
                .update(listName, FieldValue.arrayUnion(entrantIds.toArray()))
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Entrants added to " + listName + " successfully");
                    callback.onSuccess();  // Notify success
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding entrants: ", e);
                    callback.onFailure(e.getMessage());  // Notify failure with the error message
                });
    }

    public interface EntrantsUpdateCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface GetEventCallback {
        void onSuccess(Event event);
        void onFailure(Exception e);
    }

}
