package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private List<Event> filteredEventList;
    private final OnEventClickListener eventClickListener; // Add this

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.filteredEventList = new ArrayList<>(eventList);
        this.eventClickListener = listener; // Initialize the listener
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getName());
        holder.eventDate.setText(event.getEventDate());
        holder.eventDescription.setText(event.getDescription());

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> eventClickListener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return filteredEventList.size();
    }

    public void filter(String query) {
        filteredEventList.clear();
        if (query.isEmpty()) {
            filteredEventList.addAll(eventList);
        }
        else {
            for (Event event : eventList) {
                if (event.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredEventList.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, eventDescription;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventDescription = itemView.findViewById(R.id.event_description);
        }
    }
}
