package com.example.lotteryapp;

import android.graphics.Bitmap;
import android.graphics.Color;

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
    private List<String> waitingList;
    private String image_url;
    private Integer waitlistCap;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Event() {
        // Empty constructor for Firebase
    }

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
        this.waitingList = new ArrayList<>();  // Initialize waitingList as an empty list
    }

    public List<String> getSelectedEntrants() {
        return selectedEntrants;
    }

    public void setSelectedEntrants(List<String> selectedEntrants) {
        this.selectedEntrants = selectedEntrants;
    }

    public List<String> getCancelledEntrants() {
        return cancelledEntrants;
    }

    public void setCancelledEntrants(List<String> cancelledEntrants) {
        this.cancelledEntrants = cancelledEntrants;
    }

    public String getEventName() {
        return this.eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getNumPeople() {
        return this.numPeople;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQR_code(){
        return this.qr_code;
    }

    public void setQR_code(String qrHash){
        this.qr_code = qrHash;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Boolean getGeolocationRequired() {
        return geolocationRequired;
    }

    public void setGeolocationRequired(Boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    // Get the waiting list for this event
    public List<String> getWaitingList() {
        return this.waitingList;
    }

    // Add an entrant to the waiting list
    public boolean addEntrantToWaitingList(String entrantId) {
        if (!waitingList.contains(entrantId)) {
            waitingList.add(entrantId);
            return true;
        }
        return false;  // Entrant is already in the waiting list
    }

    // Remove an entrant from the waiting list
    public boolean removeEntrantFromWaitingList(String entrantId) {
        return waitingList.remove(entrantId);
    }

    // Check if the waiting list is full (assuming some arbitrary limit for the waiting list)
    public boolean isWaitingListFull() {
        return waitingList.size() >= numPeople; // For example, using numPeople as capacity limit
    }

    /**
     * Generates a bitmap image of the QR code.
     *
     * @return the QR code bitmap
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

    public String getEventStartDate() {
        return eventStartDate;
    }

    public void setEventStartDate(String eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public String getEventEndDate() {
        return eventEndDate;
    }

    public void setEventEndDate(String eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Integer getWaitingListLength(){
        return this.getWaitingList().size();
    }

    public Float getEventPrice() {
        return eventPrice;
    }

    public void setEventPrice(Float eventPrice) {
        this.eventPrice = eventPrice;
    }

    public Integer getWaitlistCap() {
        return waitlistCap;
    }

    public void setWaitlistCap(Integer waitlistCap) {
        this.waitlistCap = waitlistCap;
    }
}
