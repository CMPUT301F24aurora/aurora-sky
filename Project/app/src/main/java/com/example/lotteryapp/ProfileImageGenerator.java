package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * The ProfileImageGenerator class provides a method to generate a bitmap image representing
 * a user's initials on a colored background. This is typically used for generating user profile
 * images where the image is created dynamically from the user's name.
 * <p>
 * The generated image will display the first two letters of the user's name, in uppercase, centered
 * on a square background with a color derived from the user's name.
 * </p>
 *
 * @see Bitmap
 * @see Canvas
 * @see Paint
 */
public class ProfileImageGenerator {

    /**
     * Generates a bitmap image that displays the initials of a given text (typically a name) on a colored background.
     * <p>
     * The method creates a 400x400 pixel image where the background color is determined by the
     * hash of the input text. The initials of the text are then drawn at the center of the image.
     * </p>
     *
     * @param text The input string from which the initials will be extracted and displayed on the image.
     *             Typically, this would be a user's name or any string.
     * @return A Bitmap object containing the generated image with the initials on the background.
     * @throws NullPointerException if the input text is null.
     * @see Bitmap
     * @see Canvas
     * @see Paint
     */
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

    /**
     * Generates a background color for the image based on the input text (usually the user's name).
     * <p>
     * This method converts the input text into a hash code and then derives a unique RGB color
     * from the hash value to use as the background color.
     * </p>
     *
     * @param text The input string from which the background color will be derived.
     * @return An integer representing the RGB color to be used as the background.
     * @see Color
     */
    // Helper method to generate a background color based on the entrant's name
    private static int generateBackgroundColor(String text) {
        // Generate a hash code from the text (entrant's name) and use it to create a color
        int color = text.hashCode();
        // Ensure the color is a valid RGB color (you can tweak this as per your preference)
        return Color.argb(255, Math.abs(color % 256), Math.abs((color / 256) % 256), Math.abs((color / 65536) % 256));
    }
}