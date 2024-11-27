package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
/**
 * The {@code qr_code} class is responsible for displaying a QR code image for a specified {@code Event}.
 * It retrieves an {@code Event} object from the intent, generates a QR code, and displays it in an {@code ImageView}.
 * This class extends {@code AppCompatActivity}.
 *
 * @see AppCompatActivity
 * @see Event
 * @see Bitmap
 * @version v1
 * @since v1
 * @author Team Aurora
 */
public class qr_code extends AppCompatActivity {

    private ImageView qrCodeImage;

    /**
     * Initializes the activity, retrieves the {@code Event} data passed through the intent,
     * generates a QR code bitmap from the event, and displays it in an {@code ImageView}.
     *
     * @param savedInstanceState the saved instance state of the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);

        qrCodeImage = findViewById(R.id.qr_code_image);

        // Retrieve the Event object
        Event event = (Event) getIntent().getSerializableExtra("event_data");
        Organizer organizer = (Organizer) getIntent().getSerializableExtra("organizer_data");
        Entrant entrant = (Entrant) getIntent().getSerializableExtra("entrant_data");

        if (event != null) {
            // Generate and display the QR code bitmap
            Bitmap qrCodeBitmap = event.generateQRCodeBitmap();
            qrCodeImage.setImageBitmap(qrCodeBitmap);
        }
    }
}
