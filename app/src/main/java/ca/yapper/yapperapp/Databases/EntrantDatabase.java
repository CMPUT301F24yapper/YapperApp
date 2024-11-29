package ca.yapper.yapperapp.Databases;

import android.util.Log;

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

/**
 * Class holding all the entrant related functions that interact with the database.
 */
public class EntrantDatabase {
    private static final FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();


    /**
     * Interface that defines callback methods
     */
    public interface OnUserCheckListener {
        void onUserInList(boolean inList);
        void onError(String error);
    }


    /**
     * Interface for operation completion checks.
     */
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


    /**
     * Interface for event finding methods and errors associated with it
     */
    public interface OnEventFoundListener {
        void onEventFound(String eventId);
        void onEventNotFound();
        void onError(Exception e);
    }


    /**
     * Interface for event loading methods and errors associated with it
     */
    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<Event> events);
        void onError(String error);
    }


    /**
     * Function that uses a listener to check if a user is in an events waiting list.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param userId The id for the user, created from the device id.
     * @param listener handles the outcome of the user check
     */
    public static void checkUserInEvent(String eventId, String userId, EntrantDatabase.OnUserCheckListener listener) {
        FirestoreUtils.checkDocumentField("Events/" + eventId + "/waitingList", userId, null)
                .addOnSuccessListener(listener::onUserInList)
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    /**
     * Function that adds a user to the waiting list of an event and updates the joined events list.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param userId The id for the user, created from the device id.
     * @param listener handles the outcome of the event check
     */
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


    /**
     * Function that removes a user to the waiting list of an event and updates the joined events list.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param userId The id for the user, created from the device id.
     * @param listener handles the outcome of the event check
     */
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
     * @param userDeviceId The device ID of the user.
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
     * This function obtains an event by using its document id, which is the QR Code string value.
     *
     * @param documentId id of the event in the database
     * @param listener listener to handle outcome of getting the event
     */
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


    /**
     * Loads a list of events for the given event list type for a specified user.
     *
     * @param userDeviceId id of the users device used in database, different from profile name
     * @param type the type of events list(joined, registered, missed)
     * @param listener listener to handle outcome of loading an event
     */
    public static void loadEventsList(String userDeviceId, EventListType type, OnEventsLoadedListener listener) {
        Log.e("loadeventslist from entrantsdb", "loading events list");
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
                        Log.e("entrantdb", "loading eventId:" + eventId);

                        db.collection("Events").document(eventId).get()
                                .addOnSuccessListener(eventDoc -> {
                                    try {
                                        Event event = new Event(
                                                eventDoc.getLong("capacity") != null ? eventDoc.getLong("capacity").intValue() : 0,
                                                eventDoc.getString("date_Time"),
                                                eventDoc.getString("description"),
                                                eventDoc.getString("facilityLocation"),
                                                eventDoc.getString("facilityName"),
                                                eventDoc.getBoolean("isGeolocationEnabled"),
                                                eventDoc.getString("name"),
                                                eventDoc.getString("organizerId"),
                                                eventDoc.getString("registrationDeadline"),
                                                eventDoc.getLong("waitListCapacity") != null ? eventDoc.getLong("waitListCapacity").intValue() : 0,
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


    /**
     * enumerates the types of event lists
     */
    public enum EventListType {
        JOINED,
        REGISTERED,
        MISSED
    }


    /**
     * Function that loads a profile image for a given user by accessing the database.
     *
     * @param deviceId The device ID of the user.
     * @param listener listener to handle outcome of loading an image profile
     */
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


    /**
     * Interface for loading images and associated error handling
     */
    public interface OnProfileImageLoadedListener {
        void onProfileImageLoaded(String base64Image);
        void onError(String error);
    }

    /**
     * Interface for updating fields and the associated error handling
     */
    public interface OnFieldUpdateListener {
        void onFieldUpdated(Object value);
        void onError(String error);
    }

}
