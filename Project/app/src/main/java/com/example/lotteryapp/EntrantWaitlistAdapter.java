package com.example.lotteryapp;
import android.widget.CheckBox;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // To load images
import com.example.lotteryapp.Entrant;

import java.util.ArrayList;
import java.util.List;

public class EntrantWaitlistAdapter extends RecyclerView.Adapter<EntrantWaitlistAdapter.EntrantViewHolder> {

    private Context context;
    private List<Entrant> entrantList;
    private final List<Entrant> selectedEntrants = new ArrayList<>();

    public EntrantWaitlistAdapter(Context context, List<Entrant> entrantList) {
        this.context = context;
        this.entrantList = entrantList;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.entrant_item, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EntrantViewHolder holder, int position) {
        Entrant entrant = entrantList.get(position);
        holder.nameTextView.setText(entrant.getName());
        Glide.with(context).load(entrant.getImage_url()).into(holder.photoImageView);
        holder.checkBoxEntrant.setChecked(entrant.isSelected());

        // Handle checkbox toggle
        holder.checkBoxEntrant.setOnCheckedChangeListener((buttonView, isChecked) -> {
            entrant.setSelected(isChecked);
            notifyDataSetChanged();
        });
    }

    // Add a method to get the selected entrants
    public List<Entrant> getSelectedEntrants() {
        return new ArrayList<>(selectedEntrants); // Return a copy of the selected entrants list
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        ImageView photoImageView;
        CheckBox checkBoxEntrant;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.entrant_name);
            photoImageView = itemView.findViewById(R.id.entrant_photo);
            checkBoxEntrant = itemView.findViewById(R.id.checkBoxEntrant);

        }
    }
}
