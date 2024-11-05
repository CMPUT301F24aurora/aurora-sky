package com.example.lotteryapp;

import java.util.List;

public interface GetEventsCallback {
    void onSuccess(List<Event> events);
    void onFailure(Exception e);
}
