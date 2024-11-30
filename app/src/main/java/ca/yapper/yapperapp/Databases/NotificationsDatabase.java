package ca.yapper.yapperapp.Databases;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.yapper.yapperapp.UMLClasses.Notification;

public class NotificationsDatabase {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);
        void onError(String error);
    }

    /**
     * Saves the notification to Firestore under the "Notifications" collection.
     * Sets the Firestore document ID for future reference.
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

    public static Task<Void> markNotificationAsRead(String notificationId) {
        return db.collection("Notifications").document(notificationId)
                .update("isRead", true);
    }
}
