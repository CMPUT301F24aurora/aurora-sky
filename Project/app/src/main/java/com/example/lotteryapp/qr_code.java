package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class qr_code extends AppCompatActivity {

    private ImageView qrCodeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);

        qrCodeImage = findViewById(R.id.qr_code_image);

        // Retrieve the Event object
        Event event = (Event) getIntent().getSerializableExtra("event_data");

        if (event != null) {
            // Generate and display the QR code bitmap
            Bitmap qrCodeBitmap = event.generateQRCodeBitmap();
            qrCodeImage.setImageBitmap(qrCodeBitmap);
        }
    }
}
