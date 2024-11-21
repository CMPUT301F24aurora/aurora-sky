package com.example.lotteryapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
/**
 * The {@code MainActivity} class represents the main entry point of the application.
 * It handles user authentication and navigation to the appropriate user type pages.
 * The activity also checks if the device belongs to an admin and displays the admin page link accordingly.
 *
 * @see AppCompatActivity
 * @see FirebaseFirestore
 * @see EntrantsEventsActivity
 * @see OrganizerMainPage
 * @see EntrantProfileEditActivity
 * @see AdminHomepageActivity
 * @version v1
 * @since v1
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Button for Entrant
        Button entrantButton = findViewById(R.id.entrantButton);
        entrantButton.setOnClickListener(v -> checkUserExistsAndNavigate("entrant"));

        // Button for Organizer
        Button organizerButton = findViewById(R.id.organizerButton);
        organizerButton.setOnClickListener(v -> checkUserExistsAndNavigate("organizer"));

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        // Admin status check
        String deviceId = getDeviceId(this);
        Log.i("Device Id: ", deviceId);
        checkAdminAndDisplayPage(deviceId);
    }

    /**
     * Checks if a user exists in the specified user type collection and navigates to the appropriate page.
     * If the user does not exist, navigates to the profile edit page.
     *
     * @param userType the type of user, either "entrant" or "organizer"
     * @see EntrantsEventsActivity
     * @see OrganizerMainPage
     * @see EntrantProfileEditActivity
     */

    private void checkUserExistsAndNavigate(String userType) {
        String deviceId = getDeviceId(this);

        DatabaseManager.getEntrant(this, new GetEntrantCallback() {
            @Override
            public void onEntrantFound(Entrant entrant) {
                // If entrant is found, we assume organizer also exists
                DatabaseManager.getOrganizerByDeviceId(deviceId, new GetOrganizerCallback() {
                    @Override
                    public void onOrganizerFound(Organizer organizer) {
                        navigateToAppropriateActivity(userType, entrant, organizer);
                    }

                    @Override
                    public void onOrganizerNotFound() {
                        // This shouldn't happen based on our assumption, but handle it just in case
                        Log.w(TAG, "Organizer not found even though Entrant exists");
                        navigateToAppropriateActivity(userType, entrant, null);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error fetching organizer", e);
                        navigateToAppropriateActivity(userType, entrant, null);
                    }
                });
            }

            @Override
            public void onEntrantNotFound(Exception e) {
                // If entrant is not found, we navigate to profile edit
                navigateToProfileEdit(userType);
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching entrant", e);
                navigateToProfileEdit(userType);
            }
        });
    }

    private void navigateToAppropriateActivity(String userType, Entrant entrant, Organizer organizer) {
        Intent intent;
        if (userType.equals("entrant")) {
            intent = new Intent(MainActivity.this, EntrantsEventsActivity.class);
        } else {
            intent = new Intent(MainActivity.this, OrganizerMainPage.class);
        }

        intent.putExtra("entrant_data", entrant);
        intent.putExtra("organizer_data", organizer);

        startActivity(intent);
    }

    private void navigateToProfileEdit(String userType) {
        Intent intent = new Intent(MainActivity.this, EntrantProfileEditActivity.class);
        intent.putExtra("userType", userType);
        startActivity(intent);
    }

    /**
     * Checks if the device belongs to an admin and displays the admin sign-in button if true.
     *
     * @param deviceId the unique identifier of the device
     * @see AdminHomepageActivity
     */

    private void checkAdminAndDisplayPage(String deviceId) {
        db.collection("admin").whereEqualTo("id", deviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Button adminSignInButton = findViewById(R.id.admin_link);
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            adminSignInButton.setVisibility(View.VISIBLE);
                            adminSignInButton.setOnClickListener(v -> navigateToAdminHomepage());
                        } else {
                            adminSignInButton.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * Navigates to the admin homepage if the user is an admin.
     *
     * @see AdminHomepageActivity
     */
    private void navigateToAdminHomepage() {
        Intent intent = new Intent(MainActivity.this, AdminHomepageActivity.class);
        startActivity(intent);
    }

    /**
     * Retrieves the unique device ID for the current device.
     *
     * @param context the context of the current state of the application
     * @return the unique device ID as a {@code String}
     */
    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
