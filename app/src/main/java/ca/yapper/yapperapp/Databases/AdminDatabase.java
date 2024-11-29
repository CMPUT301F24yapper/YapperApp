package ca.yapper.yapperapp.Databases;

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

public class AdminDatabase {
    public static Task<Map<String, Long>> getAdminStats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    public static Task<List<Map<String, Object>>> getBiggestEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    public static Task<List<Event>> getAllEvents() {
        return FirebaseFirestore.getInstance()
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

    public static Task<List<User>> getAllUsers() {
        return FirebaseFirestore.getInstance()
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

    public static Task<Void> removeEvent(String eventId) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        // Delete the event document itself
        batch.delete(FirebaseFirestore.getInstance().collection("Events").document(eventId));

        // First get all users to check their collections
        return FirebaseFirestore.getInstance().collection("Users").get()
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

    public static Task<Void> removeUser(String userId) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        // Delete the user document itself
        batch.delete(FirebaseFirestore.getInstance().collection("Users").document(userId));

        // Get all events to clean up participant lists
        return FirebaseFirestore.getInstance().collection("Events").get()
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
                    return FirebaseFirestore.getInstance().collection("Notifications")
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

    public static Task<List<ImageData>> getAllImages() {
        TaskCompletionSource<List<ImageData>> tcs = new TaskCompletionSource<>();
        List<ImageData> allImages = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("Events").get()
                .addOnSuccessListener(eventsSnapshot -> {
                    for (DocumentSnapshot doc : eventsSnapshot.getDocuments()) {
                        String posterBase64 = doc.getString("posterBase64");
                        if (posterBase64 != null && !posterBase64.isEmpty()) {
                            allImages.add(new ImageData(posterBase64, doc.getId(), "event", "posterBase64"));
                        }
                    }

                    FirebaseFirestore.getInstance().collection("Users").get()
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

    public static Task<Void> deleteImage(String documentId, String documentType, String fieldName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, null);

        return FirebaseFirestore.getInstance()
                .collection(documentType.equals("event") ? "Events" : "Users")
                .document(documentId)
                .update(updates);
    }

    public static Task<String> getProfileImage(String userId) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();

        FirebaseFirestore.getInstance()
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

    public static Task<Void> removeFacility(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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

    public interface OnFacilityDetailsLoadedListener {
        void onFacilityLoaded(String facilityName, String facilityAddress);
        void onError(String error);
    }

    public static void getFacilityDetails(String userId, OnFacilityDetailsLoadedListener listener) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    String facilityName = document.getString("facilityName");
                    String facilityAddress = document.getString("facilityAddress");
                    listener.onFacilityLoaded(facilityName, facilityAddress);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    public static void getOrganizerName(String organizerId, OnNameLoadedListener listener) {
        FirebaseFirestore.getInstance().collection("Users")
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

    public interface OnNameLoadedListener {
        void onNameLoaded(String name);
    }
}
