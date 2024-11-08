package com.example.lotteryapp;

import com.example.lotteryapp.Organizer;

// Callback interface for getting Organizer
public interface GetOrganizerCallback {
    void onOrganizerFound(Organizer organizer);
    void onOrganizerNotFound();
    void onError(Exception e);
}