package com.example.lotteryapp;

import java.io.Serializable;

/**
 * The {@code User} class serves as an abstract representation of a system user.
 * It provides common properties and methods for managing user information,
 * and it requires subclasses to implement specific behavior through the {@code displayUserInfo()} method.
 * This class implements {@code Serializable} to allow user objects to be serialized.
 *
 * @see Serializable
 */
public abstract class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String image_url;

    /**
     * Default constructor required for Firestore and serialization purposes.
     */
    public User() {
    }

    /**
     * Constructs a {@code User} with the specified ID, name, email, and phone number.
     *
     * @param id    the unique identifier of the user
     * @param name  the name of the user
     * @param email the email address of the user
     * @param phone the phone number of the user
     */
    public User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Constructs a {@code User} with the specified ID, name, and email.
     * This constructor is used when a phone number is not provided.
     *
     * @param id    the unique identifier of the user
     * @param name  the name of the user
     * @param email the email address of the user
     */
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    /**
     * Gets the unique identifier of the user.
     *
     * @return the user's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the user.
     *
     * @param id the new ID of the user
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the user.
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     *
     * @param name the new name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the user.
     *
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email the new email address of the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the phone number of the user.
     *
     * @return the user's phone number, or {@code null} if not set
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the user.
     *
     * @param phone the new phone number of the user
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Displays user information. This method must be implemented by subclasses
     * to define specific behavior for displaying user details.
     *
     * @throws UnsupportedOperationException if not overridden by a subclass
     */
    public abstract void displayUserInfo();
}