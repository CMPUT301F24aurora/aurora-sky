package com.example.lotteryapp;

public class Admin extends User{
    //private EntrantsOrganizer entrantsOrganizer;
    private Event eventsFacility;

    @Override
    public void displayUserInfo() {
        
    }


/*
        // Constructor
    public Admin() {
        //this.entrantsOrganizer = new EntrantsOrganizer();
        this.eventsFacility = new EventsFacility();
    }

        // Remove methods
        public void removeEvent(String eventQrHash) {
        eventsFacility.deleteEvent(eventQrHash);
    }

        public void removeProfile(String profileId) {
        entrantsOrganizer.deleteProfile(profileId);
    }

        public void removeImage(String imageId) {
        eventsFacility.deleteImage(imageId);
    }

        public void removeQRCode(String qrCodeHash) {
        eventsFacility.deleteQRCode(qrCodeHash);
    }

        // Browse methods
        public List<Event> browseEvents() {
        return eventsFacility.getAllEvents();
    }

        public List<Profile> browseProfiles() {
        return entrantsOrganizer.getAllProfiles();
    }

        public List<Image> browseImages() {
        return eventsFacility.getAllImages();
    }

        // Enforce policies
        public void enforcePolicy(String facilityId) {
        eventsFacility.removeFacilityIfViolatesGuidelines(facilityId);
    }

    @Override
    public void displayUserInfo() {
        System.out.println("Admin ID: " + getId());
        System.out.println("Admin Name: " + getName());
        System.out.println("Admin Email: " + getEmail());
    }
 */
}
