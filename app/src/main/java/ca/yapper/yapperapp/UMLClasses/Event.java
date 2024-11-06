package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.R;

public class Event {
    private int capacity;
    private String date_Time;
    private String description;
    private String facilityLocation;
    private String facilityName;
    private boolean isGeolocationEnabled;
    private String name;
    private qrCode QRCode;
    private String registrationDeadline;
    private int waitListCapacity;
    private ArrayList<String> cancelledList = null;
    private ArrayList<String> finalList = null;
    private ArrayList<String> selectedList = null;

    private ArrayList<String> waitingList = null;

    public Event(int capacity, String date_Time, String description, String facilityLocation, String facilityName, boolean isGeolocationEnabled, String name, String registrationDeadline, int waitListCapacity, ArrayList<String> cancelledList, ArrayList<String> finalList, ArrayList<String> selectedList, ArrayList<String> waitingList) throws WriterException {
        this.name = name;
        this.date_Time = date_Time;
        this.registrationDeadline = registrationDeadline;
        this.facilityName = facilityName;
        this.facilityLocation = facilityLocation;
        this.capacity = capacity;
        this.waitListCapacity = waitListCapacity;
        this.isGeolocationEnabled = isGeolocationEnabled;
        this.QRCode = new qrCode(this.name);
        this.cancelledList = cancelledList;
        this.finalList = cancelledList;
        this.selectedList = cancelledList;
        this.waitingList = cancelledList;
    }

    public qrCode getQRCode() {
        return this.QRCode;
    }

    public void setQRCode(qrCode QRCode) {
        this.QRCode = QRCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_Time() {
        return date_Time;
    }

    public void setDate_Time(String date_Time) {
        this.date_Time = date_Time;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityLocation() {
        return facilityLocation;
    }

    public void setFacilityLocation(String facilityLocation) {
        this.facilityLocation = facilityLocation;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWaitListCapacity() {
        return waitListCapacity;
    }

    public void setWaitListCapacity(int waitListCapacity) {
        this.waitListCapacity = waitListCapacity;
    }

    public boolean isGeolocationEnabled() {
        return isGeolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.isGeolocationEnabled = geolocationEnabled;
    }

    public ArrayList<String> cancelledList() {
        return cancelledList;
    }

    public void setCancelledList(ArrayList<String> cancelledList) {
        this.cancelledList = cancelledList;
    }

    public ArrayList<String> finalList() {
        return finalList;
    }

    public void setFinalList(ArrayList<String> finalList) {
        this.finalList = finalList;
    }

    public ArrayList<String> selectedList() {
        return selectedList;
    }

    public void setSelectedList(ArrayList<String> selectedList) {
        this.selectedList = selectedList;
    }

    public ArrayList<String> waitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<String> waitingList) {
        this.waitingList = waitingList;
    }

    public static Event loadEventFromDatabase(String hashData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            // Create basic event with empty lists first
            Event event = new Event(0, "", "", "", "", false, "", "", 0,
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

            // Load the data in background
            db.collection("Events").document(hashData).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Update event fields from document
                    event.setCapacity(documentSnapshot.getLong("capacity").intValue());
                    event.setDate_Time(documentSnapshot.getString("date_Time"));
                    event.setFacilityName(documentSnapshot.getString("facilityName"));
                    event.setFacilityLocation(documentSnapshot.getString("facilityLocation"));
                    event.setGeolocationEnabled(documentSnapshot.getBoolean("isGeolocationEnabled"));
                    event.setName(documentSnapshot.getString("name"));
                    event.setRegistrationDeadline(documentSnapshot.getString("regDeadline"));
                    event.setWaitListCapacity(documentSnapshot.getLong("waitListCapacity").intValue());

                    loadUserIdsFromSubcollection(db, hashData, "waitingList", event.waitingList);
                    loadUserIdsFromSubcollection(db, hashData, "selectedList", event.selectedList);
                    loadUserIdsFromSubcollection(db, hashData, "finalList", event.finalList);
                    loadUserIdsFromSubcollection(db, hashData, "cancelledList", event.cancelledList);
                }
            });

            return event;

        } catch (WriterException e) {
            Log.e("EventDebug", "Error creating event", e);
            return null;
        }
    }

    private static void loadUserIdsFromSubcollection(FirebaseFirestore db, String eventId, String subcollectionName, ArrayList<String> userIdsList) {
        db.collection("Events").document(eventId).collection(subcollectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Assuming each document in the subcollection is a reference to a User in the "Users" collection
                        String userIdRef = doc.getId(); // Get the document ID in the subcollection (reference to User ID)

                        // Retrieve the User document to get the deviceId
                        db.collection("Users").document(userIdRef).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String deviceId = userDoc.getString("deviceId");
                                        if (deviceId != null) {
                                            userIdsList.add(deviceId); // Add the deviceId to the respective list
                                        }
                                    }
                                });
                    }
                });
    }

    public static Event createEventInDatabase(int capacity, String dateTime, String description,
                                              String facilityLocation, String facilityName,
                                              boolean isGeolocationEnabled, String name,
                                              String registrationDeadline, int waitListCapacity,
                                              String organizerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            // Initialize empty lists
            ArrayList<String> cancelledList = new ArrayList<>();
            ArrayList<String> finalList = new ArrayList<>();
            ArrayList<String> selectedList = new ArrayList<>();
            ArrayList<String> waitingList = new ArrayList<>();

            // Create event object with correct constructor
            Event event = new Event(capacity, dateTime, description, facilityLocation,
                    facilityName, isGeolocationEnabled, name,
                    registrationDeadline, waitListCapacity,
                    cancelledList, finalList, selectedList, waitingList);

            // Create event data map
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("name", event.getName());
            eventData.put("date_Time", event.getDate_Time());
            eventData.put("description", description);
            eventData.put("facilityName", event.getFacilityName());
            eventData.put("facilityLocation", event.getFacilityLocation());
            eventData.put("registrationDeadline", event.getRegistrationDeadline());
            eventData.put("capacity", event.getCapacity());
            eventData.put("waitListCapacity", event.getWaitListCapacity());
            eventData.put("isGeolocationEnabled", event.isGeolocationEnabled());
            eventData.put("qrCode_hashData", event.getQRCode().getQRCodeValue());
            eventData.put("organizerId", organizerId);

            // Get event ID from QR code hash
            String eventId = Integer.toString(event.getQRCode().getHashData());

            // Add event to Firestore
            db.collection("Events").document(eventId)
                    .set(eventData)
                    .addOnSuccessListener(aVoid -> {
                        // Add to organizer's created events
                        Map<String, Object> eventRef = new HashMap<>();
                        eventRef.put("timestamp", com.google.firebase.Timestamp.now());

                        db.collection("Users").document(organizerId)
                                .collection("createdEvents")
                                .document(eventId)
                                .set(eventRef);
                    });

            return event;
        } catch (WriterException e) {
            Log.e("Event", "Error creating event", e);
            return null;
        }
    }
}