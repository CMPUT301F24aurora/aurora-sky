package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrgEventAdapter extends RecyclerView.Adapter<OrgEventAdapter.EventViewHolder> {
    private final OnEventClickListener eventClickListener;
    private final List<Event> eventList;

    // Interface for handling clicks
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }


    public OrgEventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventClickListener = listener;
        this.eventList = eventList;
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
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getEventDate());
        holder.eventDescription.setText(event.getDescription());

        holder.itemView.setOnClickListener(v -> eventClickListener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, eventDescription;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventDescription = itemView.findViewById(R.id.event_description);
        }
    }
}
