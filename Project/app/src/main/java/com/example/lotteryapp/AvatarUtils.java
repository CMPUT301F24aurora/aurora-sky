package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AvatarUtils {

    /**
     * Generates an avatar with the initial of the provided name.
     * The avatar will have a background color based on the name hash and a white initial in the center.
     *
     * @param name The name to generate the avatar for.
     * @return A Bitmap representing the avatar image.
     */
    public static Bitmap generateAvatarWithInitial(String name) {
        // Determine the initial to display on the avatar
        String initial = (name != null && !name.isEmpty()) ? String.valueOf(name.charAt(0)).toUpperCase() : "?";

        // Define avatar dimensions
        int size = 100; // Define avatar size in pixels
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Get a consistent background color based on the name's hash
        int color = getColorFromHash(name);

        // Paint the background
        Paint paint = new Paint();
        paint.setColor(color); // Set the generated background color
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, size, size, paint);

        // Set up paint for the initial text (white color)
        paint.setColor(Color.WHITE);
        paint.setTextSize(40); // Adjust text size
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        // Draw the initial in the center of the bitmap
        canvas.drawText(initial, size / 2f, size / 2f - (paint.descent() + paint.ascent()) / 2f, paint);

        return bitmap;
    }

    /**
     * Generates a color from the hash of a name. Produces consistent colors for each unique name.
     *
     * @param name The name to hash into a color.
     * @return An RGB color integer.
     */
    public static int getColorFromHash(String name) {
        String hash = hash(name);
        if (hash == null || hash.isEmpty()) return Color.GRAY; // Fallback color if hash fails
        return Color.parseColor("#" + hash.substring(0, 6)); // Use first 6 chars for color
    }

    /**
     * Hashes a string using SHA-256 and returns the hash in hexadecimal format.
     *
     * @param input The input string to hash.
     * @return A hexadecimal hash string, or null if an error occurs.
     */
    private static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
