package com.example.lotteryapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Handle notification messages
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body);
        }

        // Handle data payloads
        if (remoteMessage.getData().size() > 0) {
            handleCustomData(remoteMessage.getData());
        }
    }

    private void showNotification(String title, String message) {
        String channelId = "LotteryChannel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE
        );
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Lottery Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for lottery events");
            channel.setLightColor(Color.BLUE);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, builder.build());
    }

    private void handleCustomData(Map<String, String> data) {
        // Example: Log data or trigger specific actions
        for (Map.Entry<String, String> entry : data.entrySet()) {
            // Handle the custom key-value pairs
            String key = entry.getKey();
            String value = entry.getValue();
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // Send the new token to your server
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        // Example: Send the token to your backend or save it in Firestore
    }
}
