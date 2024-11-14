package com.example.lotteryapp;

import android.content.Context;

public class RefreshDataManager {
    private static RefreshDataManager instance;
    private Context context;

    public RefreshDataManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized RefreshDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new RefreshDataManager(context);
        }
        return instance;
    }

    public interface DataRefreshListener {
        void onDataRefreshed(Entrant entrant, Organizer organizer);
        void onError(Exception e);
    }

    public void refreshData(String id, DataRefreshListener listener) {
        DatabaseManager.getEntrant(context, new GetEntrantCallback() {
            @Override
            public void onEntrantFound(Entrant entrant) {
                refreshOrganizer(entrant, id, listener);
            }

            @Override
            public void onEntrantNotFound(Exception e) {
                listener.onDataRefreshed(null, null);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    private void refreshOrganizer(Entrant entrant, String id, DataRefreshListener listener) {
        DatabaseManager.getOrganizerByDeviceId(id, new GetOrganizerCallback() {
            @Override
            public void onOrganizerFound(Organizer organizer) {
                listener.onDataRefreshed(entrant, organizer);
            }

            @Override
            public void onOrganizerNotFound() {
                listener.onDataRefreshed(entrant, null);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }
}