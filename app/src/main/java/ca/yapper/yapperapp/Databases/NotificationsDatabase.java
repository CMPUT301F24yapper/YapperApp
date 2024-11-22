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
    private static final String COLLECTION_NOTIFICATIONS = "Notifications";

    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);
        void onError(String error);
    }

    /**
     * Saves the notification to Firestore under the "Notifications" collection.
     * Sets the Firestore document ID for future reference.
     */
    public static void saveToDatabase(Date dateTimeStamp, String userToId, String userFromId, String title, String message, String notificationType, boolean isRead) {
        //FirebaseFirestore db = FirebaseFirestore.getInstance();
        Notification notification = new Notification(dateTimeStamp, userToId, userFromId, title, message, notificationType);
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("dateTimeStamp", dateTimeStamp);
        notificationData.put("userToId", userToId);
        notificationData.put("userFromId", userFromId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("notificationType", notificationType);
        notificationData.put("isRead", isRead);

        db.collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    notification.setId(documentReference.getId()); // Set the document ID
                    Log.d("Notification", "Notification added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> Log.e("NotificationError", "Error adding notification", e));
    }

    public static void loadNotifications(String userDeviceId, OnNotificationsLoadedListener listener) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("userToId", userDeviceId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Notification notification = document.toObject(Notification.class);
                        if (notification != null && document.getId() != null) {
                            notification.setId(document.getId());
                            notifications.add(notification);
                        }
                    }
                    listener.onNotificationsLoaded(notifications);
                })
                .addOnFailureListener(e -> listener.onError("Error loading notifications: " + e.getMessage()));
    }

    public static void markNotificationAsRead(String notificationId) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true);
    }
}