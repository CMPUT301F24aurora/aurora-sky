package com.example.lotteryapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AcceptDeclineActivity extends AppCompatActivity {
    private Button acceptButton;
    private Button declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accept_decline_invitation);

        acceptButton = findViewById(R.id.accept_button);
        declineButton = findViewById(R.id.decline_button);


    }
}
