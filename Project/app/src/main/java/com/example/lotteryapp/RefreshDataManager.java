package com.example.lotteryapp;

import android.content.Context;

/**
 * The {@code RefreshDataManager} class is responsible for managing the refreshing of data related to
 * {@code Entrant} and {@code Organizer} objects from a database. It follows a singleton pattern to ensure
 * a single instance across the application.
 *
 * @see DatabaseManager
 * @see Entrant
 * @see Organizer
 */
public class RefreshDataManager {
    private static RefreshDataManager instance;
    private Context context;

    /**
     * Constructs a new {@code RefreshDataManager} with the specified application context.
     *
     * @param context The {@code Context} of the application, used to access resources and database managers.
     */
    public RefreshDataManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Retrieves the singleton instance of {@code RefreshDataManager}.
     *
     * @param context The {@code Context} of the application.
     * @return The singleton instance of {@code RefreshDataManager}.
     */
    public static synchronized RefreshDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new RefreshDataManager(context);
        }
        return instance;
    }

    /**
     * Interface for listening to data refresh events.
     */
    public interface DataRefreshListener {
        /**
         * Called when data refresh is successful.
         *
         * @param entrant   The refreshed {@code Entrant} object, or {@code null} if not found.
         * @param organizer The refreshed {@code Organizer} object, or {@code null} if not found.
         */
        void onDataRefreshed(Entrant entrant, Organizer organizer);

        /**
         * Called when an error occurs during data refresh.
         *
         * @param e The {@code Exception} detailing the error.
         */
        void onError(Exception e);
    }

    /**
     * Initiates the data refresh process for a given entrant and organizer.
     *
     * @param id       The unique identifier for the organizer's device.
     * @param listener A {@code DataRefreshListener} to handle callbacks for success or error events.
     * @throws IllegalArgumentException If the {@code id} is {@code null} or empty.
     * @see DatabaseManager
     */
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

    /**
     * Retrieves and refreshes the organizer's data after successfully fetching the entrant.
     *
     * @param entrant  The {@code Entrant} object that was fetched.
     * @param id       The unique identifier for the organizer's device.
     * @param listener A {@code DataRefreshListener} to handle callbacks for success or error events.
     * @see DatabaseManager
     */
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