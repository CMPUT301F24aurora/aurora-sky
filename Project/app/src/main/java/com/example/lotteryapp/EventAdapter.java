package com.example.lotteryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.Filter;
import android.widget.Filterable;
import java.util.ArrayList;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private List<Event> filteredList;      // Filtered list
    private final OnEventClickListener eventClickListener; // Add this

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.filteredList = new ArrayList<>(eventList);  // Initialize with the full list
        this.eventClickListener = listener; // Initialize the listener
    }

    /**
     * Creates a new {@code EventViewHolder} by inflating the event item layout.
     *
     * @param parent    the parent view into which the new view will be added
     * @param viewType  the view type of the new view
     * @return          a new {@code EventViewHolder} that holds the view for each event item
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds data to the {@code EventViewHolder} at the specified position.
     *
     * @param holder    the {@code EventViewHolder} which should be updated to represent the contents of the item
     * @param position  the position of the item within the data set
     */

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = filteredList.get(position);  // Use the filtered list
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getEventStartDate());
        holder.eventDescription.setText(event.getDescription());

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> eventClickListener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();  // Return size of filtered list
    }

    public void updateData(List<Event> newEventList) {
        this.eventList.clear();
        this.eventList.addAll(newEventList);
        this.filteredList = new ArrayList<>(newEventList);  // Reset filtered list
        notifyDataSetChanged();  // Notify adapter that the data has changed
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString().toLowerCase().trim();
                FilterResults results = new FilterResults();

                if (query.isEmpty()) {
                    results.values = eventList;
                } else {
                    List<Event> filtered = new ArrayList<>();
                    for (Event event : eventList) {
                        if (event.getEventName().toLowerCase().contains(query)) {
                            filtered.add(event);
                        }
                    }
                    results.values = filtered;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (List<Event>) results.values;
                notifyDataSetChanged();  // Refresh the RecyclerView
            }
        };
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
