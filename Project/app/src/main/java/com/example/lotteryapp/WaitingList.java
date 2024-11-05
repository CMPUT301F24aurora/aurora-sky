package com.example.lotteryapp;

import java.util.ArrayList;
import java.util.List;

public class WaitingList {
    private List<Entrant> waitingList;
    private int capacity; // Maximum number of entrants allowed in the waiting list

    public WaitingList(int capacity) {
        this.waitingList = new ArrayList<>();
        this.capacity = capacity;
    }

    // Method to add an entrant to the waiting list
    public boolean addEntrant(Entrant entrant) {
        if (waitingList.size() < capacity) {
            waitingList.add(entrant);
            return true; // Successfully added
        }
        return false; // Waiting list is full
    }

    // Method to remove an entrant from the waiting list
    public boolean removeEntrant(Entrant entrant) {
        return waitingList.remove(entrant); // Returns true if the entrant was found and removed
    }

    // Get the current list of entrants
    public List<Entrant> getWaitingList() {
        return waitingList;
    }

    // Get the capacity of the waiting list
    public int getCapacity() {
        return capacity;
    }
}
