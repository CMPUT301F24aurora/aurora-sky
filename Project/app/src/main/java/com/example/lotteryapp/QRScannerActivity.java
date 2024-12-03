package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * {@code QRScannerActivity} scans QR codes and fetches event details.
 * Displays options to sign up for or view the event.
 */
public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Entrant currentEntrant;
    private Organizer organizer;
    private DBManagerEvent dbManagerEvent;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String qrData = result.getContents();
                    handleScanResult(qrData);
                } else {
                    Toast.makeText(this, "Scan failed, please try again.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        currentEntrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        dbManagerEvent = new DBManagerEvent();

        Button scanEventButton = findViewById(R.id.scan_event_button);
        scanEventButton.setOnClickListener(view -> startQRCodeScanner());
    }

    /**
     * Starts the QR code scanner with prompt options.
     */
    private void startQRCodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR Code");
        options.setBeepEnabled(true);
        barcodeLauncher.launch(options);
    }

    /**
     * Handles the scanned QR code result.
     *
     * @param qrData The scanned QR code data.
     */
    private void handleScanResult(String qrData) {
        if (qrData != null && !qrData.isEmpty()) {
            new Handler().postDelayed(() -> fetchEventData(qrData), 100);
        } else {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches event data from Firestore based on the QR code.
     *
     * @param qrCode The scanned QR code data.
     */
    private void fetchEventData(String qrCode) {
        dbManagerEvent.getEventByQRCode(qrCode, new DBManagerEvent.GetEventCallback() {
            @Override
            public void onSuccess(Event event) {
                showEventOptionsDialog(event);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(QRScannerActivity.this, "Error fetching event data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays an alert dialog with options to sign up for or view the event.
     *
     * @param event The fetched event data.
     */
    private void showEventOptionsDialog(Event event) {
        new AlertDialog.Builder(this)
                .setTitle(event.getEventName())
                .setMessage("What would you like to do?")
                .setPositiveButton("Sign Up", (dialog, which) -> navigateToEventDetails(event, true))
                .setNegativeButton("View Event", (dialog, which) -> navigateToEventDetails(event, false))
                .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Navigates to the EntrantEventDetailsActivity with the selected event data.
     *
     * @param event   The event to view or sign up for.
     * @param signUp  Indicates whether the user is signing up.
     */
    private void navigateToEventDetails(Event event, boolean signUp) {
        Intent intent = new Intent(this, EntrantEventDetailsActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", currentEntrant);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("sign_up", signUp);
        startActivity(intent);
    }
}
