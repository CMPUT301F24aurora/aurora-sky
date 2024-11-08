package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class ProfileImageGenerator {

    public static Bitmap generateTextDrawable(String text) {
        int width = 400;
        int height = 400;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(generateBackgroundColor(text));

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(150f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        String initials = text.length() > 1 ? text.substring(0, 2).toUpperCase() : text.toUpperCase();
        Rect textBounds = new Rect();
        paint.getTextBounds(initials, 0, initials.length(), textBounds);

        float x = canvas.getWidth() / 2f;
        float y = (canvas.getHeight() / 2f) - (textBounds.exactCenterY());
        canvas.drawText(initials, x, y, paint);

        return bitmap;
    }

    // Helper method to generate a background color based on the entrant's name
    private static int generateBackgroundColor(String text) {
        // Generate a hash code from the text (entrant's name) and use it to create a color
        int color = text.hashCode();
        // Ensure the color is a valid RGB color (you can tweak this as per your preference)
        return Color.argb(255, Math.abs(color % 256), Math.abs((color / 256) % 256), Math.abs((color / 65536) % 256));
    }
}