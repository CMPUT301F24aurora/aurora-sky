package com.example.lotteryapp;

public interface SaveEventCallback {
    void onSuccess();
    void onFailure(Exception e);
}
