package com.example.lotteryapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.util.HashMap;
import java.util.Map;

public class DBManagerEvent {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Method to add an Event to the database and return the Event
    public Event addEventToDatabase(Event event) {
        String eventId = event.getQR_code();  // Use QR code as event ID
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", event.getName());
        eventData.put("eventDate", event.getEventDate());
        eventData.put("numPeople", event.getNumPeople());
        eventData.put("description", event.getDescription());
        eventData.put("qr_code", eventId);  // Store QR code as the ID field
        eventData.put("waitingList", event.getWaitingList().getWaitingListIds());  // Store waiting list IDs

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
}
