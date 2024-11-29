package ca.yapper.yapperapp.Databases;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ca.yapper.yapperapp.UMLClasses.Event;

/**
 * Class holding all the organizer related functions that interact with the database.
 */
public class OrganizerDatabase {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Interface for dealing with user checking and associated error handling
     */
    public interface OnUserCheckListener {
        void onUserInList(boolean inList);
        void onError(String error);
    }

    /**
     * Interface for data fetching and associated errors
     */
    public interface OnDataFetchListener<T> {
        void onFetch(T data);
        void onError(Exception e);
    }

    /**
     * Interface for loading wait list counts and associated errors
     */
    public interface OnWaitListCountLoadedListener {
        void onCountLoaded(int waitListCount);
        void onError(String errorMessage);
    }

    /**
     * Interface for completing operations associated error handling
     */
    public interface OnOperationCompleteListener {
        void onComplete(boolean success);
    }

    /**
     * Interface for handling results from loading event capacities and associated error handling
     */
    public interface OnEventCapLoadedListener {
        void onCapacityLoaded(int capacity);
        void onError(String errorMessage);
    }

    /**
     * Interface for handling the result of loading an event from Firestore.
     */
    public interface OnEventLoadedListener {
        void onEventLoaded(Event event);
        void onEventLoadError(String error);
    }

    /**
     * Interface for handling results from loading user Ids and associated error handling
     */
    public interface OnUserIdsLoadedListener {
        void onUserIdsLoaded(ArrayList<String> userIdsList);

        void onError(String error);
    }

    /**
     * Interface for handling results from loading events and associated error handling
     */
    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<String> eventIds);
        void onEventsLoadError(String error);
    }

    /**
     * Interface for handling results from loading facility data and associated error handling
     */
    public interface OnFacilityDataLoadedListener {
        void onFacilityDataLoaded(String facilityName, String location);  // When the data is successfully loaded
        void onError(String error);                                        // When there's an error
    }


    /**
     * Checks if the user is an admin based on their device ID.
     *
     * @param deviceId The device ID of the user.
     * @return A Task that resolves to true if the user is an admin, false otherwise.
     */
    public static Task<Boolean> checkIfUserIsAdmin(String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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


    /**
     * This function loads an event from the database using the eventId
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param listener handles the outcome of the event loading operation
     */
    public static void loadEventFromDatabase(String eventId, OnEventLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Events").document(eventId).get()
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
                                    documentSnapshot.getString("organizerId"),
                                    documentSnapshot.getString("registrationDeadline"),
                                    documentSnapshot.getLong("waitListCapacity") != null ? documentSnapshot.getLong("waitListCapacity").intValue() : null,
                                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
                            );

                            // Check if posterBase64 exists
                            String posterBase64 = documentSnapshot.getString("posterBase64");
                            if (posterBase64 != null) {
                                event.setPosterBase64(posterBase64);
                            }

                            listener.onEventLoaded(event);
                        } catch (Exception e) {
                            listener.onEventLoadError("Error creating event: " + e.getMessage());
                        }
                    } else {
                        listener.onEventLoadError("Event not found.");
                    }
                })
                .addOnFailureListener(e -> listener.onEventLoadError("Error loading event: " + e.getMessage()));
    }


    /**
     * This function checks if a user is in an events selected or waiting list, by iterating through
     * both lists.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param userId The id for the user, created from the device id.
     * @param listener handles the outcome of the user check
     */
    public static void checkUserInEvent(String eventId, String userId, OnUserCheckListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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


    /**
     * This function gets users from a given subcollection and obtains their device IDs from the
     * users collection, then adds them to a device Id list.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param subcollectionName name of the specified subcollection we want to load user from
     * @param listener handles the outcome of loading the user Ids
     */
    public static void loadUserIdsFromSubcollection(String eventId, String subcollectionName, OnUserIdsLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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


    /**
     * This function obtains the event capacity for a given event.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param listener handles the outcome of loading the event capacity
     */
    public static void loadEventCapacity(String eventId, OnEventCapLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int capacity = documentSnapshot.getLong("capacity").intValue();
                        Log.i("loadEventCapacity", "Event capacity loaded successfully: " + capacity);
                        listener.onCapacityLoaded(capacity);
                            }
                    else {
                        Log.e("loadEventCapacity", "Document does not exist for eventId: " + eventId);
                        listener.onCapacityLoaded(0); // Default if document does not exist
                    }
                });
    }


    /**
     * This function creates and saves a new event in Firestore with the provided details.
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
    public static void createEventInDatabase(int capacity, String dateTime, String description,
                                             String facilityLocation, String facilityName, boolean isGeolocationEnabled,
                                             String name, String registrationDeadline, Integer waitListCapacity,
                                             String organizerId, String posterBase64, OnOperationCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            // Create the Event object
            Event event = new Event(
                    capacity, dateTime, description, facilityLocation, facilityName,
                    isGeolocationEnabled, name, organizerId, registrationDeadline, waitListCapacity,
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
            );

            // Generate the eventId using the QR code hash
            String eventId = Integer.toString(event.getQRCode().getHashData());

            // can delete / update later (add event to user's missed out events list)
            addEventToAllUsersMissedOut(eventId);

            // Build the event data map
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("capacity", capacity);
            eventData.put("date_Time", dateTime);
            eventData.put("description", description);
            eventData.put("facilityLocation", facilityLocation);
            eventData.put("facilityName", facilityName);
            eventData.put("isGeolocationEnabled", isGeolocationEnabled);
            eventData.put("organizerId", organizerId);
            eventData.put("name", name);
            eventData.put("qrCode_hashData", event.getQRCode().getHashData());
            eventData.put("registrationDeadline", registrationDeadline);
            eventData.put("waitListCapacity", waitListCapacity);

            if (posterBase64 != null) {
                eventData.put("posterBase64", posterBase64); // Add poster image if present
            }

            // Save the event to Firestore
            db.collection("Events").document(eventId).set(eventData)
                    .addOnSuccessListener(unused -> {
                        // Add a reference to the organizer's created events
                        Map<String, Object> eventRef = new HashMap<>();
                        eventRef.put("timestamp", FieldValue.serverTimestamp());
                        db.collection("Users").document(organizerId).collection("createdEvents")
                                .document(eventId).set(eventRef)
                                .addOnSuccessListener(unused1 -> listener.onComplete(true))
                                .addOnFailureListener(e -> listener.onComplete(false));
                    })
                    .addOnFailureListener(e -> listener.onComplete(false));
        } catch (Exception e) {
            listener.onComplete(false);
        }
    }


    /**
     * This function adds a given event to each users "Missed Out" events collection.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @return a task that return complete when all users have had the event added to the sub collection. Otherwise fails.
     */
    private static Task<Void> addEventToAllUsersMissedOut(String eventId) {
        FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();
        CollectionReference usersRef = db.collection("Users");

        return usersRef.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                List<Task<Void>> tasks = new ArrayList<>();
                for (DocumentSnapshot userDoc : task.getResult().getDocuments()) {
                    String userId = userDoc.getId();
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("eventId", eventId);
                    tasks.add(usersRef.document(userId).collection("missedOutEvents").document(eventId).set(eventData));
                }
                return Tasks.whenAll(tasks);
            } else {
                throw task.getException();
            }
        });
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


    /**
     * This function moves a given user from an events waiting list to its selected list.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param userId The id for the user, created from the device id.
     * @param listener handles the outcome of the operation
     */
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


    /**
     * This function moves a given user from an events selected list to its waiting list.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param userId The id for the user, created from the device id.
     * @param listener handles the outcome of the operation
     */
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


    /**
     * This function gets the total number of users in the selected list for a given event
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param listener handles the outcome of the data fetch
     */
    public static void getSelectedListCount(String eventId, OnDataFetchListener<Integer> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId)
                .collection("selectedList").get()
                .addOnSuccessListener(snapshot -> {
                    listener.onFetch(snapshot.size());
                })
                .addOnFailureListener(listener::onError);
    }


    /**
     * This function gets the total number of users in the waiting list for a given event
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param listener handles the outcome of the data fetch
     */
    public static void getWaitingListCount(String eventId, OnWaitListCountLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId)
                .collection("waitingList").get()
                .addOnSuccessListener(snapshot -> {
                    listener.onCountLoaded(snapshot.size());
                })
                .addOnFailureListener(e -> listener.onError("Error retrieving wait list count data: " + e.getMessage()));    }


    /**
     * This function loads all of a users created events.
     *
     * @param userDeviceId The device ID of the user.
     * @param listener handles the outcome of the event loading
     */
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


    /**
     * This functions loads a given users facility name and location.
     *
     * @param deviceId The Id for users device.
     * @param listener handles the outcome of the facility loading
     */
    public static void loadFacilityData(String deviceId, final OnFacilityDataLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Retrieve the facilityName and location fields
                        String facilityName = document.getString("facilityName");
                        String location = document.getString("facilityAddress");

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

    /**
     * This function saves event data to the events collection in the database.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param eventData a map with event data
     * @param listener handles the outcome of the operation
     */
    public static void saveEventData(String eventId, Map<String, Object> eventData, OnOperationCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Events").document(eventId).set(eventData)
                .addOnSuccessListener(unused -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }
}
