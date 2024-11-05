package com.example.lotteryapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Set;

public class OrganizerMainPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_main_page);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.menu_button);


        // Find the Create Event button by its ID
        Button createEventButton = findViewById(R.id.create_event_button);

        // Set an OnClickListener to navigate to the CreateEventActivity
        createEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrganizerMainPage.this, OrganizerCreateEvent.class);
            startActivity(intent);
        });

        //Set up the toggle for the navigation drawer
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();

        // Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.entrant_nav) {
                    // Get the Entrant object (assuming you have a Context available)
                    Entrant.getEntrant(OrganizerMainPage.this, new GetEntrantCallback() {
                        @Override
                        public void onEntrantFound(Entrant entrant) {
                            // Create the intent and add the entrant data
                            Intent intent = new Intent(OrganizerMainPage.this, EntrantsEventsActivity.class);
                            intent.putExtra("entrant_data", entrant);
                            startActivity(intent);
                        }

                        @Override
                        public void onEntrantNotFound(Exception e) {
                            // Handle the case where the entrant is not found
                            Toast.makeText(OrganizerMainPage.this, "Entrant not found", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            // Handle errors that occurred during the retrieval process
                            Toast.makeText(OrganizerMainPage.this, "Error retrieving entrant data", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (id == R.id.organizer_nav) {
                    Toast.makeText(OrganizerMainPage.this, "You are on the organizer page", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.map_nav) {
                    Intent intent = new Intent(OrganizerMainPage.this, MapActivity.class);
                    startActivity(intent);
                }
                drawerLayout.closeDrawers(); // Close drawer after selection
                return true;
            }
        });
    }
}
