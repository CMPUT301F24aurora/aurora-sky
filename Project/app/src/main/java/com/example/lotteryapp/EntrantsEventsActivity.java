package com.example.lotteryapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class EntrantsEventsActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private ImageButton profileIcon;
    private RecyclerView eventsRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private TextView noEventsText;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrants_events_page);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton menuButton = findViewById(R.id.menu_button);

        // Set up profile icon
        profileIcon = findViewById(R.id.profile_icon);

        // Retrieve Entrant data
        Intent oldIntent = getIntent();
        Entrant entrant = (Entrant) oldIntent.getSerializableExtra("entrant_data");

        if (entrant != null) {
            // Check if the entrant has a profile image URL
            if (entrant.getProfileImageUrl() == null || entrant.getProfileImageUrl().isEmpty()) {
                // Set the generated initial as the icon if no profile image is available
                profileIcon.setImageBitmap(generateTextDrawable(entrant.getName()));
            } else {
                // Load the profile image if available
                Picasso.get().load(entrant.getProfileImageUrl()).into(profileIcon);
            }
        }

        // Handle profile icon click
        profileIcon.setOnClickListener(v -> {
            if (entrant != null) {
                Intent intent = new Intent(EntrantsEventsActivity.this, EntrantProfileActivity.class);
                intent.putExtra("entrant_data", entrant);
                startActivity(intent);
            }
        });

        //Open drawer when menu button is clicked
        menuButton.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.entrant_nav) {
                Toast.makeText(EntrantsEventsActivity.this, "You are on the entrant page", Toast.LENGTH_SHORT).show();
                // Add your navigation logic here
            } else if (id == R.id.organizer_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, OrganizerMainPage.class);
                startActivity(organizerIntent);
            } else if (id == R.id.map_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, MapActivity.class);
                startActivity(organizerIntent);
            } else if (id == R.id.qr_code_nav) {
                Intent organizerIntent = new Intent(EntrantsEventsActivity.this, QRScannerActivity.class);
                startActivity(organizerIntent);
            }
            drawerLayout.closeDrawers(); // Close drawer after selection
            return true;
        });

        // Initialize RecyclerView and TextView
        eventsRecyclerView = findViewById(R.id.events_recycler_view);
        noEventsText = findViewById(R.id.no_events_text);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize event list and adapter with click listener
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, this); // Pass `this` as the listener
        eventsRecyclerView.setAdapter(eventAdapter);

        // Load events into RecyclerView
        loadEvents();

    }

    public static class AvatarUtils {
        public static int getColorFromHash(String name) {
            String hash = hash(name);
            if (hash == null || hash.isEmpty()) return Color.GRAY;
            return Color.parseColor("#" + hash.substring(0, 6));
        }

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


    private Bitmap generateTextDrawable(String name) {
        String initial = name != null && !name.isEmpty() ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        int size = 100; // Define icon size
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Use the getColorFromHash method to generate a consistent background color
        int color = AvatarUtils.getColorFromHash(name);

        Paint paint = new Paint();
        paint.setColor(color); // Set the generated background color
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, size, size, paint);

        paint.setColor(Color.WHITE); // Set text color
        paint.setTextSize(40); // Set text size
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        // Draw the initial in the center of the bitmap
        canvas.drawText(initial, size / 2, size / 2 - ((paint.descent() + paint.ascent()) / 2), paint);

        return bitmap;
    }


    private int generateBackgroundColor(String name) {
        int hash = name.hashCode();
        int r = (hash & 0xFF0000) >> 16;
        int g = (hash & 0x00FF00) >> 8;
        int b = (hash & 0x0000FF);
        return Color.rgb(r, g, b);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the event list when the activity is resumed
        loadEvents();
    }

    private void loadEvents() {
        // Call the Firestore retrieval method from Event class
        Event.getEventsFromFirestore(new GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);

                // Check if events were retrieved and update visibility of RecyclerView and noEventsText
                if (eventList.isEmpty()) {
                    noEventsText.setVisibility(View.VISIBLE); // Show "No events available" message
                    eventsRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
                } else {
                    noEventsText.setVisibility(View.GONE); // Hide "No events available" message
                    eventsRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView with events
                }
                eventAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onFailure(Exception e) {
                // Handle the error (you can display a Toast or log the error)
                noEventsText.setVisibility(View.VISIBLE);
                eventsRecyclerView.setVisibility(View.GONE);
                noEventsText.setText("Failed to load events. Please try again.");
            }
        });
    }

    @Override
    public void onEventClick(Event event) {
        // Navigate to the event details page
        Intent eventDetailsIntent = new Intent(EntrantsEventsActivity.this, EntrantEventDetailsActivity.class);
//        Intent eventDetailsIntent = new Intent(EntrantsEventsActivity.this, qr_code.class);

        // Fetch the entrant data from Firestore
        Entrant.getEntrant(this, new GetEntrantCallback() {
            @Override
            public void onEntrantFound(Entrant fetchedEntrant) {
                // Successfully retrieved entrant, put the event and entrant data into the intent
                eventDetailsIntent.putExtra("event_data", event); // Assuming Event implements Serializable
                eventDetailsIntent.putExtra("entrant_data", fetchedEntrant); // Pass the fetched entrant data

                // Start the event details activity
                startActivity(eventDetailsIntent);
            }

            @Override
            public void onEntrantNotFound(Exception e) {
                // Handle case when entrant data is not found
                //Log.w(TAG, "Entrant not found", e);
                Toast.makeText(EntrantsEventsActivity.this, "Entrant not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                // Handle error while fetching entrant data
                //Log.e(TAG, "Error fetching entrant", e);
                Toast.makeText(EntrantsEventsActivity.this, "Error fetching entrant", Toast.LENGTH_SHORT).show();
            }
        });
    }

}