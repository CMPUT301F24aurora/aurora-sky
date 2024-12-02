package com.example.lotteryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity handles the display of notification data passed through an intent.
 * It retrieves the data from the intent and displays it in a TextView.
 *
 * @see AppCompatActivity
 */
public class NotificationMessaging extends AppCompatActivity {
    TextView textView;

    /**
     * Initializes the activity and sets up the TextView to display the received notification data.
     * The data is passed to this activity via an intent.
     *
     * @param savedInstanceState A bundle containing the activity's previous state, or null if this is the first time the activity is being created.
     * @throws NullPointerException If the "data" extra in the intent is missing.
     * @see #getIntent()
     * @see #setContentView(int)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_text);

        textView = findViewById(R.id.textViewData);
        String data = getIntent().getStringExtra("data");
        textView.setText(data);
        Log.d("NotificationMessaging", "Received data: " + data);
        if (data != null) {
            textView.setText(data);
        }
    }
}
