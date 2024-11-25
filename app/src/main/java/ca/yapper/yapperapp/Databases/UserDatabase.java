package ca.yapper.yapperapp.Databases;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.UMLClasses.User;

public class UserDatabase {

    private static final FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    /**
     * Checks if a user is an admin based on their device ID.
     *
     * @param deviceId The device ID of the user.
     * @return A Task that resolves to true if the user is an admin, false otherwise.
     */
    public static Task<Boolean> checkIfUserIsAdmin(String deviceId) {
        return FirestoreUtils.checkDocumentField("Users", deviceId, "Admin");
    }

    /**
     * Loads a User from Firestore using the specified device ID and provides the result
     * through the provided listener.
     *
     * @param userDeviceId The unique device ID of the user to be loaded.
     * @param listener The listener to handle success or error when loading the user.
     */
    public static void loadUserFromDatabase(String userDeviceId,
                                            EntrantDatabase.OnUserLoadedListener listener) {
        db.collection("Users").document(userDeviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            User user = new User(
                                    documentSnapshot.getString("deviceId"),
                                    documentSnapshot.getString("entrantEmail"),
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
        /**
        // DON'T NEED SUBCOLLECTION INSTANTIATION (is directly instantiated when applicable, i.e. when they join, cancel, are selected for an event...)
        db.collection("Users").document(deviceId).set(userData);
        db.collection("Users").document(deviceId).collection("joinedEvents").document("placeholder").set(timestamp);
        db.collection("Users").document(deviceId).collection("registeredEvents").document("placeholder").set(timestamp);
        db.collection("Users").document(deviceId).collection("missedOutEvents").document("placeholder").set(timestamp);
        db.collection("Users").document(deviceId).collection("createdEvents").document("placeholder").set(timestamp); **/

        Log.d("User", "User and subcollections created");

        return user;
    }

    public static void updateUserField(String deviceId, String field, Object fieldContents, EntrantDatabase.OnFieldUpdateListener listener) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(field, fieldContents); // Add the new image as Base64 string

        db.collection("Users").document(deviceId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> listener.onFieldUpdated(fieldContents))  // update successful
                .addOnFailureListener(e -> listener.onError("error"));  // update failed
    }
}
