package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.util.ArrayList;

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

    public static Event loadEventFromDatabase(String hashData) {
        // connect to Firestore
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        Event notEvent = null;
        Log.d("EventDebug", "Loading event with ID: " + hashData);

        db.collection("Events").document(hashData).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d("EventDebug", "Event document found with ID: " + hashData);
                // get Event details
                String eventCapacity = documentSnapshot.contains("capacity") ?
                        Long.toString(documentSnapshot.getLong("capacity")) : "0";
                String eventDateTimeString = documentSnapshot.getString("date_Time");
                String eventDescriptionString = documentSnapshot.getString("description");
                String eventFacilityName = documentSnapshot.getString("facilityName");
                String eventFacilityLocation = documentSnapshot.getString("facilityLocation");
                boolean isGeolocationEnabledBool = documentSnapshot.getBoolean("isGeolocationEnabled") != null ?
                        documentSnapshot.getBoolean("isGeolocationEnabled") : false;
                String eventNameString = documentSnapshot.getString("name");
                String eventRegDeadlineString = documentSnapshot.getString("regDeadline");
                String eventWaitListCapacity = documentSnapshot.contains("waitListCapacity") ?
                        Long.toString(documentSnapshot.getLong("waitListCapacity")) : "0";
                eventWaitListFinal = Integer.valueOf(eventWaitListCapacity);
                ArrayList<String> eventCancelledList = null;
                ArrayList<String> eventFinalList = null;
                ArrayList<String> eventSelectedList = null;
                ArrayList<String> eventWaitingList = null;

                loadUserIdsFromSubcollection(db, hashData, "waitingList", eventWaitingList);
                loadUserIdsFromSubcollection(db, hashData, "selectedList", eventSelectedList);
                loadUserIdsFromSubcollection(db, hashData, "finalList", eventFinalList);
                loadUserIdsFromSubcollection(db, hashData, "cancelledList", eventCancelledList);

                // Here you can create an Event object and set these ArrayLists to their respective fields
                // Event event = new Event(...); (Initialize and set fields here as per your Event class)
                //     public Event(int capacity, String date_Time, String description, String facilityLocation, String facilityName, boolean isGeolocationEnabled, String name, String registrationDeadline, int waitListCapacity, ArrayList<String> cancelledList, ArrayList<String> finalList, ArrayList<String> selectedList, ArrayList<String> waitingList) throws WriterException {
                try {
                    return new Event(eventCapacity, eventDateTimeString, eventDescriptionString, eventFacilityLocation, eventFacilityName, isGeolocationEnabledBool, eventNameString, eventRegDeadlineString, eventWaitListCapacity, eventCancelledList, eventFinalList, eventSelectedList, eventWaitingList);
                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }

                Log.d("EventDebug", "Event details loaded successfully.");
            } else {
                Log.w("EventDebug", "Event document with ID: " + hashData + " does not exist.");
            }
                })
                .addOnFailureListener(e -> Log.e("EventDebug", "Error loading event document with ID: " + hashData, e));

        return notEvent; // or return the created Event object if applicable
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
}