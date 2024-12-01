package com.example.lotteryapp;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;

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

            // Add functionality to download the QR code when the button is clicked
            Button downloadButton = findViewById(R.id.download_button);
            downloadButton.setOnClickListener(v -> {
                saveQRCodeToGallery(qrCodeBitmap);
            });
        }
    }

    // Save the QR code bitmap to the gallery
    private void saveQRCodeToGallery(Bitmap qrCodeBitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API level 29) and above, use MediaStore API to save the image
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "QR_Code.png");
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QR_Codes"); // Directory in the gallery

            // Insert the image into the MediaStore
            Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (imageUri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                    if (outputStream != null) {
                        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.flush();
                        Toast.makeText(this, "QR code saved to gallery", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show();
            }
        } else {
            // For Android versions below API level 29, use external storage (deprecated)
            // Handle saving in older versions, but this approach is not recommended for newer Android versions
        }
    }
}
