package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/**
 * The User class represents a user in the application with details such as device ID,
 * contact information, roles, and lists of events they are associated with.
 * This class provides methods to manage user data in Firestore and retrieve user information.
 */
public class User {

    private String deviceId;
    private String email;
    private boolean isAdmin;
    private boolean isEntrant;
    private boolean isOrganizer;
    private String name;
    private String phoneNum;
    private boolean isOptedOut;
    private ArrayList<String> joinedEvents;
    private ArrayList<String> registeredEvents;
    private ArrayList<String> missedOutEvents;
    private ArrayList<String> createdEvents;


    /**
     * Constructs a new User with the specified attributes.
     *
     * @param deviceId The unique device ID of the user.
     * @param email The email address of the user.
     * @param isAdmin True if the user has admin privileges.
     * @param isEntrant True if the user has entrant privileges.
     * @param isOrganizer True if the user has organizer privileges.
     * @param name The name of the user.
     * @param phoneNum The phone number of the user.
     * @param isOptedOut True if the user has opted out of notifications.
     * @param joinedEvents List of event IDs the user has joined.
     * @param registeredEvents List of event IDs the user has registered for.
     * @param missedOutEvents List of event IDs the user missed out on.
     * @param createdEvents List of event IDs the user created.
     */
    public User(String deviceId, String email, boolean isAdmin, boolean isEntrant, boolean isOrganizer, String name, String phoneNum, boolean isOptedOut, ArrayList<String> joinedEvents, ArrayList<String> registeredEvents, ArrayList<String> missedOutEvents, ArrayList<String> createdEvents) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
        this.isOptedOut = isOptedOut;
        this.joinedEvents = joinedEvents;
        this.registeredEvents = registeredEvents;
        this.missedOutEvents = missedOutEvents;
        this.createdEvents = createdEvents;
    }


    /**
     * Loads a User from Firestore using the specified device ID and provides the result
     * through the provided listener.
     *
     * @param userDeviceId The unique device ID of the user to be loaded.
     * @param listener The listener to handle success or error when loading the user.
     */
    public static void loadUserFromDatabase(String userDeviceId, OnUserLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userDeviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            // public User(String deviceId, String email, boolean isAdmin, boolean isEntrant, boolean isOrganizer,
                            // String name, String phoneNum, boolean isOptedOut, ArrayList<String> joinedEvents, ArrayList<String> registeredEvents, ArrayList<String> missedOutEvents, ArrayList<String> createdEvents) {
                            User user = new User(
                                    /** user.setAdmin(documentSnapshot.getBoolean("Admin"));
                                     user.setEntrant(documentSnapshot.getBoolean("Entrant"));
                                     user.setOrganizer(documentSnapshot.getBoolean("Organizer"));
                                     user.setDeviceId(documentSnapshot.getString("deviceId"));
                                     user.setEmail(documentSnapshot.getString("entrantEmail"));
                                     user.setName(documentSnapshot.getString("entrantName"));
                                     user.setPhoneNum(documentSnapshot.getString("entrantPhone")); **/
                                    documentSnapshot.getString("deviceId"),
                                    documentSnapshot.getString("email"),
                                    documentSnapshot.getBoolean("Admin"),
                                    documentSnapshot.getBoolean("Entrant"),
                                    documentSnapshot.getBoolean("Organizer"),
                                    documentSnapshot.getString("entrantName"),
                                    documentSnapshot.getString("entrantPhone"),
                                    documentSnapshot.getBoolean("notificationsEnabled"),
                                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
                            );
                            listener.onUserLoaded(user);
                        }
                        catch (Exception e) {
                            listener.onUserLoadError("Error creating user");
                        }
                    }
                });
    }
        // User user = new User(userDeviceId, "", false, false, false, "", "", false,
        // new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        /** db.collection("Users").document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
         if (documentSnapshot.exists()) {
         loadEventIdsFromSubcollection(db, userDeviceId, "joinedEvents", user.joinedEvents);
         loadEventIdsFromSubcollection(db, userDeviceId, "registeredEvents", user.registeredEvents);
         loadEventIdsFromSubcollection(db, userDeviceId, "missedOutEvents", user.missedOutEvents);
         loadEventIdsFromSubcollection(db, userDeviceId, "createdEvents", user.createdEvents);
         }
         });
         return user;
         } **/


    /**
     * Loads event IDs from a specified subcollection in Firestore and populates the eventIdsList.
     *
     * @param db Firestore instance.
     * @param userDeviceId The unique device ID of the user.
     * @param subcollectionName The name of the subcollection to load.
     * @param eventIdsList The list to store retrieved event IDs.
     */
    private static void loadEventIdsFromSubcollection(FirebaseFirestore db, String userDeviceId, String subcollectionName, ArrayList<String> eventIdsList) {
        db.collection("Users").document(userDeviceId).collection(subcollectionName).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String eventIdRef = doc.getId();

                db.collection("Events").document(eventIdRef).get().addOnSuccessListener(eventDoc -> {
                    if (eventDoc.exists()) {
                        String eventId = eventDoc.getString("qrCode_hashData");
                        if (eventId != null) {
                            eventIdsList.add(eventId);
                        }
                    }
                });
            }
        });
    }


    /**
     * Creates a new user entry in Firestore with the provided details.
     *
     * @param deviceId The unique device ID of the user.
     * @param email The email address of the user.
     * @param isAdmin True if the user has admin privileges.
     * @param isEntrant True if the user has entrant privileges.
     * @param isOrganizer True if the user has organizer privileges.
     * @param name The name of the user.
     * @param phoneNum The phone number of the user.
     * @param isOptedOut True if the user has opted out of notifications.
     * @return The created User instance.
     */
    public static User createUserInDatabase(String deviceId, String email, boolean isAdmin,
                                            boolean isEntrant, boolean isOrganizer, String name,
                                            String phoneNum, boolean isOptedOut) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        User user = new User(deviceId, email, isAdmin, isEntrant, isOrganizer, name, phoneNum,
                isOptedOut, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());

        Map<String, Object> userData = new HashMap<>();
        userData.put("deviceId", user.getDeviceId());
        userData.put("entrantEmail", user.getEmail());
        userData.put("Admin", user.isAdmin());
        userData.put("Entrant", user.isEntrant());
        userData.put("Organizer", user.isOrganizer());
        userData.put("entrantName", user.getName());
        userData.put("entrantPhone", user.getPhoneNum());
        userData.put("notificationsEnabled", !user.isOptedOut());

        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("created", com.google.firebase.Timestamp.now());

        db.collection("Users").document(deviceId).set(userData);
        db.collection("Users").document(deviceId).collection("joinedEvents").document("placeholder").set(timestamp);
        db.collection("Users").document(deviceId).collection("registeredEvents").document("placeholder").set(timestamp);
        db.collection("Users").document(deviceId).collection("missedOutEvents").document("placeholder").set(timestamp);
        db.collection("Users").document(deviceId).collection("createdEvents").document("placeholder").set(timestamp);

        Log.d("User", "User and subcollections created");

        return user;
    }


    /**
     * Converts the user's attributes to a Map, suitable for storage in Firestore.
     *
     * @return A map containing the user's attributes.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("deviceId", deviceId);
        userMap.put("email", email);
        userMap.put("isAdmin", isAdmin);
        userMap.put("isEntrant", isEntrant);
        userMap.put("isOrganizer", isOrganizer);
        userMap.put("name", name);
        userMap.put("phoneNum", phoneNum);
        userMap.put("isOptedOut", isOptedOut);
        userMap.put("joinedEvents", joinedEvents);
        userMap.put("registeredEvents", registeredEvents);
        userMap.put("missedOutEvents", missedOutEvents);
        userMap.put("createdEvents", createdEvents);

        return userMap;
    }


    /**
     * Interface for handling the result of loading a User from Firestore.
     */
    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onUserLoadError(String error);
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

    public boolean isOptedOut() {
        return isOptedOut;
    }

    public void setOptedOut(boolean optedOut) {
        isOptedOut = optedOut;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isEntrant() {
        return isEntrant;
    }

    public void setEntrant(boolean entrant) {
        isEntrant = entrant;
    }

    public boolean isOrganizer() {
        return isOrganizer;
    }

    public void setOrganizer(boolean organizer) {
        isOrganizer = organizer;
    }

    public ArrayList<String> joinedEvents() {
        return joinedEvents;
    }

    public void setJoinedEvents(ArrayList<String> joinedEvents) {this.joinedEvents = joinedEvents;}

    public ArrayList<String> registeredEvents() {
        return registeredEvents;
    }

    public void setRegisteredEvents(ArrayList<String> registeredEvents) {this.registeredEvents = registeredEvents;}

    public ArrayList<String> missedOutEvents() {
        return missedOutEvents;
    }

    public void setMissedOutEvents(ArrayList<String> missedOutEvents) {this.missedOutEvents = missedOutEvents;}

    public ArrayList<String> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(ArrayList<String> createdEvents) {this.createdEvents = createdEvents;}
}