package com.example.lotteryapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SamplingResultsActivity extends AppCompatActivity {

    private RecyclerView selectedEntrantsRecyclerView;
    private RecyclerView cancelledEntrantsRecyclerView;
    private EntrantWaitlistAdapter selectedAdapter;
    private EntrantWaitlistAdapter cancelledAdapter;
    private Button sendNotificationsButton;
    // Initialize Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_sampling);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        selectedEntrantsRecyclerView = findViewById(R.id.selected_entrants_recyclerView);
        cancelledEntrantsRecyclerView = findViewById(R.id.cancelled_entrants_recyclerView);
        sendNotificationsButton = findViewById(R.id.send_notifications);

        // Get the selected and cancelled entrants lists from the intent
        List<Entrant> selectedEntrants = (List<Entrant>) getIntent().getSerializableExtra("selectedEntrants");
        List<Entrant> cancelledEntrants = (List<Entrant>) getIntent().getSerializableExtra("cancelledEntrants");

        if (selectedEntrants != null && cancelledEntrants != null) {
            //Log.d("SamplingResultsActivity", "Selected Entrants: " + selectedEntrants.size());
            //Log.d("SamplingResultsActivity", "Cancelled Entrants: " + cancelledEntrants.size());

            // Set up the RecyclerViews for selected and cancelled entrants
            selectedAdapter = new EntrantWaitlistAdapter(this, selectedEntrants);
            selectedEntrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            selectedEntrantsRecyclerView.setAdapter(selectedAdapter);

            cancelledAdapter = new EntrantWaitlistAdapter(this, cancelledEntrants);
            cancelledEntrantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            cancelledEntrantsRecyclerView.setAdapter(cancelledAdapter);
        }

        if (selectedEntrants != null) {
            Log.d("SamplingResultsActivity", "Selected Entrants:");
            for (Entrant entrant : selectedEntrants) {
                Log.d("SamplingResultsActivity", "Name: " + entrant.getName() +
                        ", Email: " + entrant.getEmail() +
                        ", Device ID: " + entrant.getId());
            }
        }

        if (cancelledEntrants != null) {
            Log.d("SamplingResultsActivity", "Cancelled Entrants:");
            for (Entrant entrant : cancelledEntrants) {
                Log.d("SamplingResultsActivity", "Name: " + entrant.getName() +
                        ", Email: " + entrant.getEmail() +
                        ", Device ID: " + entrant.getId());
            }
        }

        sendNotificationsButton = findViewById(R.id.send_notifications);
        sendNotificationsButton.setOnClickListener(v -> {
            if (selectedEntrants != null && !selectedEntrants.isEmpty()) {
                for (Entrant entrant : selectedEntrants) {
                    String deviceId = entrant.getId(); // Assume this is the device ID (FCM token)
                    Log.d("Notification", "selectedEntrant deviceId: "+ deviceId);
                    String name = entrant.getName();  // Entrant's name for personalization
                    if (deviceId != null && !deviceId.isEmpty()) {
                        // Send a personalized notification
                        String title = "Congratulations, " + name + "!";
                        String message = "You have been selected in the lottery. Check your invitations for details.";
                        addNotificationToEntrant(deviceId, title, message);
                    } else {
                        Log.e("Notification", "Device ID is null or empty for entrant: " + name);
                    }
                }
                Toast.makeText(this, "Notifications sent to selected entrants!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No selected entrants to notify.", Toast.LENGTH_SHORT).show();
            }
            if (cancelledEntrants != null && !cancelledEntrants.isEmpty()) {
                for (Entrant entrant : cancelledEntrants) {
                    String deviceId = entrant.getId(); // Assume this is the device ID (FCM token)
                    String name = entrant.getName();  // Entrant's name for personalization
                    if (deviceId != null && !deviceId.isEmpty()) {
                        // Send a personalized notification
                        String title = "Oops " + name + "!";
                        String message = "You weren't selected in the lottery. Check your app for details.";
                        addNotificationToEntrant(deviceId, title, message);
                    } else {
                        Log.e("Notification", "Device ID is null or empty for entrant: " + name);
                    }
                }
                Toast.makeText(this, "Notifications sent to selected entrants!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No selected entrants to notify.", Toast.LENGTH_SHORT).show();
            }
        });

    }

//    public void makeNotification(String deviceId, String title, String message) {
//        // Create a notification channel if running on Android O or later
//        String channelID = "CHANNEL_ID_NOTIFICATION";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            if (notificationManager != null) {
//                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);
//                if (notificationChannel == null) {
//                    int importance = NotificationManager.IMPORTANCE_HIGH;
//                    notificationChannel = new NotificationChannel(channelID, "Notification Channel", importance);
//                    notificationChannel.setLightColor(Color.GREEN);
//                    notificationChannel.enableVibration(true);
//                    notificationManager.createNotificationChannel(notificationChannel);
//                }
//            }
//        }
//
//        // Build the notification
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
//                .setSmallIcon(R.drawable.notifications)
//                .setContentTitle(title)  // Use the passed-in title
//                .setContentText(message) // Use the passed-in message
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//        // Set up a pending intent (optional)
//        Intent intent = new Intent(this, NotificationMessaging.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("data", "some value");
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                this,
//                0,
//                intent,
//                PendingIntent.FLAG_MUTABLE
//        );
//        builder.setContentIntent(pendingIntent);
//
//        // Show the notification
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (notificationManager != null) {
//            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
//            Log.d("NotificationActivity", "Notification sent to device: " + deviceId);
//
//            // Now add the notification to Firestore for the respective entrant
//            addNotificationToEntrant(deviceId, title, message);
//        } else {
//            Log.e("NotificationActivity", "NotificationManager is null");
//        }
//    }

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



//    public void makeNotification(String deviceId, String title, String message) {
//        String channelID = "CHANNEL_ID_NOTIFICATION";
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
//
//        builder.setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle("Notification Title")
//                .setContentText("some text")
//                .setAutoCancel(true)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        Intent intent = new Intent(getApplicationContext(), NotificationMessaging.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("data", "some value");
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                getApplicationContext(),
//                0,
//                intent,
//                PendingIntent.FLAG_MUTABLE // Use FLAG_IMMUTABLE for a non-mutable PendingIntent
//        );
//
//        builder.setContentIntent(pendingIntent);
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (notificationManager != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);
//                if (notificationChannel == null) {
//                    int importance = NotificationManager.IMPORTANCE_HIGH;
//                    notificationChannel = new NotificationChannel(channelID, "Notification Channel", importance);
//                    notificationChannel.setLightColor(Color.GREEN);
//                    notificationChannel.enableVibration(true);
//                    notificationManager.createNotificationChannel(notificationChannel);
//                }
//            }
//            notificationManager.notify(0, builder.build());
//            Log.d("NotificationActivity", "Button clicked, attempting to send notification...");
//        } else {
//            Log.e("NotificationActivity", "NotificationManager is null");
//        }
//
//    }
}