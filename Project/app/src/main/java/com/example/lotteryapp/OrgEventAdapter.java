package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter for displaying a list of events in a {@link RecyclerView}.
 * <p>
 * This adapter is used to bind a list of {@link Event} objects to the view items in a RecyclerView.
 * Each item displays the event name, start date, and description.
 * The adapter also handles click events on the items using the {@link OnEventClickListener}.
 * </p>
 *
 * @see RecyclerView.Adapter
 * @see Event
 */
public class OrgEventAdapter extends RecyclerView.Adapter<OrgEventAdapter.EventViewHolder> {
    private final OnEventClickListener eventClickListener;
    private final List<Event> eventList;

    /**
     * Interface definition for a callback to be invoked when an event item is clicked.
     * This interface should be implemented by classes that wish to handle click events on event items.
     */
    // Interface for handling clicks
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /**
     * Constructs an {@link OrgEventAdapter} with the given event list and click listener.
     *
     * @param eventList The list of events to display in the RecyclerView.
     *                  This list cannot be null.
     * @param listener The listener for handling event item clicks.
     *                 This listener cannot be null.
     * @throws IllegalArgumentException if eventList or listener is null.
     */
    public OrgEventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventClickListener = listener;
        this.eventList = eventList;
    }

    /**
     * Creates a new {@link EventViewHolder} to represent an event item view.
     *
     * @param parent The parent view group that will contain the created view.
     * @param viewType The view type of the new view. This is used if multiple view types are supported.
     * @return A new {@link EventViewHolder} to hold the event item view.
     * @see EventViewHolder
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds the data of an {@link Event} object to the view holder.
     * This method sets the event name, start date, and description in the corresponding TextViews.
     * It also sets up the click listener for the item view.
     *
     * @param holder The {@link EventViewHolder} to bind data to.
     * @param position The position of the event item within the adapter.
     * @return void
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getEventStartDate());
        holder.eventDescription.setText(event.getDescription());

        holder.itemView.setOnClickListener(v -> eventClickListener.onEventClick(event));
    }

    /**
     * Returns the total number of event items in the adapter.
     *
     * @return The number of events in the event list.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder for event items in the RecyclerView. Holds references to the event name,
     * date, and description TextViews.
     *
     * @see RecyclerView.ViewHolder
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDate, eventDescription;

        /**
         * Constructs a new {@link EventViewHolder}.
         *
         * @param itemView The view that represents the event item.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventDescription = itemView.findViewById(R.id.event_description);
        }
    }
}
