package ca.yapper.yapperapp.Databases;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.yapper.yapperapp.NotificationListener;
import ca.yapper.yapperapp.UMLClasses.Notification;

/**
 * Class holding all the notification related functions that interact with the database.
 */
public class NotificationsDatabase {
    private static final FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    private static final String TAG = "NotificationsDatabase";
    private static ListenerRegistration listenerRegistration;
    private static Date lastNotificationTime;

    /**
     * Interface for handling notification loading methods
     */
    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);
        void onError(String error);
    }

    /**
     * Function that saves the notification to Firestore under the "Notifications" collection.
     * Sets the Firestore document ID for future reference.
     *
     * @param dateTimeStamp when the notification was made
     * @param userToId id of user getting the notification
     * @param userFromId id of user sending the notification
     * @param title title of notification
     * @param message contents of the notification
     * @param notificationType the type of notification
     * @param isRead the status of the notification(if it's been read or not)
     */
    public static void saveToDatabase(Date dateTimeStamp, String userToId, String userFromId,
                                      String title, String message, String notificationType,
                                      boolean isRead, String eventId, String eventName) { // Include eventName
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("dateTimeStamp", dateTimeStamp);
        notificationData.put("userToId", userToId);
        notificationData.put("userFromId", userFromId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("notificationType", notificationType);
        notificationData.put("isRead", isRead);
        notificationData.put("eventId", eventId);
        notificationData.put("eventName", eventName); // Include eventName

        db.collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Notification", "Notification added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> Log.e("NotificationError", "Error adding notification", e));
    }

    /**
     * Function that loads notifications that haven't been read, for a given user.
     *
     * @param userDeviceId The device ID of the user.
     * @param listener listener to handle outcome of loading a notification
     */
    public static void loadNotifications(String userDeviceId, OnNotificationsLoadedListener listener) {
        db.collection("Notifications")
                .whereEqualTo("userToId", userDeviceId)
                .whereEqualTo("isRead", false) // Load only unread notifications
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Notification notification = document.toObject(Notification.class);
                        if (notification != null) {
                            notification.setId(document.getId());
                            notifications.add(notification);
                        }
                    }
                    listener.onNotificationsLoaded(notifications);
                })
                .addOnFailureListener(e -> listener.onError("Error loading notifications: " + e.getMessage()));
    }

    /**
     * Function that changes notifications from unread to read.
     *
     * @param notificationId id of a specific notification
     * @return a Task with the result of the operation
     */
    public static Task<Void> markNotificationAsRead(String notificationId) {
        return db.collection("Notifications").document(notificationId)
                .update("isRead", true);
    }

    public static void sendNotificationToUser(String userId, Notification notification) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userId)
                .collection("Notifications").add(notification)
                .addOnSuccessListener(documentReference -> Log.d("NotificationsDB", "Notification sent successfully."))
                .addOnFailureListener(e -> Log.e("NotificationsDB", "Error sending notification", e));
    }

    public static void startNotificationListener(String deviceId, NotificationListener.NotificationCallback callback) {
        Log.d(TAG, "Starting Firestore notification listener for device: " + deviceId);

        lastNotificationTime = new Date();

        listenerRegistration = db.collection("Notifications")
                .whereEqualTo("userToId", deviceId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots == null) {
                        Log.d(TAG, "Snapshot is null");
                        return;
                    }

                    snapshots.getDocumentChanges().forEach(change -> {
                        if (change.getType().equals(com.google.firebase.firestore.DocumentChange.Type.ADDED)) {
                            Date documentDate = change.getDocument().getDate("dateTimeStamp");
                            if (documentDate != null && documentDate.after(lastNotificationTime)) {
                                String message = change.getDocument().getString("message");
                                String title = change.getDocument().getString("title");
                                Log.d(TAG, "New notification - Title: " + title + ", Message: " + message);
                                if (message != null) {
                                    callback.onNotification(title, message);
                                }
                            }
                        }
                    });
                });
    }

    public static void stopNotificationListener() {
        Log.d(TAG, "Stopping Firestore notification listener");
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}
