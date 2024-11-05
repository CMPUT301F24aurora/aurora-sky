package com.example.lotteryapp;

public interface SaveEventCallback {
    void onSuccess(String documentId);
    void onFailure(Exception e);
}
