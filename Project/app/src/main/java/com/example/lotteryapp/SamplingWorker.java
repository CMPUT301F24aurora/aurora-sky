package com.example.lotteryapp;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SamplingWorker extends Worker {
    private List<Entrant> entrantsList;
    private FirebaseFirestore db;

    public SamplingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    @NonNull
    @Override
    public Result doWork() {
        // This method will run in the background to sample entrants
        String eventId = getInputData().getString("eventId");
        if (eventId != null) {
            Log.d("SamplingWorker", "Sampling event: " + eventId);
            sampleEntrants(eventId);
            return Result.success();
        } else {
            Log.e("SamplingWorker", "Event ID is null.");
            return Result.failure();
        }
    }


    private void sampleEntrants(String eventId) {
        db.collection("events").document(eventId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            long currentTime = System.currentTimeMillis();
                            Object deadlineObject = task.getResult().get("registrationDeadline");

                            long deadlineTime = 0;
                            if (deadlineObject instanceof String) {
                                String deadlineString = (String) deadlineObject;
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                try {
                                    Date deadlineDate = sdf.parse(deadlineString);
                                    if (deadlineDate != null) {
                                        deadlineTime = deadlineDate.getTime();
                                    }
                                } catch (ParseException e) {
                                    Log.e("SamplingWorker", "Error parsing registrationDeadline", e);
                                    return;
                                }
                            } else if (deadlineObject instanceof Number) {
                                deadlineTime = ((Number) deadlineObject).longValue();
                            }

                            int numPeople = task.getResult().getLong("numPeople").intValue();
                            List<String> waitingList = (List<String>) task.getResult().get("waitingList");

                            if (waitingList != null && !waitingList.isEmpty()) {
                                if (currentTime > deadlineTime) {
                                    performSampling(eventId, numPeople, waitingList);
                                } else {
                                    Log.d("SamplingWorker", "Registration deadline not reached.");
                                }
                            } else {
                                Log.d("SamplingWorker", "Waiting list is empty or null.");
                            }
                        }
                    } else {
                        Log.e("SamplingWorker", "Error fetching event data.", task.getException());
                    }
                });
    }

    private void performSampling(String eventId, int numPeople, List<String> waitingList) {
        if (waitingList.size() > numPeople) {
            Collections.shuffle(waitingList);

            List<String> selectedEntrants = waitingList.subList(0, numPeople);
            List<String> cancelledEntrants = waitingList.subList(numPeople, waitingList.size());

            db.collection("events").document(eventId)
                    .update("selectedEntrants", selectedEntrants, "cancelledEntrants", cancelledEntrants)
                    .addOnSuccessListener(aVoid -> Log.d("SamplingWorker", "Sampling complete"))
                    .addOnFailureListener(e -> Log.e("SamplingWorker", "Error updating event", e));
            // Update the "entrants" collection with the selected and canceled entrants
            updateEntrantDocuments(selectedEntrants, eventId, "selected_event");
            updateEntrantDocuments(cancelledEntrants, eventId, "cancelled_event");

        } else {
            List<String> selectedEntrants = new ArrayList<>(waitingList);
            db.collection("events").document(eventId)
                    .update("selectedEntrants", selectedEntrants)
                    .addOnSuccessListener(aVoid -> Log.d("SamplingWorker", "Sampling complete"))
                    .addOnFailureListener(e -> Log.e("SamplingWorker", "Error updating event", e));

            // Update the "entrants" collection with the selected and canceled entrants
            updateEntrantDocuments(selectedEntrants, eventId, "selected_event");
        }
    }

    private void updateEntrantDocuments(List<String> entrantIds, String eventId, String eventStatus) {
        for (String entrantId : entrantIds) {
            db.collection("entrants")
                    .document(entrantId)
                    .update(eventStatus, eventId)
                    .addOnSuccessListener(aVoid -> Log.d("Sampling", "Updated entrant: " + entrantId))
                    .addOnFailureListener(e -> Log.e("Sampling", "Failed to update entrant: " + entrantId, e));
        }
    }


}
