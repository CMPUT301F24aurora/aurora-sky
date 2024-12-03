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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * {@code QRScannerActivity} is an activity that allows the user to scan QR codes associated with events.
 * Upon scanning a QR code, the event details are fetched from Firebase Firestore, and the user is presented with options
 * to either sign up for the event or view event details.
 *
 * @version v1
 * @since v1
 * @author Team Aurora
 * @see AppCompatActivity
 * @see Entrant
 * @see Event
 * @see FirebaseFirestore
 */
public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DecoratedBarcodeView barcodeScannerView;

    private Entrant currentEntrant;
    private Organizer organizer;
    private DBManagerEvent dbManagerEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        currentEntrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        dbManagerEvent = new DBManagerEvent();
        Toast.makeText(this, "Entrant Name: " + currentEntrant.getName(), Toast.LENGTH_SHORT).show();

        Button scanEventButton = findViewById(R.id.scan_event_button);

        // Set up the button to start scanning
        scanEventButton.setOnClickListener(view -> startQRCodeScanner());
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }

    /**
     * Starts the QR code scanner by making the barcode scanner view visible and initiating a scan.
     */
    private void startQRCodeScanner() {
        barcodeScannerView.setVisibility(View.VISIBLE);
        scanQRCode();
    }

    /**
     * Initiates the QR code scan with specified options.
     */
    private void scanQRCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR Code");
        options.setBeepEnabled(true);
        barcodeLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String qrData = result.getContents();
                    handleScanResult(qrData);
                } else {
                    Toast.makeText(this, "Scan failed, try again.", Toast.LENGTH_SHORT).show();
                }
            });

    private void handleScanResult(String qrData) {
        if (qrData != null && !qrData.isEmpty()) {
            barcodeScannerView.pause();
            new Handler().postDelayed(() -> testEvent(qrData), 100);
        } else {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

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

    /**
     * Displays the sign-up and view event buttons once the event has been successfully fetched.
     *
     * @param event the event fetched from Firestore
     */
    private void showOptions(Event event) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        //barcodeScannerView.setVisibility(View.GONE);
        new AlertDialog.Builder(QRScannerActivity.this)
                .setTitle("Event Options")
                .setMessage("Choose an option:")
                .setPositiveButton("Sign Up", (dialog, which) -> {
                    navigateToSignUp(event);  // Navigate to the sign-up activity
                })
                .setNegativeButton("View Event", (dialog, which) -> {
                    navigateToEventDetails(event);  // Navigate to the event details activity
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();  // Dismiss the dialog
                    Intent intent = new Intent(QRScannerActivity.this, QRScannerActivity.class);
                    intent.putExtra("entrant_data", currentEntrant);
                    intent.putExtra("organizer_data", organizer);
                    startActivity(intent);
                })
                .setOnDismissListener(dialog -> barcodeScannerView.resume())  // Resume scanning when dialog is dismissed
                .show();
    }

    /**
     * Navigates to the EntrantEventDetailsActivity to allow the user to sign up for an event.
     *
     * @param event The event that the user intends to sign up for.
     */
    private void navigateToSignUp(Event event) {
        Intent intent = new Intent(QRScannerActivity.this, EntrantEventDetailsActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", currentEntrant);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("sign_up", true);
        startActivity(intent);
    }

    /**
     * Navigates to the EntrantEventDetailsActivity to view the details of an event without signing up.
     *
     * @param event The event for which the user is viewing details.
     */
    private void navigateToEventDetails(Event event) {
        Intent intent = new Intent(QRScannerActivity.this, EntrantEventDetailsActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", currentEntrant);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("sign_up", false);
        Log.d(TAG, "Navigating to event details for event: " + event.getEventName());
        startActivity(intent);
    }
}