package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
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
    private Button signUpButton, viewEventButton;

    private Entrant currentEntrant;
    private Organizer organizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        currentEntrant = (Entrant) getIntent().getSerializableExtra("entrant_data");
        organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        Toast.makeText(this, "Entrant Name: " + currentEntrant.getName(), Toast.LENGTH_SHORT).show();

        Button scanEventButton = findViewById(R.id.scan_event_button);
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        signUpButton = findViewById(R.id.sign_up_button);
        viewEventButton = findViewById(R.id.view_event_button);

        // Set up the button to start scanning
        scanEventButton.setOnClickListener(view -> startQRCodeScanner());

        // Initially hide the option buttons
        signUpButton.setVisibility(View.GONE);
        viewEventButton.setVisibility(View.GONE);
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
            fetchEventFromFirestore(qrData);
        } else {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches the event from Firestore using the event ID obtained from the QR code.
     *
     * @param eventId the ID of the event to retrieve from Firestore
     */
    private void fetchEventFromFirestore(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            showOptions(event);
                        }
                    } else {
                        Toast.makeText(QRScannerActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching event data", e);
                    Toast.makeText(QRScannerActivity.this, "Error fetching event data", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays the sign-up and view event buttons once the event has been successfully fetched.
     *
     * @param event the event fetched from Firestore
     */
    private void showOptions(Event event) {
        barcodeScannerView.setVisibility(View.GONE);
        signUpButton.setVisibility(View.VISIBLE);
        viewEventButton.setVisibility(View.VISIBLE);

        signUpButton.setOnClickListener(v -> navigateToSignUp(event));
        viewEventButton.setOnClickListener(v -> navigateToEventDetails(event));
    }

    private void navigateToSignUp(Event event) {
        Intent intent = new Intent(QRScannerActivity.this, EntrantEventDetailsActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", currentEntrant);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("sign_up", true);
        startActivity(intent);
    }

    private void navigateToEventDetails(Event event) {
        Intent intent = new Intent(QRScannerActivity.this, EntrantEventDetailsActivity.class);
        intent.putExtra("event_data", event);
        intent.putExtra("entrant_data", currentEntrant);
        intent.putExtra("organizer_data", organizer);
        intent.putExtra("sign_up", false);
        startActivity(intent);
    }
}