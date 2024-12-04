package com.example.lotteryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private Button scanEventButton;
    private DBManagerEvent dbManagerEvent;
    private Organizer organizer;
    private Entrant entrant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        dbManagerEvent = new DBManagerEvent();

        scanEventButton = findViewById(R.id.scan_event_button);
        scanEventButton.setOnClickListener(view -> startQRCodeScanner());
    }

    private void startQRCodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{
       if (result.getContents()!=null){
           String qrCode = result.getContents();
           testEvent(qrCode);
           Log.d("", qrCode);

       }
    });

    public void testEvent(String qrcode) {
        dbManagerEvent.getEventByQRCode(qrcode, new DBManagerEvent.GetEventCallback() {
            @Override
            public void onSuccess(Event event) {
                showOptions(event);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QRScannerActivity.this, "Error fetching event data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOptions(Event event){
        new AlertDialog.Builder(QRScannerActivity.this)
                .setTitle(event.getEventName())
                .setMessage("Choose an option:")
                .setPositiveButton("Sign Up", (dialog, which) -> {
                    navigateToSignUp(event);  // Navigate to the sign-up activity
                })
                .setNegativeButton("View Event", (dialog, which) -> {
                    navigateToEventDetails(event);  // Navigate to the event details activity
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();  // Dismiss the dialog
                })
                .show();
    }

    private void navigateToSignUp(Event event){
        Intent intent = new Intent(QRScannerActivity.this, EntrantEventDetailsActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", entrant);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("sign_up", true);
        Log.d(TAG, "Navigating to event details for event: " + event.getEventName());
        startActivity(intent);

    }

    private void navigateToEventDetails(Event event){
        Intent intent = new Intent(QRScannerActivity.this, EntrantEventDetailsActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", entrant);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("sign_up", false);
        startActivity(intent);
    }
}