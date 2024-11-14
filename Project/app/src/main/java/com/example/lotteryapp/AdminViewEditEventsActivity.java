package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

/**
 * The AdminViewEditEventsActivity class allows admin users to view and edit events.
 * This activity displays a list of events and provides search functionality.
 *
 * @see AppCompatActivity
 * @see AdminEventAdapter
 * @see Event
 * @see FirebaseFirestore
 * @version v1
 *
 * @author Team Aurora
 */
public class AdminViewEditEventsActivity extends AppCompatActivity implements AdminEventAdapter.AdminEventClickListener {

    private RecyclerView adminEvList;
    private List<Event> eventList;
    private List<Event> filteredEventList;
    private AdminEventAdapter adapter;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * This method sets up the layout, initializes the RecyclerView, and loads events from the database.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_view_events);

        adminEvList = findViewById(R.id.admin_ev_list);
        eventList = new ArrayList<>();
        adapter = new AdminEventAdapter(eventList, this);

        adminEvList.setLayoutManager(new LinearLayoutManager(this));
        adminEvList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadEvents();

        // Set up SearchView
        SearchView searchView = findViewById(R.id.admin_search_ev);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Called when a query is submitted in the SearchView.
             *
             * @param query the search query
             * @return false to indicate the query has been handled
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                filteredEventList = adapter.filter(query);
                eventList.clear();
                eventList.addAll(filteredEventList);
                adapter.notifyDataSetChanged();
                return false;
            }

            /**
             * Called when the query text is changed in the SearchView.
             *
             * @param newText the new text in the SearchView
             * @return false to indicate the query text change has been handled
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.trim().isEmpty()) {
                    filteredEventList = adapter.filter(newText);
                    eventList.clear();
                    eventList.addAll(filteredEventList);
                    adapter.notifyDataSetChanged();
                }
                else{
                    loadEvents();
                }
                return false;
            }
        });
    }

    /**
     * Loads events from the database and updates the event list.
     * Retrieves events from the "events" collection in Firestore and adds them to the event list.
     *
     * @see FirebaseFirestore#collection(String)
     */
    private void loadEvents() {
        CollectionReference eventsRef = db.collection("events");
        eventsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            /**
             * Called when the task to retrieve events is complete.
             *
             * @param task the task to retrieve events
             */
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminViewEditEventsActivity.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Called when an event is clicked.
     * Starts the AdminViewEventsContent activity and passes the event details to it.
     *
     * @param event the clicked event
     * @see AdminViewEventsContent
     */
    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, AdminViewEventsContent.class);
        intent.putExtra("eventName", event.getName());
        intent.putExtra("eventDate", event.getEventDate());
        intent.putExtra("eventDescription", event.getDescription());
        intent.putExtra("eventId", event.getQR_code());
        intent.putExtra("eventHash", event.getQR_code());
        startActivity(intent);
    }
}

