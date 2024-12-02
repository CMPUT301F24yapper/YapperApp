package ca.yapper.yapperapp.Databases;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.WriterException;

import java.io.IOException;
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
    private static FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    public static void setFirestoreInstance(FirebaseFirestore firestore) {
        db = firestore;
    }


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
        batch.set(eventWaitingListRef, entrantData, SetOptions.merge());

        // Add to user's joined events
        DocumentReference userJoinedEventsRef = db.collection("Users")
                .document(userId)
                .collection("joinedEvents")
                .document(eventId);
        batch.set(userJoinedEventsRef, entrantData, SetOptions.merge());

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
     * This function adds events to an entrants registered events list
     *
     * @param userId The id for the user, created from the device id.
     * @param eventId The unique id for the event, created from the QR code.
     */
    public static void addEventToRegisteredEvents(String userId, String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the user's joinedEvents subcollection
        DocumentReference eventDocRef = db.collection("Users")
                .document(userId)
                .collection("joinedEvents")
                .document(eventId);

        // Add the event with initial status
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("timestamp", FieldValue.serverTimestamp());
        // eventData.put("invitationStatus", "Pending");  // Default status is "Pending"

        eventDocRef.set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EntrantDatabase", "Event added to registeredEvents.");
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantDatabase", "Error adding event to registeredEvents: " + e.getMessage());
                });
    }


    /**
     * This function updates the invitation status for a user
     *
     * @param userId The id for the user, created from the device id.
     * @param eventId The unique id for the event, created from the QR code.
     * @param newStatus the new status of the invitation
     * @param listener listener handles the outcome of the operation
     */
   /** public static void updateInvitationStatus(String userId, String eventId, String newStatus, OnOperationCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocRef = db.collection("Users")
                .document(userId)
                .collection("joinedEvents")
                .document(eventId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("invitationStatus", newStatus);

        eventDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EntrantDatabase", "Invitation status updated to: " + newStatus);
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantDatabase", "Error updating invitation status: " + e.getMessage());
                    listener.onComplete(false);
                });
    } **/


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
                                                eventDoc.getLong("waitListCapacity") != null ? eventDoc.getLong("waitListCapacity").intValue() : null,
                                                new ArrayList<>(), new ArrayList<>(),
                                                new ArrayList<>(), new ArrayList<>()
                                        );
                                        event.setDocumentId(eventId);
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
     * Loads a User from Firestore using the specified device ID and provides the result
     * through the provided listener.
     *
     * @param userDeviceId The unique device ID of the user to be loaded.
     * @param listener The listener to handle success or error when loading the user.
     */
    public static void loadUserFromDatabase(String userDeviceId, OnUserLoadedListener listener) {
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
     * This function obtains the invitation status for a specific user and event
     *
     * @param userId The id for the user, created from the device id.
     * @param eventId The unique id for the event, created from the QR code.
     * @param listener  handles the outcome of the invitation status check
     */
    public static void getInvitationStatus(String userId, String eventId, OnStatusCheckListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocRef = db.collection("Users")
                .document(userId)
                .collection("joinedEvents")
                .document(eventId);

        eventDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("invitationStatus");
                        if (status != null) {
                            listener.onStatusLoaded(status);
                        } else {
                            listener.onStatusNotFound();  // Status field missing
                        }
                    } else {
                        listener.onUserNotInList();  // User not part of this event
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error fetching status: " + e.getMessage()));
    }


    /**
     * Interface for status checking methods and errors associated with it
     */
    public interface OnStatusCheckListener {
        void onStatusLoaded(String status);
        void onStatusNotFound();
        void onUserNotInList();
        void onError(String error);
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
                        try {
                            listener.onProfileImageLoaded(base64Image); // Send the image to the listener
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        listener.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error retrieving image: " + e.getMessage()));
    }


    /**
     * This function moves a given user between two of an events subcollection.
     *
     * @param eventId The id for the event
     * @param userId The id for the user, created from the device id.
     * @param fromSubcollection subcollection user was originally in
     * @param toSubcollection subcollection to move user to
     * @param listener handles the outcome of the database operation
     */
    public static void moveUserBetweenUserSubcollections(String eventId, String userId, String fromSubcollection, String toSubcollection, EntrantDatabase.OnOperationCompleteListener listener) {
        // References to the relevant documents
        DocumentReference fromDocRef = db.collection("Users")
                .document(userId)
                .collection(fromSubcollection)
                .document(eventId);

        DocumentReference toDocRef = db.collection("Users")
                .document(userId)
                .collection(toSubcollection)
                .document(eventId);

        // Retrieve user data from the original subcollection
        fromDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> userData = documentSnapshot.getData();

                // Begin batch operation
                WriteBatch batch = db.batch();

                // Add user to the new subcollection
                batch.set(toDocRef, userData);

                // Remove user from the original subcollection
                batch.delete(fromDocRef);

                // Commit the batch
                batch.commit().addOnSuccessListener(aVoid -> {
                    Log.d("EntrantDatabase", "Successfully moved user: " + userId + " from " + fromSubcollection + " to " + toSubcollection);
                }).addOnFailureListener(e -> {
                    Log.e("EntrantDatabase", "Error moving user: " + e.getMessage());
                });
            } else {
                Log.e("EntrantDatabase", "User not found in " + fromSubcollection);
            }
        }).addOnFailureListener(e -> {
            Log.e("EntrantDatabase", "Error retrieving user: " + e.getMessage());
        });
    }

    /**
     * Interface for loading images and associated error handling
     */
    public interface OnProfileImageLoadedListener {
        void onProfileImageLoaded(String base64Image) throws IOException;
        void onError(String error);
    }

    /**
     * Interface for updating fields and the associated error handling
     */
    public interface OnFieldUpdateListener {
        void onFieldUpdated(Object value);
        void onError(String error);
    }

    public static void loadRegisteredEventsFromFinalLists(String userId, OnEventsLoadedListener listener) {
        db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onEventsLoaded(new ArrayList<>());
                        return;
                    }

                    List<Event> eventList = new ArrayList<>();
                    AtomicInteger pendingEvents = new AtomicInteger(queryDocumentSnapshots.size());

                    for (DocumentSnapshot eventDoc : queryDocumentSnapshots) {
                        String eventId = eventDoc.getId();

                        // Check if user exists in finalList
                        eventDoc.getReference()
                                .collection("finalList")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(finalListDoc -> {
                                    if (finalListDoc.exists()) {
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
                                                    eventDoc.getLong("waitListCapacity") != null ? eventDoc.getLong("waitListCapacity").intValue() : null,
                                                    new ArrayList<>(), new ArrayList<>(),
                                                    new ArrayList<>(), new ArrayList<>()
                                            );
                                            event.setDocumentId(eventId);
                                            eventList.add(event);
                                        } catch (WriterException e) {
                                            Log.e("Database", "Error creating event", e);
                                        }
                                    }

                                    if (pendingEvents.decrementAndGet() == 0) {
                                        listener.onEventsLoaded(eventList);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Database", "Error checking finalList for event " + eventId, e);
                                    if (pendingEvents.decrementAndGet() == 0) {
                                        listener.onEventsLoaded(eventList);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error loading events: " + e.getMessage()));
    }

}