package ca.yapper.yapperapp.Databases;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;

//Test MERGE
public class AdminDatabase {
    public static Task<Map<String, Long>> getAdminStats() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TaskCompletionSource<Map<String, Long>> tcs = new TaskCompletionSource<>();
        Map<String, Long> stats = new HashMap<>();

        db.collection("Events").get().addOnSuccessListener(eventSnapshot -> {
            stats.put("totalEvents", (long) eventSnapshot.size());

            db.collection("Users").get().addOnSuccessListener(userSnapshot -> {
                long organizerCount = 0;
                for (int i = 0; i < userSnapshot.size(); i++) {
                    Boolean isOrganizer = userSnapshot.getDocuments().get(i).getBoolean("Organizer");
                    if (isOrganizer != null && isOrganizer) {
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

            querySnapshot.forEach(doc -> {
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("name", doc.getString("name"));
                eventMap.put("capacity", doc.getLong("capacity"));
                eventList.add(eventMap);
            });

            eventList.sort((e1, e2) -> {
                Long cap1 = (Long) e1.get("capacity");
                Long cap2 = (Long) e2.get("capacity");
                return cap2.compareTo(cap1);
            });

            // Get top 5 events
            List<Map<String, Object>> topEvents = eventList.subList(0, Math.min(5, eventList.size()));
            tcs.setResult(topEvents);
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
                            String facilityName = doc.getString("facilityName");
                            boolean isGeolocationEnabled = doc.getBoolean("isGeolocationEnabled");
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
                                        registrationDeadline,
                                        waitListCapacity,
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new ArrayList<>(),
                                        new ArrayList<>()
                                );
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
        return FirebaseFirestore.getInstance()
                .collection("Events")
                .document(eventId)
                .delete();
    }

    public static Task<Void> removeUser(String userId) {
        return FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId)
                .delete();
    }
}
