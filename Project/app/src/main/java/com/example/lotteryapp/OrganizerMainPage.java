package com.example.lotteryapp;

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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.entrant_nav) {
                    Intent intent = new Intent(OrganizerMainPage.this, EntrantsEventsActivity.class);
                    startActivity(intent);
                } else if (id == R.id.organizer_nav) {
                    Toast.makeText(OrganizerMainPage.this, "You are on the organizer page", Toast.LENGTH_SHORT).show();
                    // Add your navigation logic here
                }
                drawerLayout.closeDrawers(); // Close drawer after selection
                return true;
            }
        });
    }
}
