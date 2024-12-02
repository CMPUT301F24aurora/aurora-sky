package com.example.lotteryapp;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents an entrant user in the lottery application.
 * Extends the User class and implements Serializable for data storage and retrieval.
 */
public class Entrant extends User implements Serializable {
    private String image_url;
    private String name;
    private List<String> selected_event;
    private Boolean notificationAllowed;
    private boolean isSelected;

    /**
     * Default constructor required for Firestore.
     */
    public Entrant() {}

    /**
     * Constructs an Entrant with a given name.
     *
     * @param name The name of the entrant.
     */
    public Entrant(String name) {
        super();
        this.setName(name);
    }

    /**
     * Constructs an Entrant with id, name, and email.
     *
     * @param id The ID of the entrant.
     * @param name The name of the entrant.
     * @param email The email of the entrant.
     */
    public Entrant(String id, String name, String email) {
        super(id, name, email);
    }

    /**
     * Constructs an Entrant with id, name, email, and phone.
     *
     * @param id The ID of the entrant.
     * @param name The name of the entrant.
     * @param email The email of the entrant.
     * @param phone The phone number of the entrant.
     */
    public Entrant(String id, String name, String email, String phone) {
        super(id, name, email, phone);
    }

    /**
     * Gets the image URL of the entrant.
     *
     * @return The image URL.
     */
    public String getImage_url() {
        return image_url;
    }

    /**
     * Sets the image URL of the entrant.
     *
     * @param image_url The image URL to set.
     */
    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    /**
     * Displays the entrant's information.
     */
    @Override
    public void displayUserInfo() {
        System.out.println("Entrant ID: " + getId());
        System.out.println("Entrant Name: " + getName());
        System.out.println("Entrant Email: " + getEmail());
    }

    /**
     * Checks if this Entrant is equal to another object.
     *
     * @param o The object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrant)) return false;
        Entrant entrant = (Entrant) o;
        return Objects.equals(getId(), entrant.getId());
    }

    /**
     * Returns a string representation of the Entrant.
     *
     * @return The name of the entrant.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Generates a hash code for the Entrant.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Gets the list of selected events for the entrant.
     *
     * @return The list of selected events.
     */
    public List<String> getSelected_event() {
        return selected_event;
    }

    /**
     * Sets the list of selected events for the entrant.
     *
     * @param selected_event The list of selected events to set.
     */
    public void setSelected_event(List<String> selected_event) {
        this.selected_event = selected_event;
    }

    /**
     * Checks if notifications are allowed for the entrant.
     *
     * @return true if notifications are allowed, false otherwise.
     */
    public Boolean getNotificationAllowed() {
        return notificationAllowed;
    }

    /**
     * Sets whether notifications are allowed for the entrant.
     *
     * @param notificationAllowed The notification permission to set.
     */
    public void setNotificationAllowed(Boolean notificationAllowed) {
        this.notificationAllowed = notificationAllowed;
    }

    /**
     * Checks if the entrant is selected.
     *
     * @return true if the entrant is selected, false otherwise.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Sets the selected status of the entrant.
     *
     * @param selected The selected status to set.
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}