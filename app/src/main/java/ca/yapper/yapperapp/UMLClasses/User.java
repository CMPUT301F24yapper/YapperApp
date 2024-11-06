package ca.yapper.yapperapp.UMLClasses;

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
    private boolean isOptedOut;
    private ArrayList<String> joinedEvents;
    private ArrayList<String> registeredEvents;
    private ArrayList<String> missedOutEvents;
    private ArrayList<String> createdEvents;

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

    public void uploadProfilePic(ProfilePic profilePic) {
    }

    public void removeProfilePic() {
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

    public void setJoinedEvents(ArrayList<String> joinedEvents) {
        this.joinedEvents = joinedEvents;
    }

    public ArrayList<String> registeredEvents() {
        return registeredEvents;
    }

    public void setRegisteredEvents(ArrayList<String> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    public ArrayList<String> missedOutEvents() {
        return missedOutEvents;
    }

    public void setMissedOutEvents(ArrayList<String> missedOutEvents) {
        this.missedOutEvents = missedOutEvents;
    }

    public ArrayList<String> getCreatedEvents() {
        return createdEvents;
    }

    public void setCreatedEvents(ArrayList<String> createdEvents) {
        this.createdEvents = createdEvents;
    }

    public static User loadUserFromDatabase(String userDeviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        User user = new User(userDeviceId, "", false, false, false, "", "", false,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        db.collection("Users").document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                user.setAdmin(documentSnapshot.getBoolean("Admin"));
                user.setEntrant(documentSnapshot.getBoolean("Entrant"));
                user.setOrganizer(documentSnapshot.getBoolean("Organizer"));
                user.setDeviceId(documentSnapshot.getString("deviceId"));
                user.setEmail(documentSnapshot.getString("entrantEmail"));
                user.setName(documentSnapshot.getString("entrantName"));
                user.setPhoneNum(documentSnapshot.getString("entrantPhone"));

                loadEventIdsFromSubcollection(db, userDeviceId, "joinedEvents", user.joinedEvents);
                loadEventIdsFromSubcollection(db, userDeviceId, "registeredEvents", user.registeredEvents);
                loadEventIdsFromSubcollection(db, userDeviceId, "missedOutEvents", user.missedOutEvents);
                loadEventIdsFromSubcollection(db, userDeviceId, "createdEvents", user.createdEvents);
            }
        });

        return user;
    }

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
}