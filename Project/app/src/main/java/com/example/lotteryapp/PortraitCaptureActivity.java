package com.example.lotteryapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * PortraitCaptureActivity is an extension of the CaptureActivity class that locks the screen orientation
 * to portrait mode when the activity is created.
 * <p>
 * This activity overrides the {@link #onCreate(Bundle)} method to set the screen orientation
 * to portrait mode by calling {@link #setRequestedOrientation(int)} with
 * {@link ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}.
 * </p>
 *
 * @see CaptureActivity
 * @see ActivityInfo
 */
public class PortraitCaptureActivity extends CaptureActivity {

    /**
     * Called when the activity is created. This method locks the screen orientation
     * to portrait mode for this activity.
     * <p>
     * The method uses {@link #setRequestedOrientation(int)} to request that the activity
     * remains in portrait orientation during its lifecycle.
     * </p>
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     *                           If the activity has never been created before, this value is null.
     * @return void
     * @see ActivityInfo#SCREEN_ORIENTATION_PORTRAIT
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // Locks this activity in portrait
    }
}
