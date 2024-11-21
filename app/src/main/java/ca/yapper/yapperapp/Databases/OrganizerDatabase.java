package ca.yapper.yapperapp.Databases;

import android.graphics.Color;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ca.yapper.yapperapp.UMLClasses.Event;

public class OrganizerDatabase {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    static int eventCapacity;

    public interface OnUserCheckListener {
        void onUserInList(boolean inList);
        void onError(String error);
    }

    public interface OnDataFetchListener<T> {
        void onFetch(T data);
        void onError(Exception e);
    }

    public interface OnOperationCompleteListener {
        void onComplete(boolean success);
    }

    /**
     * Checks if the user is an admin based on their device ID.
     *
     * @param deviceId The unique device ID of the user.
     * @return A Task that resolves to true if the user is an admin, false otherwise.
     */

    /**
     * Interface for handling the result of loading an event from Firestore.
     */
    public interface OnEventLoadedListener {
        void onEventLoaded(Event event);
        void onEventLoadError(String error);
    }

    public interface OnUserIdsLoadedListener {
        void onUserIdsLoaded(ArrayList<String> userIdsList);

        void onError(String error);
    }

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

    public static void loadEventFromDatabase(String hashData, OnEventLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(hashData).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Event event = new Event(
                                    documentSnapshot.getLong("capacity").intValue(),
                                    documentSnapshot.getString("date_Time"),
                                    documentSnapshot.getString("description"),
                                    documentSnapshot.getString("facilityLocation"),
                                    documentSnapshot.getString("facilityName"),
                                    documentSnapshot.getBoolean("isGeolocationEnabled"),
                                    documentSnapshot.getString("name"),
                                    documentSnapshot.getString("registrationDeadline"),
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

    public static void checkUserInEvent(String eventId, String userId, OnUserCheckListener listener) {
        db.collection("Events").document(eventId).collection("waitingList").document(userId).get()
                .addOnSuccessListener(waitingListDoc -> {
                    if (waitingListDoc.exists()) {
                        listener.onUserInList(true);
                    }
                    else {
                        // Check selectedList only if not found in waitingList
                        db.collection("Events").document(eventId).collection("selectedList").document(userId).get()
                                .addOnSuccessListener(selectedListDoc -> {
                                    listener.onUserInList(selectedListDoc.exists()); // User found in selectedList or not at all
                                })
                                .addOnFailureListener(e -> listener.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public static void loadUserIdsFromSubcollection(String eventId, String subcollectionName, OnUserIdsLoadedListener listener) {
        ArrayList<String> userIdsList = new ArrayList<>();

        db.collection("Events").document(eventId).collection(subcollectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onUserIdsLoaded(userIdsList);
                        return;
                    }
                    int totalDocs = queryDocumentSnapshots.size();
                    AtomicInteger processedDocs = new AtomicInteger(0);

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String userIdRef = doc.getId();

                        // Skip invalid placeholder documents by checking the `Users` collection
                        db.collection("Users").document(userIdRef).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String deviceId = userDoc.getString("deviceId");
                                        if (deviceId != null) {
                                            userIdsList.add(deviceId);
                                        }
                                    }
                                    // Check if all documents have been processed
                                    if (processedDocs.incrementAndGet() == totalDocs) {
                                        listener.onUserIdsLoaded(userIdsList);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (processedDocs.incrementAndGet() == totalDocs) {
                                        listener.onUserIdsLoaded(userIdsList);
                                    }
                                });
                    } });
                    /**for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String userIdRef = doc.getId();
                        db.collection("Users").document(userIdRef).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String deviceId = userDoc.getString("deviceId");
                                        if (deviceId != null) {
                                            userIdsList.add(deviceId);
                                        }
                                    }

                                    // Call the listener only after all documents are processed
                                    if (userIdsList.size() == queryDocumentSnapshots.size()) {
                                        listener.onUserIdsLoaded(userIdsList);
                                    }
                                });
                    }

                    // Handle case when there are no documents in subcollection
                    if (queryDocumentSnapshots.isEmpty()) {
                        listener.onUserIdsLoaded(userIdsList);
                    }
                });**/
    }

    public static void loadEventCapacity(String eventId) {

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        eventCapacity = documentSnapshot.getLong("capacity").intValue();
                    }
                    else {
                        // implement error logic
                    }
                });
    }


    /**
     * Creates and saves a new event in Firestore with the provided details.
     *
     * @param capacity The maximum number of attendees.
     * @param dateTime The event date and time.
     * @param description The description of the event.
     * @param facilityLocation The location of the event.
     * @param facilityName The name of the facility.
     * @param isGeolocationEnabled Whether geolocation is enabled for the event.
     * @param name The name of the event.
     * @param registrationDeadline The registration deadline for the event.
     * @param waitListCapacity Capacity of the waiting list.
     * @param organizerId The ID of the organizer creating the event.
     * @return The created Event instance.
     */
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
            eventData.put("capacity", event.getCapacity());
            eventData.put("date_Time", event.getDate_Time());
            eventData.put("description", event.getDescription());
            eventData.put("facilityLocation", event.getFacilityLocation());
            eventData.put("facilityName", event.getFacilityName());
            eventData.put("isGeolocationEnabled", event.isGeolocationEnabled());
            eventData.put("name", event.getName());
            eventData.put("qrCode_hashData", event.getQRCode().getHashData());
            eventData.put("registrationDeadline", event.getRegistrationDeadline());
            eventData.put("waitListCapacity", event.getWaitListCapacity());
            eventData.put("organizerId", organizerId);

            // Initialize the subcollections with placeholder data
            initializeSubcollections(db, eventId);

            db.collection("Events").document(eventId).set(eventData);

            Map<String, Object> eventRef = new HashMap<>();
            eventRef.put("timestamp", com.google.firebase.Timestamp.now());
            db.collection("Users").document(organizerId).collection("createdEvents").document(eventId).set(eventRef);

            return event;
        } catch (WriterException e) {
            return null;
        }
    }

    /**
     * Initializes Firestore subcollections for the event with placeholder data.
     *
     * @param db Firestore instance.
     * @param eventId The unique ID of the event.
     */
    public static void initializeSubcollections(FirebaseFirestore db, String eventId) {
        // Empty implementation - no placeholder data added
        // Subcollections will be implicitly created when documents are added later
        /**Map<String, Object> placeholderData = new HashMap<>();
        placeholderData.put("placeholder", true);

        db.collection("Events").document(eventId).collection("waitingList").add(placeholderData);
        db.collection("Events").document(eventId).collection("selectedList").add(placeholderData);
        db.collection("Events").document(eventId).collection("finalList").add(placeholderData);
        db.collection("Events").document(eventId).collection("cancelledList").add(placeholderData);**/
    }


    public static void moveUserToSelectedList(String eventId, String userId, OnOperationCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        DocumentReference waitingListRef = db.collection("Events").document(eventId)
                .collection("waitingList").document(userId);
        DocumentReference selectedListRef = db.collection("Events").document(eventId)
                .collection("selectedList").document(userId);

        db.runTransaction(transaction -> {
                    transaction.delete(waitingListRef);
                    transaction.set(selectedListRef, timestamp);
                    return null;
                }).addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public static void moveUserToWaitingList(String eventId, String userId, OnOperationCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        DocumentReference selectedListRef = db.collection("Events").document(eventId)
                .collection("selectedList").document(userId);
        DocumentReference waitingListRef = db.collection("Events").document(eventId)
                .collection("waitingList").document(userId);

        db.runTransaction(transaction -> {
                    transaction.delete(selectedListRef);
                    transaction.set(waitingListRef, timestamp);
                    return null;
                }).addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public static void getSelectedListCount(String eventId, OnDataFetchListener<Integer> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId)
                .collection("selectedList").get()
                .addOnSuccessListener(snapshot -> {
                    listener.onFetch(snapshot.size());
                })
                .addOnFailureListener(listener::onError);
    }

    public static void loadCreatedEvents(String userDeviceId, OnEventsLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (userDeviceId == null) {
            listener.onEventsLoadError("Error: Unable to get user ID");
            return;
        }

        db.collection("Users")
                .document(userDeviceId)
                .collection("createdEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        eventIds.add(document.getId());
                    }
                    listener.onEventsLoaded(eventIds);
                })
                .addOnFailureListener(e -> {
                    listener.onEventsLoadError("Error loading events: " + e.getMessage());
                });
    }

    // Define an interface for callback
    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<String> eventIds);
        void onEventsLoadError(String error);
    }

    public static void loadFacilityData(String deviceId, final OnFacilityDataLoadedListener listener) {
        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Retrieve the facilityName and location fields
                        String facilityName = document.getString("facilityName");
                        String location = document.getString("location");

                        // Check if the data is available
                        if (facilityName != null && location != null) {
                            listener.onFacilityDataLoaded(facilityName, location); // Send the data to the listener
                        } else {
                            listener.onError("Facility data not found");
                        }
                    } else {
                        listener.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error retrieving facility data: " + e.getMessage()));
    }

    // Define an interface to handle the result when the facility data is loaded
    public interface OnFacilityDataLoadedListener {
        void onFacilityDataLoaded(String facilityName, String location);  // When the data is successfully loaded
        void onError(String error);                                        // When there's an error
    }

}
