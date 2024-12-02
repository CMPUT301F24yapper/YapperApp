package ca.yapper.yapperapp.Databases;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
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
    private static FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    public static void setFirestoreInstance(FirebaseFirestore firestore) {
        db = firestore;
    }

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

    public static void checkUserInEvent(String eventId, String userId, EntrantDatabase.OnUserCheckListener listener) {
        FirestoreUtils.checkDocumentField("Events/" + eventId + "/waitingList", userId, null)
                .addOnSuccessListener(listener::onUserInList)
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
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
        eventData.put("invitationStatus", "Pending");  // Default status is "Pending"

        eventDocRef.set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EntrantDatabase", "Event added to registeredEvents with Pending status.");
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantDatabase", "Error adding event to registeredEvents: " + e.getMessage());
                });
    }

    public static void updateInvitationStatus(String userId, String eventId, String newStatus, OnOperationCompleteListener listener) {
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

    // Define callback interface
    public interface OnStatusCheckListener {
        void onStatusLoaded(String status);  // "Pending", "Accepted", "Rejected", etc.
        void onStatusNotFound();             // If status is missing
        void onUserNotInList();              // If event is not in joinedEvents
        void onError(String error);          // Error handling
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

    public interface OnFieldUpdateListener {
        void onFieldUpdated(Object value);  // When the image is successfully loaded
        void onError(String error);                    // When there's an error
    }
}