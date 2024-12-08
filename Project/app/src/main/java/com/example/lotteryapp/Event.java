package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.firebase.firestore.CollectionReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Event} class represents an event with details such as the event name, date,
 * geolocation requirement, number of attendees, description, waiting list, and a generated QR code.
 * It supports saving to and retrieving from Firebase Firestore.
 *
 * @version v1
 * @see FirebaseFirestore
 */

public class Event implements Serializable {

    private String posterImage;
    private String eventName;
    private String eventStartDate;
    private String eventEndDate;
    private String registrationDeadline;
    private Boolean geolocationRequired;
    private Integer numPeople;
    private Float eventPrice;
    private String description;
    private String qr_code;
    private List<String> selectedEntrants;
    private List<String> cancelledEntrants;
    private List<String> finalEntrants;
    private List<String> waitingList;
    private String image_url;
    private FirebaseFirestore db;
    private Integer waitlistCap;


    /**
     * Default constructor for Firebase.
     */
    public Event() {
        // Initialize Firestore
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Constructs an Event with specified details.
     *
     * @param eventName The name of the event.
     * @param numPeople The number of people allowed in the event.
     * @param description The description of the event.
     * @param geolocationRequired Whether geolocation is required for the event.
     * @param registrationDeadline The deadline for registration.
     * @param eventStartDate The start date of the event.
     * @param eventEndDate The end date of the event.
     * @param eventPrice The price of the event.
     */
    public Event(String eventName, Integer numPeople, String description, Boolean geolocationRequired, String registrationDeadline, String eventStartDate, String eventEndDate, Float eventPrice) {
        this.eventName = eventName;
        this.numPeople = numPeople;
        this.description = description;
        this.geolocationRequired = geolocationRequired;
        this.registrationDeadline = registrationDeadline;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
        this.eventPrice = eventPrice;
        this.qr_code = generateQRHash();
        this.waitingList = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Gets the list of selected entrants.
     *
     * @return The list of selected entrants.
     */
    public List<String> getSelectedEntrants() {
        return selectedEntrants;
    }

    /**
     * Sets the list of selected entrants.
     *
     * @param selectedEntrants The list of selected entrants to set.
     */
    public void setSelectedEntrants(List<String> selectedEntrants) {
        this.selectedEntrants = selectedEntrants;
    }

    /**
     * Gets the list of cancelled entrants.
     *
     * @return The list of cancelled entrants.
     */
    public List<String> getCancelledEntrants() {
        return cancelledEntrants;
    }

    /**
     * Sets the list of cancelled entrants.
     *
     * @param cancelledEntrants The list of cancelled entrants to set.
     */
    public void setCancelledEntrants(List<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;
    }

    /**
     * Gets the event name.
     *
     * @return The event name.
     */
    public String getEventName() {
        return this.eventName;
    }

    /**
     * Sets the event name.
     *
     * @param eventName The event name to set.
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Gets the number of people allowed in the event.
     *
     * @return The number of people.
     */
    public int getNumPeople() {
        return this.numPeople;
    }

    /**
     * Sets the number of people allowed in the event.
     *
     * @param numPeople The number of people to set.
     */
    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    /**
     * Gets the event description.
     *
     * @return The event description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the event description.
     *
     * @param description The event description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the QR code for the event.
     *
     * @return The QR code.
     */
    public String getQR_code(){
        return this.qr_code;
    }

    /**
     * Sets the QR code for the event.
     *
     * @param qrHash The QR code hash to set.
     */
    public void setQR_code(String qrHash){
        this.qr_code = qrHash;
    }

    /**
     * Gets the image URL for the event.
     *
     * @return The image URL.
     */
    public String getImage_url() {
        return image_url;
    }

    /**
     * Sets the image URL for the event.
     *
     * @param image_url The image URL to set.
     */
    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    /**
     * Checks if geolocation is required for the event.
     *
     * @return True if geolocation is required, false otherwise.
     */
    public Boolean getGeolocationRequired() {
        return geolocationRequired;
    }

    /**
     * Sets whether geolocation is required for the event.
     *
     * @param geolocationRequired True if geolocation is required, false otherwise.
     */
    public void setGeolocationRequired(Boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    /**
     * Gets the waiting list for this event.
     *
     * @return The list of entrants in the waiting list.
     */
    public List<String> getWaitingList() {
        return this.waitingList;
    }

    /**
     * Adds an entrant to the waiting list.
     *
     * @param entrantId The ID of the entrant to add.
     * @return True if the entrant was added successfully, false if already in the list.
     */
    public boolean addEntrantToWaitingList(String entrantId) {
        if (!waitingList.contains(entrantId)) {
            waitingList.add(entrantId);
            return true;
        }
        return false;  // Entrant is already in the waiting list
    }

    /**
     * Removes an entrant from the waiting list.
     *
     * @param entrantId The ID of the entrant to remove.
     * @return True if the entrant was removed successfully, false if not found.
     */
    public boolean removeEntrantFromWaitingList(String entrantId) {
        return waitingList.remove(entrantId);
    }

    /**
     * Checks if the waiting list is full.
     *
     * @return True if the waiting list is full, false otherwise.
     */
    public boolean isWaitingListFull() {
        return waitingList.size() >= numPeople; // For example, using numPeople as capacity limit
    }

    /**
     * Generates a hash for the QR code.
     *
     * @return The generated QR code hash.
     */
    public String generateQRHash() {
        String uniqueIdentifier = eventName + eventStartDate + eventEndDate + System.currentTimeMillis();
        String firebaseUrl = "https://yourfirebaseproject.firebaseio.com/events/" + uniqueIdentifier;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = writer.encode(firebaseUrl, BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder qrData = new StringBuilder();
        for (int y = 0; y < bitMatrix.getHeight(); y++) {
            for (int x = 0; x < bitMatrix.getWidth(); x++) {
                qrData.append(bitMatrix.get(x, y) ? "1" : "0");
            }
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(qrData.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a bitmap image of the QR code.
     *
     * @return The QR code bitmap.
     */
    public Bitmap generateQRCodeBitmap() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qr_code, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the event start date.
     *
     * @return The event start date.
     */
    public String getEventStartDate() {
        return eventStartDate;
    }

    /**
     * Sets the event start date.
     *
     * @param eventStartDate The event start date to set.
     */
    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    /**
     * Gets the event end date.
     *
     * @return The event end date.
     */
    public String getEventEndDate() {
        return eventEndDate;
    }

    /**
     * Sets the event end date.
     *
     * @param eventEndDate The event end date to set.
     */
    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    /**
     * Gets the registration deadline.
     *
     * @return The registration deadline.
     */
    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    /**
     * Sets the registration deadline.
     *
     * @param registrationDeadline The registration deadline to set.
     */
    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    /**
     * Gets the length of the waiting list.
     *
     * @return The number of entrants in the waiting list.
     */
    public Integer getWaitingListLength(){
        return this.getWaitingList().size();
    }

    /**
     * Gets the event price.
     *
     * @return The event price.
     */
    public Float getEventPrice() {
        return eventPrice;
    }

    /**
     * Sets the event price.
     *
     * @param eventPrice The event price to set.
     */
    public void setEventPrice(Float eventPrice) {
        this.eventPrice = eventPrice;
    }

    /**
     * Gets the waitlist cap.
     *
     * @return The waitlist cap.
     */
    public Integer getWaitlistCap() {
        return waitlistCap;
    }

    /**
     * Sets the waitlist cap.
     *
     * @param waitlistCap The waitlist cap to set.
     */
    public void setWaitlistCap(Integer waitlistCap) {
        this.waitlistCap = waitlistCap;
    }

    /**
     * Gets the list of final entrants.
     *
     * @return The list of final entrants.
     */
    public List<String> getFinalEntrants() {
        return finalEntrants;
    }

    /**
     * Sets the list of final entrants.
     *
     * @param finalEntrants The list of final entrants to set.
     */
    public void setFinalEntrants(List<String> finalEntrants) {
        this.finalEntrants = finalEntrants;
    }
}