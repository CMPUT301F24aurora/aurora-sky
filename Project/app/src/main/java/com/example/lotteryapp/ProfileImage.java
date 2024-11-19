package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class ProfileImage {

    // Method for generating an arbitrary picture
    public Bitmap generateArbitraryPicture(String name) {
        int size = 200; // Size of the profile image
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Generate background color
        int backgroundColor = generateRandomColor();
        canvas.drawColor(backgroundColor);

        // Draw the first letter of the name
        String firstLetter = name.substring(0, 1).toUpperCase();
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80); // Adjust text size as needed
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);

        // Center the text
        Rect bounds = new Rect();
        textPaint.getTextBounds(firstLetter, 0, firstLetter.length(), bounds);
        int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
        int y = (canvas.getHeight() / 2) + (bounds.height() / 2);

        canvas.drawText(firstLetter, x, y, textPaint);

        return bitmap;
    }

    // Helper method to generate a random color
    private int generateRandomColor() {
        // This is a simple implementation, you can customize it further
        return Color.rgb(
                (int) (Math.random() * 255),
                (int) (Math.random() * 255),
                (int) (Math.random() * 255)
        );
    }

    // Method for uploading a picture
    public void uploadPicture() {
        // TODO: Add implementation to upload the picture
    }
}
