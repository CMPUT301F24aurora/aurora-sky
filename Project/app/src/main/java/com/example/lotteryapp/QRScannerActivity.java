package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        scanQRCode();
    }

    private void scanQRCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setCaptureActivity(CaptureActivity.class);
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

    private void fetchEventFromFirestore(String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Event document found, handle the event data
                        // You can extract the event details here
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            // Do something with the event data, for example, navigate to a new activity
                            Log.d(TAG, "Event data: " + event.toString());
                            Intent intent = new Intent(QRScannerActivity.this, EntrantEventDetailsActivity.class);
                            intent.putExtra("event_data", event); // Passing event data to another activity
                            startActivity(intent);
                        }
                    } else {
                        // Event document does not exist
                        Toast.makeText(QRScannerActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors (e.g., no internet connection)
                    Log.e(TAG, "Error fetching event data", e);
                    Toast.makeText(QRScannerActivity.this, "Error fetching event data", Toast.LENGTH_SHORT).show();
                });
    }
}
