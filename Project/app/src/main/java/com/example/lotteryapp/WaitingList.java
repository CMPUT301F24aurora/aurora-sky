package com.example.lotteryapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaitingList {
    private List<Entrant> waitingList;
    private Optional<Integer> capacity; // Optional maximum number of entrants allowed

    // Constructor for unlimited capacity
    public WaitingList() {
        this.waitingList = new ArrayList<>();
        this.capacity = Optional.empty();
    }

    // Constructor with optional capacity limit
    public WaitingList(int capacity) {
        this.waitingList = new ArrayList<>();
        this.capacity = capacity > 0 ? Optional.of(capacity) : Optional.empty();
    }

    // Method to add an entrant to the waiting list
    public boolean addEntrant(Entrant entrant) {
        if (capacity.isPresent() && waitingList.size() >= capacity.get()) {
            return false; // Waiting list is full
        }
        return waitingList.add(entrant);
    }

    // Method to remove an entrant from the waiting list
    public boolean removeEntrant(Entrant entrant) {
        return waitingList.remove(entrant);
    }

    // Get the current list of entrants
    public List<Entrant> getWaitingList() {
        return new ArrayList<>(waitingList); // Return a copy to prevent external modifications
    }

    // Get the capacity of the waiting list
    public Optional<Integer> getCapacity() {
        return capacity;
    }

    // Get the current size of the waiting list
    public int size() {
        return waitingList.size();
    }

    // Check if the waiting list is full
    public boolean isFull() {
        return capacity.isPresent() && waitingList.size() >= capacity.get();
    }
}