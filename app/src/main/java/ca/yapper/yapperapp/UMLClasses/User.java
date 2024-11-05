package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;

import com.google.common.primitives.Booleans;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class User {
    private String deviceId;
    private String email;
    private boolean isAdmin;
    private boolean isEntrant;
    private boolean isOrganizer;
    private String name;
    private String phoneNum;
    // private ProfilePic profilePic;
    // private ProfilePic generatedProfilePic;
    private ArrayList<String> joinedEvents;
    private ArrayList<String> registeredEvents;
    private ArrayList<String> missedOutEvents;

    // default constructor (required for Firestore deserialization)
    public User() {}
    // constructor version with parameters
    public User(String deviceId, String email, boolean isAdmin, boolean isEntrant, boolean isOrganizer, String name, String phoneNum, ArrayList<String> joinedEvents, ArrayList<String> selectedEvents, ArrayList<String> waitingList) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
        this.joinedEvents = joinedEvents;
        this.selectedEvents = selectedEvents;
        this.waitingEvents = waitingEvents;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {

        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Boolean getIsEntrant() {
        return isEntrant;
    }

    public void setIsEntrant(Boolean entrant) {
        isEntrant = entrant;
    }

    public Boolean getIsOrganizer() {
        return isOrganizer;
    }

    public void setIsOrganizer(Boolean organizer) {
        isOrganizer = organizer;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public void uploadProfilePic(ProfilePic profilePic) {
        // method logic
    }

    public void removeProfilePic() {
        // method logic
    }

    public static Event loadUserFromDatabase(String userDeviceId) {
        // connect to Firestore
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        Log.d("UserDebug", "Loading user with ID: " + userDeviceId);

        db.collection("Users").document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("UserDebug", "User document found with ID: " + userDeviceId);
                        // get User details
                        // public User(String deviceId, String email, boolean isAdmin, boolean isEntrant, boolean isOrganizer, String name, String phoneNum, ArrayList<String> joinedEvents, ArrayList<String> selectedEvents, ArrayList<String> waitingList) {
                        String deviceIdString = documentSnapshot.getString("deviceId");
                        String emailString = documentSnapshot.getString("email");
                        boolean isAdminBoolean = documentSnapshot.getBoolean("isAdmin");
                        boolean isEntrantBoolean = documentSnapshot.getBoolean("isEntrant");
                        boolean isOrganizerBoolean = documentSnapshot.getBoolean("isOrganizer");
                        String nameString = documentSnapshot.getString("name");
                        String phoneNum = documentSnapshot.getString("phoneNum");
                        ArrayList<String> joinedEventsArray = null;
                        ArrayList<String> registeredEventsArray = null;
                        ArrayList<String> missedOutEventsArray = null;

                        loadEventIdsFromSubcollection(db, userDeviceId, "joinedEvents", joinedEventsArray);
                        loadEventIdsFromSubcollection(db, userDeviceId, "registeredEvents", registeredEventsArray);
                        loadEventIdsFromSubcollection(db, userDeviceId, "missedOutEvents", missedOutEventsArray);

                        // Here you can create an User object and set these ArrayLists to their respective fields
                        User user = new User(deviceIdString, emailString, isAdminBoolean, isEntrantBoolean, isOrganizerBoolean, nameString, phoneNum, joinedEventsArray, registeredEventsArray, missedOutEventsArray);

                        Log.d("UserDebug", "User details loaded successfully.");
                    } else {
                        Log.w("UserDebug", "User document with ID: " + userDeviceId + " does not exist.");
                    }
                })
                .addOnFailureListener(e -> Log.e("UserDebug", "Error loading user document with ID: " + userDeviceId, e));

        return null; // or return the created User object if applicable
    }

    private static void loadEventIdsFromSubcollection(FirebaseFirestore db, String userDeviceId, String subcollectionName, ArrayList<String> eventIdsList) {
        db.collection("Users").document(userDeviceId).collection(subcollectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Assuming each document in the subcollection is a reference to a Event in the "Events" collection
                        String eventIdRef = doc.getId(); // Get the document ID in the subcollection (reference to Event ID)

                        // Retrieve the Event document to get the eventId
                        db.collection("Events").document(eventIdRef).get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (eventDoc.exists()) {
                                        String eventId = eventDoc.getString("eventId");
                                        if (eventId != null) {
                                            eventIdsList.add(eventId); // Add the eventId to the respective list
                                        }
                                    }
                                });
                    }
                });
    }
}