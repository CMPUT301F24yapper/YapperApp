package ca.yapper.yapperapp.Databases;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import ca.yapper.yapperapp.UMLClasses.Notification;

public class NotificationsDatabase {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnNotificationsLoadedListener {
        void onNotificationsLoaded(List<Notification> notifications);
        void onError(String error);
    }

    public static void loadNotifications(String userDeviceId, OnNotificationsLoadedListener listener) {
        db.collection("Notifications")
                .whereEqualTo("userToId", userDeviceId)
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