package com.example.lotteryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The AdminViewEditProfilesActivity class allows admin users to view and edit entrant profiles.
 * This activity displays a list of entrants and provides search functionality.
 *
 * @see AppCompatActivity
 * @see EntrantAdapter
 * @see Entrant
 * @see FirebaseFirestore
 * @version v1
 *
 * @author Team Aurora
 */
public class AdminViewEditProfilesActivity extends AppCompatActivity implements EntrantAdapter.EntrantClickListener {

    private RecyclerView recyclerViewEntrants;
    private EntrantAdapter entrantAdapter;
    private List<Entrant> entrantList;
    private List<Entrant> filteredEntrantList;
    private FirebaseFirestore db;
    private SearchView searchView;
    private TextView noEntrantsText;
    private DBManagerEvent dbManagerEvent;

    /**
     * Called when the activity is first created.
     * This method sets up the layout, initializes the RecyclerView, and loads entrants from the database.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_event_entrants);

        recyclerViewEntrants = findViewById(R.id.ev_entrants_lv);
        noEntrantsText = findViewById(R.id.admin_no_entrants_text);
        db = FirebaseFirestore.getInstance();
        entrantList = new ArrayList<>();
        entrantAdapter = new EntrantAdapter(entrantList, this);
        //searchView = findViewById(R.id.admin_pro_search_view);

        dbManagerEvent = new DBManagerEvent();

        recyclerViewEntrants.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEntrants.setAdapter(entrantAdapter);

        setupSearchView();
        loadEntrants();
    }

    /**
     * Initializes the search functionality for filtering the entrant list.
     * This method sets up a {@link SearchView} to listen for query text input and applies a filter
     * to the {@link EntrantAdapter} to dynamically update the displayed list based on the entered text.
     *
     * @see SearchView
     * @see EntrantAdapter#getFilter()
     */
    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.admin_pro_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                entrantAdapter.getFilter().filter(query);  // Apply filter on submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                entrantAdapter.getFilter().filter(newText);  // Apply filter on text change
                return false;
            }
        });
    }

    /**
     * Loads entrants from the database and updates the entrant list.
     * Retrieves entrants from the "entrants" collection in Firestore and adds them to the entrant list.
     */
    private void loadEntrants() {
        CollectionReference entrantsRef = db.collection("entrants");
        /**
         * Called when the task to retrieve entrants is complete.
         *
         * @param task the task to retrieve entrants
         */
        entrantsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Entrant> entrants = task.getResult().toObjects(Entrant.class);
                    entrantList.clear();
                    entrantList.addAll(entrants);
                    filteredEntrantList = new ArrayList<>(entrants);  // Reset filtered list with fresh data
                    if (entrantList.isEmpty()) {
                        noEntrantsText.setVisibility(View.VISIBLE);
                        recyclerViewEntrants.setVisibility(View.GONE);
                    } else {
                        noEntrantsText.setVisibility(View.GONE);
                        recyclerViewEntrants.setVisibility(View.VISIBLE);
                    }
                    entrantAdapter.updateData(entrants);
                    entrantAdapter.notifyDataSetChanged();  // Notify adapter that the data has changed
                } else {
                    Toast.makeText(AdminViewEditProfilesActivity.this, "Error getting entrants!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Called when an entrant is clicked.
     * Starts the AdminViewProfilesContent activity and passes the entrant details to it.
     *
     * @param entrant the clicked entrant
     * @see AdminViewProfilesContent
     */
    @Override
    public void onEntrantClick(Entrant entrant) {
        Intent intent = new Intent(this, AdminViewProfilesContent.class);
        intent.putExtra("entrantName", entrant.getName());
        intent.putExtra("entrantId", entrant.getId());
        intent.putExtra("entrantEmail", entrant.getEmail());
        intent.putExtra("entrantPhone", entrant.getPhone());
        intent.putExtra("entrantPhoto", entrant.getImage_url());
        startActivity(intent);
    }
}

