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

public class AdminViewEditEventsActivity extends AppCompatActivity implements AdminEventAdapter.OnEventClickListener{

    private RecyclerView adminEvList;
    private List<Event> eventList;
    private AdminEventAdapter adapter;
    private FirebaseFirestore db;

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
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query); return false;
            }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private void loadEvents() {
        CollectionReference eventsRef = db.collection("events");
        eventsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
