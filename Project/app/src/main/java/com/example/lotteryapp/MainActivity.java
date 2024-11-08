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

        // Admin status check
        String deviceId = getDeviceId(this);
        checkAdminAndDisplayPage(deviceId);
    }

    private void checkUserExistsAndNavigate(String userType) {
        String deviceId = getDeviceId(this);

        // Check if the user exists in either the entrants or organizers collection
        db.collection(userType.equals("entrant") ? "entrants" : "organizers").document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User exists, navigate to the appropriate page based on type
                        Intent intent = new Intent(MainActivity.this,
                                userType.equals("entrant") ? EntrantsEventsActivity.class : OrganizerMainPage.class);
                        startActivity(intent);
                    } else {
                        // User does not exist, navigate to profile edit page
                        Intent intent = new Intent(MainActivity.this, EntrantProfileEditActivity.class);
                        intent.putExtra("userType", userType);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error checking user in Firestore", e));
    }

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

    private void navigateToAdminHomepage() {
        Intent intent = new Intent(MainActivity.this, AdminHomepageActivity.class);
        startActivity(intent);
    }

    private String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
