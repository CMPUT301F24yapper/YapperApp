package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public Event(int capacity, String date_Time, String description, String facilityLocation, String facilityName,
                 boolean isGeolocationEnabled, String name, String registrationDeadline, int waitListCapacity,
                 ArrayList<String> cancelledList, ArrayList<String> finalList, ArrayList<String> selectedList,
                 ArrayList<String> waitingList) throws WriterException {
        this.capacity = capacity;
        this.date_Time = date_Time;
        this.description = description;
        this.facilityLocation = facilityLocation;
        this.facilityName = facilityName;
        this.isGeolocationEnabled = isGeolocationEnabled;
        this.name = name;
        this.QRCode = new qrCode(this.name);
        this.registrationDeadline = registrationDeadline;
        this.waitListCapacity = waitListCapacity;
        this.cancelledList = cancelledList;
        this.finalList = finalList;
        this.selectedList = selectedList;
        this.waitingList = waitingList;
    }

    public static void loadEventFromDatabase(String hashData, OnEventLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(hashData).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Event event = new Event(
                                    documentSnapshot.getLong("capacity").intValue(),
                                    documentSnapshot.getString("date_Time"),
                                    "",
                                    documentSnapshot.getString("facilityLocation"),
                                    documentSnapshot.getString("facilityName"),
                                    documentSnapshot.getBoolean("isGeolocationEnabled"),
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getString("regDeadline"),
                                    documentSnapshot.getLong("waitListCapacity").intValue(),
                                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
                            );
                            listener.onEventLoaded(event);
                        } catch (WriterException e) {
                            listener.onEventLoadError("Error creating event");
                        }
                    }
                });
    }

    public static void loadUserIdsFromSubcollection(FirebaseFirestore db, String eventId, String subcollectionName, ArrayList<String> userIdsList) {
        db.collection("Events").document(eventId).collection(subcollectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String userIdRef = doc.getId();
                        db.collection("Users").document(userIdRef).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String deviceId = userDoc.getString("deviceId");
                                        if (deviceId != null) {
                                            userIdsList.add(deviceId);
                                        }
                                    }
                                });
                    }
                });
    }

    public static Event createEventInDatabase(int capacity, String dateTime, String description,
                                              String facilityLocation, String facilityName, boolean isGeolocationEnabled, String name,
                                              String registrationDeadline, int waitListCapacity, String organizerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            Event event = new Event(capacity, dateTime, description, facilityLocation,
                    facilityName, isGeolocationEnabled, name, registrationDeadline, waitListCapacity,
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

            String eventId = Integer.toString(event.getQRCode().getHashData());
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("capacity", event.capacity);
            eventData.put("date_Time", event.date_Time);
            eventData.put("description", event.description);
            eventData.put("facilityLocation", event.facilityLocation);
            eventData.put("facilityName", event.facilityName);
            eventData.put("isGeolocationEnabled", event.isGeolocationEnabled);
            eventData.put("name", event.name);
            eventData.put("qrCode_hashData", event.QRCode.getHashData());
            eventData.put("registrationDeadline", event.registrationDeadline);
            eventData.put("waitListCapacity", event.waitListCapacity);
            eventData.put("organizerId", organizerId);

            db.collection("Events").document(eventId).set(eventData);
            Map<String, Object> eventRef = new HashMap<>();
            eventRef.put("timestamp", com.google.firebase.Timestamp.now());
            db.collection("Users").document(organizerId).collection("createdEvents").document(eventId).set(eventRef);

            return event;
        } catch (WriterException e) {
            return null;
        }
    }

    public interface OnEventLoadedListener {
        void onEventLoaded(Event event);
        void onEventLoadError(String error);
    }

//    public int getCapacity() { return capacity; }
//    public void setCapacity(int capacity) { this.capacity = capacity; }
//
//    public String getDate_Time() { return date_Time; }
//    public void setDate_Time(String date_Time) { this.date_Time = date_Time; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public String getFacilityLocation() { return facilityLocation; }
//    public void setFacilityLocation(String facilityLocation) { this.facilityLocation = facilityLocation; }
//
//    public String getFacilityName() { return facilityName; }
//    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
//
//    public boolean isGeolocationEnabled() { return isGeolocationEnabled; }
//    public void setGeolocationEnabled(boolean isGeolocationEnabled) { this.isGeolocationEnabled = isGeolocationEnabled; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public qrCode getQRCode() { return QRCode; }
//    public void setQRCode(qrCode QRCode) { this.QRCode = QRCode; }
//
//    public String getRegistrationDeadline() { return registrationDeadline; }
//    public void setRegistrationDeadline(String registrationDeadline) { this.registrationDeadline = registrationDeadline; }
//
//    public int getWaitListCapacity() { return waitListCapacity; }
//    public void setWaitListCapacity(int waitListCapacity) { this.waitListCapacity = waitListCapacity; }
//
//    public ArrayList<String> cancelledList() { return cancelledList; }
//    public void setCancelledList(ArrayList<String> cancelledList) { this.cancelledList = cancelledList; }
//
//    public ArrayList<String> finalList() { return finalList; }
//    public void setFinalList(ArrayList<String> finalList) { this.finalList = finalList; }
//
//    public ArrayList<String> selectedList() { return selectedList; }
//    public void setSelectedList(ArrayList<String> selectedList) { this.selectedList = selectedList; }
//
//    public ArrayList<String> waitingList() { return waitingList; }
//    public void setWaitingList(ArrayList<String> waitingList) { this.waitingList = waitingList; }


    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getDate_Time() {
        return date_Time;
    }

    public void setDate_Time(String date_Time) {
        this.date_Time = date_Time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFacilityLocation() {
        return facilityLocation;
    }

    public void setFacilityLocation(String facilityLocation) {
        this.facilityLocation = facilityLocation;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public boolean isGeolocationEnabled() {
        return isGeolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        isGeolocationEnabled = geolocationEnabled;
    }

    public qrCode getQRCode() {
        return QRCode;
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

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public int getWaitListCapacity() {
        return waitListCapacity;
    }

    public void setWaitListCapacity(int waitListCapacity) {
        this.waitListCapacity = waitListCapacity;
    }

    public ArrayList<String> getCancelledList() {
        return cancelledList;
    }

    public void setCancelledList(ArrayList<String> cancelledList) {
        this.cancelledList = cancelledList;
    }

    public ArrayList<String> getFinalList() {
        return finalList;
    }

    public void setFinalList(ArrayList<String> finalList) {
        this.finalList = finalList;
    }

    public ArrayList<String> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(ArrayList<String> selectedList) {
        this.selectedList = selectedList;
    }

    public ArrayList<String> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<String> waitingList) {
        this.waitingList = waitingList;
    }
}