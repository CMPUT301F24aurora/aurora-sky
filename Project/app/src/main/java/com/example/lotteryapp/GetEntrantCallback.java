package com.example.lotteryapp;

public interface GetEntrantCallback {
    void onEntrantFound(Entrant entrant);
    void onEntrantNotFound(Exception e);
    void onError(Exception e);

}
