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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import ca.yapper.yapperapp.UMLClasses.Event;

public class OrganizerDatabase {

    private static final FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    public interface OnUserCheckListener {
        void onUserInList(boolean inList);
        void onError(String error);
    }

    public interface OnWaitListCountLoadedListener {
        void onCountLoaded(int waitListCount);
        void onError(String errorMessage);
    }

    public interface OnOperationCompleteListener {
        void onComplete(boolean success);
    }

    public interface OnEventCapLoadedListener {
        void onCapacityLoaded(int capacity);
        void onError(String errorMessage);
    }

    public interface OnOrganizerDetailsLoadedListener {
        void onOrganizerLoaded(String organizerName);
        void onError(String error);
    }

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

    public interface OnEventsLoadedListener {
        void onEventsLoaded(List<String> eventIds);
        void onEventsLoadError(String error);
    }

    public interface OnFacilityDataLoadedListener {
        void onFacilityDataLoaded(String facilityName, String location);  // When the data is successfully loaded
        void onError(String error);                                        // When there's an error
    }

    public interface OnSelectedListLoadCountListener {
        void onSelectedListCountLoaded(int selectedListCount);
        void onError(String error);
    }

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

    public static void getEventDetails(String eventId, OnEventDetailsFetchListener listener) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                        listener.onEventDetailsFetched(documentSnapshot.getString("name"));
                    } else {
                        listener.onError("Event name not found.");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error fetching event details: " + e.getMessage()));
    }

    // Define the callback interface:
    public interface OnEventDetailsFetchListener {
        void onEventDetailsFetched(String eventName);
        void onError(String errorMessage);
    }

    public static void loadEventFromDatabase(String eventId, OnEventLoadedListener listener) {
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
                            event.setDocumentId(documentSnapshot.getId());

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

    public static void checkUserInEvent(String eventId, String userId, OnUserCheckListener listener) {


        db.collection("Events").document(eventId).collection("waitingList").document(userId).get()
                .addOnSuccessListener(waitingListDoc -> {
                    if (waitingListDoc.exists()) {
                        Log.i("OrganizerDatabase", "checkUserInEvent: waitingListDoc exists!");
                        Log.i("OrganizerDatabase", "checkUserInEvent: waitingListDoc exists!");
                        listener.onUserInList(true);
                        Log.i("OrganizerDatabase", "checkUserInEvent: calling listener (onUserInList(true))!");
                        Log.i("OrganizerDatabase", "checkUserInEvent: calling listener (onUserInList(true))!");
                    }
                    else {
                        // Check selectedList only if not found in waitingList
                        Log.i("OrganizerDatabase", "checkUserInEvent: waitingListDoc does NOT exist!");
                        Log.i("OrganizerDatabase", "checkUserInEvent: waitingListDoc does NOT exist!");
                        db.collection("Events").document(eventId).collection("selectedList").document(userId).get()
                                .addOnSuccessListener(selectedListDoc -> {
                                    listener.onUserInList(selectedListDoc.exists()); // User found in selectedList or not at all
                                    Log.i("OrganizerDatabase", "checkUserInEvent: calling listener (selectedListDoc.exists())!");
                                    Log.i("OrganizerDatabase", "checkUserInEvent: calling listener (selectedListDoc.exists())!");
                                })
                                .addOnFailureListener(e -> listener.onError(e.getMessage()));
                        Log.i("OrganizerDatabase", "checkUserInEvent: user not in waiting or selected list -> can join event!");
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
                                        // String deviceId = userDoc.getString("deviceId");
                                        String deviceId = userIdRef;

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
    }

    public static void getInvitationStatusFromEvent(String eventId, String userId, OnStatusCheckListener listener) {
        DocumentReference userDocRef = db.collection("Events")
                .document(eventId)
                .collection("selectedList")
                .document(userId);

        userDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean invitationStatus = documentSnapshot.getBoolean("invitationStatus");
                        if (invitationStatus != null) {
                            listener.onStatusLoaded(invitationStatus);  // True for accepted, false for rejected
                        } else {
                            listener.onStatusNotFound(); // Invitation status missing
                        }
                    } else {
                        listener.onUserNotInList();  // User not in the selected list
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error fetching status: " + e.getMessage()));
    }

    // Define callback interface
    public interface OnStatusCheckListener {
        void onStatusLoaded(boolean accepted);  // True if accepted, false if rejected
        void onStatusNotFound();               // Field missing, default to pending
        void onUserNotInList();                // User not in selected list
        void onError(String error);            // Any error occurred
    }

    public static void changeUserStatusForEvent(String eventId, String userId, String status, OnOperationCompleteListener listener) {
        // change the joinedEvents subcollection field invitationStatus
        // Get reference to the user's document in the "Users" collection
        DocumentReference userDocRef = db.collection("Users").document(userId);
        CollectionReference joinedEventsRef = userDocRef.collection("joinedEvents");
        DocumentReference eventDocRef = joinedEventsRef.document(eventId);

        eventDocRef.set(new HashMap<String, Object>() {{
                    put("invitationStatus", status);
                }}, SetOptions.merge())  // Use SetOptions.merge() to only update the status field
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Field added/updated successfully.");
                    // Notify the listener about the success
                    if (listener != null) {
                        listener.onComplete(true);  // Operation was successful
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating field", e);
                    // Notify the listener about the failure
                    if (listener != null) {
                        listener.onComplete(false);  // Operation failed
                    }
                });
    }

    public static void moveUserBetweenEventSubcollections(String eventId, String userId, String fromSubcollection, String toSubcollection, OnOperationCompleteListener listener) {
        // References to the relevant documents
        DocumentReference fromDocRef = db.collection("Events")
                .document(eventId)
                .collection(fromSubcollection)
                .document(userId);

        DocumentReference toDocRef = db.collection("Events")
                .document(eventId)
                .collection(toSubcollection)
                .document(userId);

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
                    Log.d("OrganizerDatabase", "Successfully moved user: " + userId + " from " + fromSubcollection + " to " + toSubcollection);
                }).addOnFailureListener(e -> {
                    Log.e("OrganizerDatabase", "Error moving user: " + e.getMessage());
                });
            } else {
                Log.e("OrganizerDatabase", "User not found in " + fromSubcollection);
            }
        }).addOnFailureListener(e -> {
            Log.e("OrganizerDatabase", "Error retrieving user: " + e.getMessage());
        });
    }

    public static void loadEventCapacity(String eventId, OnEventCapLoadedListener listener) {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int capacity = documentSnapshot.getLong("capacity").intValue();
                        Log.i("loadEventCapacity", "Event capacity loaded successfully: " + capacity);
                        listener.onCapacityLoaded(capacity);
                    }
                    else {
                        Log.e("loadEventCapacity", "Document does not exist for eventId: " + eventId); }
                })
                .addOnFailureListener(e -> listener.onError("Error loading event: " + e.getMessage()));
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
    public static void createEventInDatabase(int capacity, String dateTime, String description,
                                             String facilityLocation, String facilityName, boolean isGeolocationEnabled,
                                             String name, String registrationDeadline, Integer waitListCapacity,
                                             String organizerId, String posterBase64, OnOperationCompleteListener listener) {
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

    private static Task<Void> addEventToAllUsersMissedOut(String eventId) {
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
    //is this redundant?
    public static void moveUserToSelectedList(String eventId, String userId, OnOperationCompleteListener listener) {
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", FieldValue.serverTimestamp());
        details.put("invitationStatus", "Pending");  // initial invite status is set to pending

        DocumentReference waitingListRef = db.collection("Events").document(eventId)
                .collection("waitingList").document(userId);
        DocumentReference selectedListRef = db.collection("Events").document(eventId)
                .collection("selectedList").document(userId);

        db.runTransaction(transaction -> {
                    transaction.delete(waitingListRef);
                    transaction.set(selectedListRef, details);
                    return null;
                }).addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public static void getSelectedListCount(String eventId, OnSelectedListLoadCountListener listener) {
        db.collection("Events").document(eventId)
                .collection("selectedList").get()
                .addOnSuccessListener(snapshot -> {
                    listener.onSelectedListCountLoaded(snapshot.size());
                })
                .addOnFailureListener(e -> listener.onError("Error retrieving selected list count data: " + e.getMessage()));
    }

    public static void sendNotificationToUser(String eventId, String userId, String userFromId, String message, OnOperationCompleteListener listener) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("OrganizerDatabase", "Event ID is null or empty. Cannot send notification.");
            listener.onComplete(false);
            return;
        }
        if (userId == null || userId.isEmpty()) {
            Log.e("OrganizerDatabase", "User ID is null or empty. Cannot send notification.");
            listener.onComplete(false);
            return;
        }
        if (message == null || message.isEmpty()) {
            Log.e("OrganizerDatabase", "Message is null or empty. Cannot send notification.");
            listener.onComplete(false);
            return;
        }

        // Create the notification data
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("dateTimeStamp", FieldValue.serverTimestamp());
        notificationData.put("userToId", userId);
        notificationData.put("userFromId", userFromId); // Assuming no specific sender
        notificationData.put("title", "Custom Notification");
        notificationData.put("message", message);
        notificationData.put("notificationType", "CustomNotification");
        notificationData.put("eventId", eventId);
        notificationData.put("isRead", false);

        // Save the notification to Firestore
        db.collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("OrganizerDatabase", "Notification sent successfully with ID: " + documentReference.getId());
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerDatabase", "Failed to send notification: " + e.getMessage());
                    listener.onComplete(false);
                });
    }

    public static void getWaitingListCount(String eventId, OnWaitListCountLoadedListener listener) {
        db.collection("Events").document(eventId)
                .collection("waitingList").get()
                .addOnSuccessListener(snapshot -> {
                    listener.onCountLoaded(snapshot.size());
                })
                .addOnFailureListener(e -> listener.onError("Error retrieving wait list count data: " + e.getMessage()));    }

    public static void loadCreatedEvents(String userDeviceId, OnEventsLoadedListener listener) {
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

    public static void loadFacilityData(String deviceId, final OnFacilityDataLoadedListener listener) {
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

    public static void loadOrganizerData(String organizerId, OnOrganizerDetailsLoadedListener listener) {
        db.collection("Users").document(organizerId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Retrieve the organizerName
                        String organizerName = document.getString("entrantName");

                        // Check if the data is available
                        if (organizerName != null) {
                            listener.onOrganizerLoaded(organizerName); // Send the data to the listener
                        } else {
                            listener.onError("Organizer data not found");
                        }
                    } else {
                        listener.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error retrieving organizer data: " + e.getMessage()));
    }

    public static void saveEventData(String eventId, Map<String, Object> eventData, OnOperationCompleteListener listener) {
        db.collection("Events").document(eventId).set(eventData)
                .addOnSuccessListener(unused -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public static Task<Void> updateFacilityNameForEvents(String userId, String facilityName) {
        WriteBatch batch = db.batch();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("facilityName", facilityName);

        return db.collection("Users").document(userId).collection("createdEvents").get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot eventDoc : task.getResult()) {
                            String eventId = eventDoc.getId();
                            batch.update(db.collection("Events").document(eventId), userUpdates);
                        }
                        batch.update(db.collection("Users").document(userId), userUpdates);
                        return batch.commit();
                    }
                    throw task.getException();
                });
    }

    public static Task<Void> updateFacilityAddressForEvents(String userId, String facilityLocation) {
        WriteBatch batch = db.batch();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("facilityAddress", facilityLocation);

        return db.collection("Users").document(userId).collection("createdEvents").get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot eventDoc : task.getResult()) {
                            String eventId = eventDoc.getId();
                            batch.update(db.collection("Events").document(eventId), userUpdates);
                        }
                        batch.update(db.collection("Users").document(userId), userUpdates);
                        return batch.commit();
                    }
                    throw task.getException();
                });
    }

    public static void addUserToFinalList(String eventId, String userId, OnOperationCompleteListener listener) {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("OrganizerDatabase", "Event ID is null or empty.");
            listener.onComplete(false);
            return;
        }
        if (userId == null || userId.isEmpty()) {
            Log.e("OrganizerDatabase", "User ID is null or empty.");
            listener.onComplete(false);
            return;
        }

        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Events").document(eventId).collection("finalList").document(userId)
                .set(timestamp)
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrganizerDatabase", "User added to final list.");
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerDatabase", "Error adding user to final list.", e);
                    listener.onComplete(false);
                });
    }
    // redundant(?):
    public static void addUserToCancelledList(String eventId, String userId, OnOperationCompleteListener listener) {
        // Create a timestamp for the cancelled list entry
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        // Add the user to the cancelled list
        db.collection("Events").document(eventId).collection("cancelledList").document(userId)
                .set(timestamp)
                .addOnSuccessListener(aVoid -> {
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    listener.onComplete(false);
                });
    }

    public static void isPendingStatusForUser(String userId, String eventId, OnIsPendingStatusCheckedListener listener) {
        // Check the joinedEvents subcollection for the user and eventId
        db.collection("Events")
                .document(eventId)
                .collection("selectedList")
                .document(userId)  // Make sure you have the eventId
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean isPending;
                        String invitationStatus = documentSnapshot.getString("invitationStatus");
                        if (Objects.equals(invitationStatus, "Pending")) {
                            isPending = true;
                        }
                        else {
                            isPending = false;
                        }
                        listener.onStatusLoaded(isPending);
                    }
                })
                .addOnFailureListener(e -> listener.onError("Error loading pending status check"));  // Handle errors
    }

    // Define the listener interface to handle the invitation status
    public interface OnIsPendingStatusCheckedListener {
        void onStatusLoaded(boolean isPending);
        void onError(String error);
    }

    // Define callback interface
    public interface OnUserStatusCheckListener {
        void onStatusLoaded(String status);  // "Pending", "Accepted", "Rejected", etc.
        void onStatusNotFound();             // If status is missing
        void onUserNotInList();              // If event is not in joinedEvents
        void onError(String error);          // Error handling
    }

    public static void updateInvitationStatus(String userId, String eventId, String newStatus, OnOperationCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocRef = db.collection("Events")
                .document(eventId)
                .collection("selectedList")
                .document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("invitationStatus", newStatus);

        eventDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("OrganizerDatabase", "Invitation status updated to: " + newStatus);
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("OrganizerDatabase", "Error updating invitation status: " + e.getMessage());
                    listener.onComplete(false);
                });
    }

    public static void getInvitationStatus(String userId, String eventId, OnUserStatusCheckListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference eventDocRef = db.collection("Events")
                .document(eventId)
                .collection("selectedList")
                .document(userId);

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

    public static Task<Boolean> checkQRCodeExists(String eventId) {
        return db.collection("Events")
                .document(eventId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().get("qrCode_hashData") != null;
                    }
                    return false;
                });
    }
}