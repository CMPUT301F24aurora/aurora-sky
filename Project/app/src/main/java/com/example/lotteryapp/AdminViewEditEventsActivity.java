package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    private SearchView searchView;
    private TextView noEventsText;
    private DBManagerEvent dbManagerEvent;

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
        noEventsText = findViewById(R.id.admin_no_events_text);
        eventList = new ArrayList<>();
        adapter = new AdminEventAdapter(eventList, this);
        //searchView = findViewById(R.id.admin_ev_search_view);

        dbManagerEvent = new DBManagerEvent();

        adminEvList.setLayoutManager(new LinearLayoutManager(this));
        adminEvList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        setupSearchView();
        loadEvents();

        /*
        // Set up SearchView
        SearchView searchView = findViewById(R.id.admin_search_ev);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //Called when a query is submitted in the SearchView.
             *
             * @param query the search query
             * @return false to indicate the query has been handled
             //
            @Override
            public boolean onQueryTextSubmit(String query) {
                filteredEventList = adapter.filter(query);
                eventList.clear();
                eventList.addAll(filteredEventList);
                adapter.notifyDataSetChanged();
                return false;
            }

            //
             * Called when the query text is changed in the SearchView.
             *
             * @param newText the new text in the SearchView
             * @return false to indicate the query text change has been handled
             //
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
    */
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.admin_ev_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);  // Apply filter on submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);  // Apply filter on text change
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
        dbManagerEvent.getEventsFromFirestore(new GetEventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                eventList.addAll(events);
                filteredEventList = new ArrayList<>(events);  // Reset filtered list with fresh data
                if (eventList.isEmpty()) {
                    noEventsText.setVisibility(View.VISIBLE);
                    adminEvList.setVisibility(View.GONE);
                } else {
                    noEventsText.setVisibility(View.GONE);
                    adminEvList.setVisibility(View.VISIBLE);
                }
                adapter.updateData(events);
                adapter.notifyDataSetChanged();  // Notify adapter that the data has changed
            }

            @Override
            public void onFailure(Exception e) {
                noEventsText.setVisibility(View.VISIBLE);
                adminEvList.setVisibility(View.GONE);
                noEventsText.setText("Failed to load events. Please try again.");
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
        intent.putExtra("event_data", event);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        loadEvents();
    }
}

