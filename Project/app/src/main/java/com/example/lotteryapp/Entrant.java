package com.example.lotteryapp;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Entrant class represents an entrant user in the application.
 * This class extends the User class and implements Serializable for data storage and retrieval.
 *
 * @see User
 * @see Serializable
 * @version v1
 *
 * Author: Team Aurora
 */
public class Entrant extends User implements Serializable {

    /**
     * Default constructor required for Firestore.
     */
    public Entrant() {}

    /**
     * Constructor with three parameters.
     *
     * @param id the ID of the entrant
     * @param name the name of the entrant
     * @param email the email of the entrant
     */
    public Entrant(String id, String name, String email) {
        super(id, name, email);
    }

    /**
     * Constructor with four parameters.
     *
     * @param id the ID of the entrant
     * @param name the name of the entrant
     * @param email the email of the entrant
     * @param phone the phone number of the entrant
     */
    public Entrant(String id, String name, String email, String phone) {
        super(id, name, email, phone);
    }

    /**
     * Displays the user's information.
     * This method is overridden from the User class.
     */
    @Override
    public void displayUserInfo() {
        System.out.println("Entrant ID: " + getId());
        System.out.println("Entrant Name: " + getName());
        System.out.println("Entrant Email: " + getEmail());
    }

    /**
     * Checks if this Entrant is equal to another object.
     * Equality is based on the ID of the Entrant.
     *
     * @param o the object to compare to
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entrant)) return false;
        Entrant entrant = (Entrant) o;
        return Objects.equals(getId(), entrant.getId());
    }

    /**
     * Returns the hash code of this Entrant.
     * The hash code is based on the ID of the Entrant.
     *
     * @return the hash code of this Entrant
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}