package ca.yapper.yapperapp.Databases;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.AdminImageAdapter.ImageData;

/**
 * Class holding all the admin related functions that interact with the database.
 */
public class AdminDatabase {
    
    private static  FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    public static void setFirestoreInstance(FirebaseFirestore firestore) {
        db = firestore;
    }


    /**
     * This function obtains statistics from the database(used in the admin home page).
     * The statistics gathered are the total number of events, users and organizers/facilities.
     *
     * @return a task with a map of the obtained statistics mentioned above.
     */
    public static Task<Map<String, Long>> getAdminStats() {
        TaskCompletionSource<Map<String, Long>> tcs = new TaskCompletionSource<>();
        Map<String, Long> stats = new HashMap<>();

        db.collection("Events").get().addOnSuccessListener(eventSnapshot -> {
            stats.put("totalEvents", (long) eventSnapshot.size());

            db.collection("Users").get().addOnSuccessListener(userSnapshot -> {
                long organizerCount = 0;
                for (DocumentSnapshot doc : userSnapshot.getDocuments()) {
                    String facilityName = doc.getString("facilityName");
                    String facilityAddress = doc.getString("facilityAddress");
                    if (facilityName != null && !facilityName.isEmpty() &&
                            facilityAddress != null && !facilityAddress.isEmpty()) {
                        organizerCount++;
                    }
                }
                stats.put("totalUsers", (long) userSnapshot.size());
                stats.put("totalOrganizers", organizerCount);
                tcs.setResult(stats);
            });
        });

        return tcs.getTask();
    }


    /**
     * This function obtains the five largest events from the database, based on total users.
     * It does this by adding up the counts from waiting, selected and cancelled lists. Then the
     * events are ordered from largest to smallest.
     *
     * @return a task with a list of the five largest events as maps. Each map contains the name
     * and count for the event.
     */
    public static Task<List<Map<String, Object>>> getBiggestEvents() {
        TaskCompletionSource<List<Map<String, Object>>> tcs = new TaskCompletionSource<>();

        db.collection("Events").get().addOnSuccessListener(querySnapshot -> {
            List<Map<String, Object>> eventList = new ArrayList<>();
            AtomicInteger pendingEvents = new AtomicInteger(querySnapshot.size());

            querySnapshot.forEach(doc -> {
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("name", doc.getString("name"));
                String eventId = doc.getId();

                Task<Integer> waitingCountTask = db.collection("Events").document(eventId)
                        .collection("waitingList").get().continueWith(task -> task.getResult().size());

                Task<Integer> selectedCountTask = db.collection("Events").document(eventId)
                        .collection("selectedList").get().continueWith(task -> task.getResult().size());

                Task<Integer> cancelledCountTask = db.collection("Events").document(eventId)
                        .collection("cancelledList").get().continueWith(task -> task.getResult().size());

                Tasks.whenAllSuccess(waitingCountTask, selectedCountTask, cancelledCountTask)
                        .addOnSuccessListener(results -> {
                            int totalCount = (Integer)results.get(0) + (Integer)results.get(1) + (Integer)results.get(2);
                            eventMap.put("capacity", (long)totalCount);
                            eventList.add(eventMap);

                            if (pendingEvents.decrementAndGet() == 0) {
                                eventList.sort((e1, e2) -> {
                                    Long cap1 = (Long) e1.get("capacity");
                                    Long cap2 = (Long) e2.get("capacity");
                                    return cap2.compareTo(cap1);
                                });

                                List<Map<String, Object>> topEvents = eventList.subList(0, Math.min(5, eventList.size()));
                                tcs.setResult(topEvents);
                            }
                        });
            });

            // Handle case when there are no events
            if (querySnapshot.isEmpty()) {
                tcs.setResult(new ArrayList<>());
            }
        });

        return tcs.getTask();
    }


    /**
     * This function obtains all the events from the database and turns them into event objects.
     * It does this by iterating through the events from the event collection in the database,
     * assigning the information obtained, then storing the event into a list. If an obtained value
     * is invalid, then its skipped.
     *
     * @return a database call that returns a list of events
     */
    public static Task<List<Event>> getAllEvents() {
        return db
                .collection("Events")
                .get()
                .continueWith(task -> {
                    List<Event> events = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            // Extract fields directly from document
                            String eventName = doc.getString("name");
                            String dateTime = doc.getString("date_Time");
                            String description = doc.getString("description");
                            String facilityLocation = doc.getString("facilityLocation");
                            boolean isGeolocationEnabled = doc.getBoolean("isGeolocationEnabled");
                            String facilityName = doc.getString("facilityName");
                            String organizerId = doc.getString("organizerId");
                            String registrationDeadline = doc.getString("registrationDeadline");
                            int capacity = doc.getLong("capacity").intValue();
                            int waitListCapacity = doc.getLong("waitListCapacity").intValue();

                            try {
                                Event event = new Event(
                                        capacity,
                                        dateTime,
                                        description,
                                        facilityLocation,
                                        facilityName,
                                        isGeolocationEnabled,
                                        eventName,
                                        organizerId,
                                        registrationDeadline,
                                        waitListCapacity,
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new ArrayList<>()
                                );
                                event.setDocumentId(doc.getId());
                                events.add(event);
                            } catch (Exception e) {
                                // Skip invalid events
                            }
                        }
                    }
                    return events;
                });
    }


    /**
     * This function obtains all the users from the database and turns them into user objects.
     * It does this by iterating through the users from the users collection in the database,
     * assigning the information obtained, then storing the user into a list.
     *
     * @return a database reference that returns a list of all the users in the database
     */
    public static Task<List<User>> getAllUsers() {
        return db
                .collection("Users")
                .get()
                .continueWith(task -> {
                    List<User> users = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            String deviceId = doc.getString("deviceId");
                            String email = doc.getString("entrantEmail");
                            Boolean isAdmin = doc.getBoolean("Admin");
                            Boolean isEntrant = doc.getBoolean("Entrant");
                            Boolean isOrganizer = doc.getBoolean("Organizer");
                            String name = doc.getString("entrantName");
                            String phone = doc.getString("entrantPhone");
                            Boolean notificationsEnabled = doc.getBoolean("notificationsEnabled");

                            User user = new User(
                                    deviceId,
                                    email,
                                    isAdmin != null && isAdmin,
                                    isEntrant != null && isEntrant,
                                    isOrganizer != null && isOrganizer,
                                    name,
                                    phone,
                                    notificationsEnabled != null && !notificationsEnabled,
                                    new ArrayList<>(),
                                    new ArrayList<>(),
                                    new ArrayList<>(),
                                    new ArrayList<>()
                            );
                            users.add(user);
                        }
                    }
                    return users;
                });
    }


    /**
     * Function for removing events from the database, along with their references. It does this
     * by deleting the event from Events collection in the database. Then it iterates through
     * all the users and removes the references to the given event from the users
     * event subcollections.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @return a task that is null when this action is complete
     * @throws Exception if we failed to get a user
     */
    public static Task<Void> removeEvent(String eventId) {
        WriteBatch batch = db.batch();

        // Delete the event document itself
        batch.delete(db.collection("Events").document(eventId));

        // First get all users to check their collections
        return db.collection("Users").get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot userDoc : task.getResult()) {
                            // Remove from createdEvents
                            batch.delete(userDoc.getReference()
                                    .collection("createdEvents")
                                    .document(eventId));

                            // Remove from joinedEvents
                            batch.delete(userDoc.getReference()
                                    .collection("joinedEvents")
                                    .document(eventId));

                            // Remove from registeredEvents
                            batch.delete(userDoc.getReference()
                                    .collection("registeredEvents")
                                    .document(eventId));

                            // Remove from missedOutEvents
                            batch.delete(userDoc.getReference()
                                    .collection("missedOutEvents")
                                    .document(eventId));
                        }
                        return batch.commit();
                    }
                    throw new Exception("Failed to get users");
                });
    }


    /**
     * Function for removing users from the database, along with their references. It does this
     * by deleting the user from Users collection in the database. Then it iterates through
     * all the events and removes the references to the given user from the event subcollections.
     *
     * @param userId The unique id for the user, created from the device id.
     * @return a task that is null when this action is complete
     */
    public static Task<Void> removeUser(String userId) {
        WriteBatch batch = db.batch();

        // Delete the user document itself
        batch.delete(db.collection("Users").document(userId));

        // Get all events to clean up participant lists
        return db.collection("Events").get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot eventDoc : task.getResult()) {
                            // Remove from waitingList
                            batch.delete(eventDoc.getReference()
                                    .collection("waitingList")
                                    .document(userId));

                            // Remove from selectedList
                            batch.delete(eventDoc.getReference()
                                    .collection("selectedList")
                                    .document(userId));

                            // Remove from finalList
                            batch.delete(eventDoc.getReference()
                                    .collection("finalList")
                                    .document(userId));

                            // Remove from cancelledList
                            batch.delete(eventDoc.getReference()
                                    .collection("cancelledList")
                                    .document(userId));
                        }
                    }

                    // Delete user's notifications
                    return db.collection("Notifications")
                            .whereEqualTo("userToId", userId)
                            .get();
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot notifDoc : task.getResult()) {
                            batch.delete(notifDoc.getReference());
                        }
                    }
                    return batch.commit();
                });
    }


    /**
     * Gets all the images from the database. Obtains images from both the event posters and
     * user profile images.
     *
     * @return A list of images
     */
    public static Task<List<ImageData>> getAllImages() {
        TaskCompletionSource<List<ImageData>> tcs = new TaskCompletionSource<>();
        List<ImageData> allImages = new ArrayList<>();

        db.collection("Events").get()
                .addOnSuccessListener(eventsSnapshot -> {
                    for (DocumentSnapshot doc : eventsSnapshot.getDocuments()) {
                        String posterBase64 = doc.getString("posterBase64");
                        if (posterBase64 != null && !posterBase64.isEmpty()) {
                            allImages.add(new ImageData(posterBase64, doc.getId(), "event", "posterBase64"));
                        }
                    }

                    db.collection("Users").get()
                            .addOnSuccessListener(usersSnapshot -> {
                                for (DocumentSnapshot doc : usersSnapshot.getDocuments()) {
                                    String profileImage = doc.getString("profileImage");
                                    if (profileImage != null && !profileImage.isEmpty()) {
                                        allImages.add(new ImageData(profileImage, doc.getId(), "user", "profileImage"));
                                    }
                                }
                                tcs.setResult(allImages);
                            })
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }


    /**
     * Function that removes an image from the database. It does this by setting the image field
     * to null.
     *
     * @param documentId The id of the images document, either from an event or profile.
     * @param documentType Type of document storing the image.
     * @param fieldName name of the field where the image is going to be deleted from
     * @return A task that is completed once the field is updated.
     */
    public static Task<Void> deleteImage(String documentId, String documentType, String fieldName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, null);

        return db
                .collection(documentType.equals("event") ? "Events" : "Users")
                .document(documentId)
                .update(updates);
    }


    /**
     * Obtains the profile image from the users document in the database.
     *
     * @param userId users id given by their device id
     * @return a task with the profile image URL or null if the user doesn't have a profile picture
     */
    public static Task<String> getProfileImage(String userId) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        db
                .collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String profileImage = document.getString("profileImage");
                        tcs.setResult(profileImage);
                    } else {
                        tcs.setResult(null);
                    }
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }


    /**
     * Function that removes a facility from the database, and also updates the related events.
     * Replaces facility references in events with "[Facility removed]".
     *
     * @param userId The user id that is attached to the facility. (The facility Owner)
     * @return a task that is sucessful once the facility fields have been cleared.
     */
    public static Task<Void> removeFacility(String userId) {
        WriteBatch batch = db.batch();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("facilityName", "");
        userUpdates.put("facilityAddress", "");

        Map<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("facilityName", "[Facility removed]");
        eventUpdates.put("facilityLocation", "[Facility removed]");

        return db.collection("Users").document(userId).collection("createdEvents").get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot eventDoc : task.getResult()) {
                            String eventId = eventDoc.getId();
                            batch.update(db.collection("Events").document(eventId), eventUpdates);
                        }
                        batch.update(db.collection("Users").document(userId), userUpdates);
                        return batch.commit();
                    }
                    throw task.getException();
                });
    }


    /**
     * Interface for facility detail loading methods and errors associated with it
     */
    public interface OnFacilityDetailsLoadedListener {
        void onFacilityLoaded(String facilityName, String facilityAddress);
        void onError(String error);
    }


    /**
     * This function obtains facility details such as facility name and address from a specific organizer
     *
     * @param userId The id for the user, created from the device id.
     * @param listener handles the outcome of loading the facility details
     */
    public static void getFacilityDetails(String userId, OnFacilityDetailsLoadedListener listener) {
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    String facilityName = document.getString("facilityName");
                    String facilityAddress = document.getString("facilityAddress");
                    listener.onFacilityLoaded(facilityName, facilityAddress);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }


    /**
     * This function obtains the organizers name from their ID, by going through their profile.
     *
     * @param organizerId The id for the organizer, created from the device id.
     * @param listener handles the outcome of obtaining organizer names
     */
    public static void getOrganizerName(String organizerId, OnNameLoadedListener listener) {
        db.collection("Users")
                .document(organizerId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("entrantName");
                        listener.onNameLoaded(name != null ? name : "Unknown");
                    } else {
                        listener.onNameLoaded("Unknown");
                    }
                })
                .addOnFailureListener(e -> listener.onNameLoaded("Unknown"));
    }


    /**
     * Interface for name loading classes
     */
    public interface OnNameLoadedListener {
        void onNameLoaded(String name);
    }


    /**
     * This function removes QR Code data from a given event.
     *
     * @param eventId The id for the event
     * @return a call to the database that removes the QR Code data on success, otherwise failure
     */
    public static Task<Void> removeQRCodeData(String eventId) {
        return db.collection("Events")
                .document(eventId)
                .update("qrCode_hashData", null)
                .addOnFailureListener(e -> {
                    Log.e("AdminDatabase", "Error removing QR code data", e);
                });
    }


    /**
     * This function removes the event poster from a given event.
     *
     * @param eventId The id for the event
     * @return a call to the database that removes the event poster on success, otherwise failure
     */
    public static Task<Void> removeEventPoster(String eventId) {
        return db.collection("Events")
                .document(eventId)
                .update("posterBase64", null)
                .addOnFailureListener(e -> {
                    Log.e("AdminDatabase", "Error removing event poster", e);
                });
    }
}
