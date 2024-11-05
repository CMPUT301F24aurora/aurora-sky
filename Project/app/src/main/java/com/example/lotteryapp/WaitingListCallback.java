package com.example.lotteryapp;

public interface WaitingListCallback {
    void onSuccess(String message);
    void onFailure(Exception e);
}
