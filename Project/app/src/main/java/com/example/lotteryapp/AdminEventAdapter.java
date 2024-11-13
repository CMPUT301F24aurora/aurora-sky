package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying events in a RecyclerView.
 * This adapter is used by admin users to view and interact with event data.
 *
 * @see RecyclerView.Adapter
 * @see Event
 * @see OnEventClickListener
 * @version v1
 *
 * @author Team Aurora
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {
    private final List<Event> eventList;
    private List<Event> filteredEventList;
    private final AdminEventAdapter.OnEventClickListener eventClickListener;

    /**
     * Interface for handling event click actions.
     */
    public interface OnEventClickListener {
        /**
         * Called when an event is clicked.
         *
         * @param event the clicked event
         */
        void onEventClick(Event event);
    }

    /**
     * Constructor for the AdminEventAdapter class.
     *
     * @param eventList the list of events to display
     * @param listener the listener for handling event click actions
     */
    public AdminEventAdapter(List<Event> eventList, AdminEventAdapter.OnEventClickListener listener) {
        this.eventList = eventList;
        this.filteredEventList = new ArrayList<>(eventList);
        this.eventClickListener = listener;
    }

    /**
     * Creates a new ViewHolder for an event item.
     *
     * @param parent the parent view group
     * @param viewType the view type of the new view
     * @return a new EventViewHolder
     * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
     */
    @NonNull
    @Override
    public AdminEventAdapter.EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        return new AdminEventAdapter.EventViewHolder(view);
    }

    /**
     * Binds data to an EventViewHolder.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the data set
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     */
    @Override
    public void onBindViewHolder(@NonNull AdminEventAdapter.EventViewHolder holder, int position) {
        Event event = filteredEventList.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getEventDate());
        holder.eventDescription.setText(event.getDescription());

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> eventClickListener.onEventClick(event));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return the total number of items
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return filteredEventList.size();
    }

    /**
     * Filters the event list based on the provided query.
     * Updates the filtered event list and notifies the adapter of the changes.
     *
     * @param query the query to filter the event list by
     */
    public void filter(String query) {
        filteredEventList.clear();
        if (query.isEmpty()) {
            filteredEventList.addAll(eventList);
        } else {
            for (Event event : eventList) {
                if (event.getEventName().toLowerCase().contains(query.toLowerCase())) {
                    filteredEventList.add(event);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for an event item.
     */
    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, eventDescription;

        /**
         * Constructor for the EventViewHolder class.
         *
         * @param itemView the view of the event item
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventDescription = itemView.findViewById(R.id.event_description);
        }
    }
}
