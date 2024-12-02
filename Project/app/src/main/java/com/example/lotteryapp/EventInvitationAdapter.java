package com.example.lotteryapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventInvitationAdapter extends RecyclerView.Adapter<EventInvitationAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventClickListener listener;

    // Constructor
    public EventInvitationAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    // onCreateViewHolder: Create a new ViewHolder instance
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_event layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    // onBindViewHolder: Bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        // Get the event for the current position
        Event event = eventList.get(position);
        Log.d("eventinvitationadapter",""+event);

        // Set the event data into the ViewHolder's views
        holder.eventNameTextView.setText(event.getEventName());
        holder.eventDateTextView.setText(event.getEventStartDate());

        // Set item click listener for each event
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event); // Trigger click listener
            }
        });
    }

    // getItemCount: Return the number of items in the event list
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class to hold the event item views
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventDateTextView;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name);
            eventDateTextView = itemView.findViewById(R.id.event_date);
        }
    }

    // Interface for handling item clicks
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    // Method to update the event data
    public void updateData(List<Event> newEventList) {
        eventList.clear();
        eventList.addAll(newEventList);
        notifyDataSetChanged(); // Notify adapter that data has changed
    }
}
