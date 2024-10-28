package com.example.lotteryapp;

public interface EntrantCheckCallback {
    void onEntrantExists(Entrant entrant);
    void onEntrantNotFound();
    void onError(Exception e);
}
