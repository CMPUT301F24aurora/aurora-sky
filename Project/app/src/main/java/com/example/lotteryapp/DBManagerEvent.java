package com.example.lotteryapp;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManagerEvent {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Method to add an Event to the database and return the Event
    public Event addEventToDatabase(Event event) {
        String eventId = event.getQR_code();  // Use QR code as event ID
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", event.getEventName());
        eventData.put("eventDate", event.getEventDate());
        eventData.put("numPeople", event.getNumPeople());
        eventData.put("description", event.getDescription());
        eventData.put("qr_code", eventId);  // Store QR code as the ID field
        eventData.put("waitingList", event.getWaitingList());  // Store waiting list IDs

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

    public interface GetEventCallback {
        void onSuccess(Event event);
        void onFailure(Exception e);
    }


}
