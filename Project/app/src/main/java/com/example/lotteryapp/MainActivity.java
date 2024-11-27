package com.example.lotteryapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@code MainActivity} class represents the main entry point of the application.
 * It handles user authentication and navigation to the appropriate user type pages.
 * The activity also checks if the device belongs to an admin and displays the admin page link accordingly.
 *
 * @see AppCompatActivity
 * @see FirebaseFirestore
 * @see EntrantsEventsActivity
 * @see OrganizerMainPage
 * @see EntrantProfileEditActivity
 * @see AdminHomepageActivity
 * @version v1
 * @since v1
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkNotificationField();

        // Button for Entrant
        Button entrantButton = findViewById(R.id.entrantButton);
        entrantButton.setOnClickListener(v -> checkUserExistsAndNavigate("entrant"));

        // Button for Organizer
        Button organizerButton = findViewById(R.id.organizerButton);
        organizerButton.setOnClickListener(v -> checkUserExistsAndNavigate("organizer"));

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        // Admin status check
        String deviceId = getDeviceId(this);
        Log.i("Device Id: ", deviceId);
        checkAdminAndDisplayPage(deviceId);
    }

    /**
     * Checks if a user exists in the specified user type collection and navigates to the appropriate page.
     * If the user does not exist, navigates to the profile edit page.
     *
     * @param userType the type of user, either "entrant" or "organizer"
     * @see EntrantsEventsActivity
     * @see OrganizerMainPage
     * @see EntrantProfileEditActivity
     */

    private void checkUserExistsAndNavigate(String userType) {
        String deviceId = getDeviceId(this);

        DatabaseManager.getEntrant(this, new GetEntrantCallback() {
            @Override
            public void onEntrantFound(Entrant entrant) {
                // If entrant is found, we assume organizer also exists
                DatabaseManager.getOrganizerByDeviceId(deviceId, new GetOrganizerCallback() {
                    @Override
                    public void onOrganizerFound(Organizer organizer) {
                        navigateToAppropriateActivity(userType, entrant, organizer);
                    }

                    @Override
                    public void onOrganizerNotFound() {
                        // This shouldn't happen based on our assumption, but handle it just in case
                        Log.w(TAG, "Organizer not found even though Entrant exists");
                        navigateToAppropriateActivity(userType, entrant, null);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error fetching organizer", e);
                        navigateToAppropriateActivity(userType, entrant, null);
                    }
                });
            }

            @Override
            public void onEntrantNotFound(Exception e) {
                // If entrant is not found, we navigate to profile edit
                navigateToProfileEdit(userType);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching entrant", e);
                navigateToProfileEdit(userType);
            }
        });
    }

    private void navigateToAppropriateActivity(String userType, Entrant entrant, Organizer organizer) {
        Intent intent;
        if (userType.equals("entrant")) {
            intent = new Intent(MainActivity.this, EntrantsEventsActivity.class);
        } else {
            intent = new Intent(MainActivity.this, OrganizerMainPage.class);
        }

        intent.putExtra("entrant_data", entrant);
        intent.putExtra("organizer_data", organizer);

        startActivity(intent);
    }

    private void navigateToProfileEdit(String userType) {
        Intent intent = new Intent(MainActivity.this, EntrantProfileEditActivity.class);
        intent.putExtra("userType", userType);
        startActivity(intent);
    }

    /**
     * Checks if the device belongs to an admin and displays the admin sign-in button if true.
     *
     * @param deviceId the unique identifier of the device
     * @see AdminHomepageActivity
     */

    private void checkAdminAndDisplayPage(String deviceId) {
        db.collection("admin").whereEqualTo("id", deviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Button adminSignInButton = findViewById(R.id.admin_link);
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            adminSignInButton.setVisibility(View.VISIBLE);
                            adminSignInButton.setOnClickListener(v -> navigateToAdminHomepage());
                        } else {
                            adminSignInButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * Navigates to the admin homepage if the user is an admin.
     *
     * @see AdminHomepageActivity
     */
    private void navigateToAdminHomepage() {
        Intent intent = new Intent(MainActivity.this, AdminHomepageActivity.class);
        startActivity(intent);
    }

    /**
     * Retrieves the unique device ID for the current device.
     *
     * @param context the context of the current state of the application
     * @return the unique device ID as a {@code String}
     */
    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void checkNotificationField() {
        String deviceId = getDeviceId(this);
        Log.d(TAG, "Device ID: " + deviceId);

        db.collection("entrants")
                .whereEqualTo("id", deviceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        // Retrieve the notifications array
                        List<Map<String, Object>> notifications =
                                (List<Map<String, Object>>) document.get("notifications");

                        if (notifications != null && !notifications.isEmpty()) {
                            // Send a notification for each entry in the array
                            for (Map<String, Object> notification : notifications) {
                                String title = (String) notification.get("title");
                                String message = (String) notification.get("message");

                                if (title != null && message != null) {
                                    sendNotification(deviceId, title, message);
                                }
                            }

//                            // Optionally clear the notifications array after processing
//                            document.getReference().update("notifications", new ArrayList<>());
                        }
                    } else {
                        Log.d(TAG, "No entrant found or notifications field is empty.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking notification field", e));
    }

//    /**
//     * Sends a notification to the user.
//     *
//     * @param title   the title of the notification
//     * @param message the message body of the notification
//     */
//    private void sendNotification(String title, String message) {
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
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
//                .setSmallIcon(R.drawable.ic_notification) // Replace with your app's notification icon
//                .setContentTitle(title)
//                .setContentText(message)
//                .setPriority(NotificationCompat.PRIORITY_HIGH);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
//    }

    public void sendNotification(String deviceId, String title, String message) {
        // Create a notification channel if running on Android O or later
        String channelID = "CHANNEL_ID_NOTIFICATION";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelID);
                if (notificationChannel == null) {
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    notificationChannel = new NotificationChannel(channelID, "Notification Channel", importance);
                    notificationChannel.setLightColor(Color.GREEN);
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle(title)  // Use the passed-in title
                .setContentText(message) // Use the passed-in message
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Set up a pending intent (optional)
        Intent intent = new Intent(this, NotificationMessaging.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "some value");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE
        );
        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d("NotificationActivity", "Notification sent to device: " + deviceId);
        } else {
            Log.e("NotificationActivity", "NotificationManager is null");
        }
    }

}
