package ca.yapper.yapperapp.Databases;

import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;

public class EntrantDatabase {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    static String base64Image;

    public interface OnUserCheckListener {
        void onUserInList(boolean inList);
        void onError(String error);
    }

    public interface OnOperationCompleteListener {
        void onComplete(boolean success);
    }

    /**
     * Interface for handling the result of loading a User from Firestore.
     */
    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onUserLoadError(String error);
    }

    public interface OnEventFoundListener {
        void onEventFound(String eventId);
        void onEventNotFound();
        void onError(Exception e);
    }

    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<Event> events);
        void onError(String error);
    }

    /**
     * Checks if a user is an admin based on their device ID.
     *
     * @param deviceId The device ID of the user.
     * @return A Task that resolves to true if the user is an admin, false otherwise.
     */
    public static Task<Boolean> checkIfUserIsAdmin(String deviceId) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Boolean isAdmin = document.getBoolean("Admin");
                        tcs.setResult(isAdmin != null && isAdmin);
                    } else {
                        tcs.setResult(false);
                    }
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    public static void checkUserInEvent(String eventId, String userId, EntrantDatabase.OnUserCheckListener listener) {
        db.collection("Events").document(eventId).collection("waitingList").document(userId).get()
                .addOnSuccessListener(document -> listener.onUserInList(document.exists()))
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
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

    public static void joinEvent(String eventId, String userId, OnOperationCompleteListener listener) {
        // Implement join event logic using Firestore batch writes

        // Create timestamp data
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("timestamp", FieldValue.serverTimestamp());

        // Start a batch write
        WriteBatch batch = db.batch();

        // Add to event's waiting list
        DocumentReference eventWaitingListRef = db.collection("Events")
                .document(eventId)
                .collection("waitingList")
                .document(userId);
        batch.set(eventWaitingListRef, entrantData);

        // Add to user's joined events
        DocumentReference userJoinedEventsRef = db.collection("Users")
                .document(userId)
                .collection("joinedEvents")
                .document(eventId);
        batch.set(userJoinedEventsRef, entrantData);

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
        }

    public static void unjoinEvent(String eventId, String userId, EntrantDatabase.OnOperationCompleteListener listener) {
     // Start a batch write
            WriteBatch batch = db.batch();

            // Remove from event's waiting list
            DocumentReference eventWaitingListRef = db.collection("Events")
                    .document(eventId)
                    .collection("waitingList")
                    .document(userId);
            batch.delete(eventWaitingListRef);

            // Remove from user's joined events
            DocumentReference userJoinedEventsRef = db.collection("Users")
                    .document(userId)
                    .collection("joinedEvents")
                    .document(eventId);
            batch.delete(userJoinedEventsRef);

            // Commit the batch
            batch.commit()
                    .addOnSuccessListener(aVoid -> listener.onComplete(true))
                    .addOnFailureListener(e -> listener.onComplete(false));
        }

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

    public static void getEventByQRCode(String documentId, OnEventFoundListener listener) {
        db.collection("Events").document(documentId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        listener.onEventFound(documentId);
                    } else {
                        if (!task.isSuccessful()) {
                            listener.onError(task.getException());
                        } else {
                            listener.onEventNotFound();
                        }
                    }
                });
    }

    public static void loadEventsList(String userDeviceId, EventListType type, OnEventsLoadedListener listener) {
        String collectionName;
        if (type == EventListType.JOINED) {
            collectionName = "joinedEvents";
        } else if (type == EventListType.REGISTERED) {
            collectionName = "registeredEvents";
        } else {
            collectionName = "missedOutEvents";
        }

        db.collection("Users")
                .document(userDeviceId)
                .collection(collectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onEventsLoaded(new ArrayList<>());
                        return;
                    }

                    List<Event> eventList = new ArrayList<>();
                    AtomicInteger pendingEvents = new AtomicInteger(queryDocumentSnapshots.size());

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();

                        db.collection("Events").document(eventId).get()
                                .addOnSuccessListener(eventDoc -> {
                                    try {
                                        Event event = new Event(
                                                eventDoc.getLong("capacity").intValue(),
                                                eventDoc.getString("date_Time"),
                                                eventDoc.getString("description"),
                                                eventDoc.getString("facilityLocation"),
                                                eventDoc.getString("facilityName"),
                                                eventDoc.getBoolean("isGeolocationEnabled"),
                                                eventDoc.getString("name"),
                                                eventDoc.getString("registrationDeadline"),
                                                eventDoc.getLong("waitListCapacity").intValue(),
                                                new ArrayList<>(), new ArrayList<>(),
                                                new ArrayList<>(), new ArrayList<>()
                                        );
                                        eventList.add(event);
                                    } catch (WriterException e) {
                                        Log.e("Database", "Error creating event", e);
                                    }

                                    if (pendingEvents.decrementAndGet() == 0) {
                                        listener.onEventsLoaded(eventList);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Database", "Error loading event " + eventId, e);
                                    if (pendingEvents.decrementAndGet() == 0) {
                                        listener.onEventsLoaded(eventList);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error loading events: " + e.getMessage()));
    }

    public enum EventListType {
        JOINED,
        REGISTERED,
        MISSED
    }

    public static void loadProfileImage(String deviceId, final OnProfileImageLoadedListener listener) {
        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String base64Image = document.getString("profileImage");
                        listener.onProfileImageLoaded(base64Image); // Send the image to the listener
                    } else {
                        listener.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error retrieving image: " + e.getMessage()));
    }

    // Define an interface to handle the result when the profile image is loaded
    public interface OnProfileImageLoadedListener {
        void onProfileImageLoaded(String base64Image);  // When the image is successfully loaded
        void onError(String error);                    // When there's an error
    }

    public static void updateUserField(String deviceId, String field, Object fieldContents, OnFieldUpdateListener listener) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(field, fieldContents); // Add the new image as Base64 string

        db.collection("Users").document(deviceId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> listener.onFieldUpdated(fieldContents))  // update successful
                .addOnFailureListener(e -> listener.onError("error"));  // update failed
    }

    public interface OnFieldUpdateListener {
        void onFieldUpdated(Object value);  // When the image is successfully loaded
        void onError(String error);                    // When there's an error
    }

}
