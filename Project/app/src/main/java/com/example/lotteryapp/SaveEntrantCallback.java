package com.example.lotteryapp;

public interface SaveEntrantCallback {
    void onSuccess();
    void onFailure(Exception e);
}
