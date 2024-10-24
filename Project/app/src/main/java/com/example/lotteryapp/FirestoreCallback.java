package com.example.lotteryapp;

public interface FirestoreCallback {
    void onEntrantExists(Entrant entrant);
    void onEntrantNotFound();
    void onError(Exception e);
}
