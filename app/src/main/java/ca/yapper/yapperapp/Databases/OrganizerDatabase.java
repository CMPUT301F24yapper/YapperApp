package ca.yapper.yapperapp.Databases;

import android.graphics.Color;
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
import java.util.Map;

import ca.yapper.yapperapp.UMLClasses.Event;

public class OrganizerDatabase {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Checks if the user is an admin based on their device ID.
     *
     * @param deviceId The unique device ID of the user.
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
                .addOnSuccessListener(document -> listener.onUserInList(document.exists()))
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
        batch.set(eventWaitingListRef, entrantData);

        // Add to user's joined events
        DocumentReference userJoinedEventsRef = db.collection("Users")
                .document(userId)
                .collection("joinedEvents")
                .document(eventId);
        batch.set(userJoinedEventsRef, entrantData);

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    joinButton.setText("Unjoin");
                    joinButton.setBackgroundColor(Color.GRAY);
                    Toast.makeText(getContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error joining the event. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    public static void unjoinEvent(String eventId, String userId, OnOperationCompleteListener listener) {
        // Implement unjoin event logic using Firestore batch writes
    }

    public interface OnUserCheckListener {
        void onUserInList(boolean inList);
        void onError(String error);
    }

    public interface OnOperationCompleteListener {
        void onComplete(boolean success);
    }

    public static void loadUserIdsFromSubcollection(FirebaseFirestore db, String eventId, String subcollectionName, OnUserIdsLoadedListener listener) {
        ArrayList<String> userIdsList = new ArrayList<>();

        db.collection("Events").document(eventId).collection(subcollectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
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
                });
    }

    public interface OnUserIdsLoadedListener {
        void onUserIdsLoaded(ArrayList<String> userIdsList);
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
        Map<String, Object> placeholderData = new HashMap<>();
        placeholderData.put("placeholder", true);

        db.collection("Events").document(eventId).collection("waitingList").add(placeholderData);
        db.collection("Events").document(eventId).collection("selectedList").add(placeholderData);
        db.collection("Events").document(eventId).collection("finalList").add(placeholderData);
        db.collection("Events").document(eventId).collection("cancelledList").add(placeholderData);
    }

    /**
     * Interface for handling the result of loading an event from Firestore.
     */
    public interface OnEventLoadedListener {
        void onEventLoaded(Event event);
        void onEventLoadError(String error);
    }

}
