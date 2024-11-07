package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Notification {
    private String id;                  // Firestore document ID
    private Date dateTimeStamp;
    private String userToId;            // Recipient's device ID
    private String userFromId;          // Sender's device ID
    private String title;
    private String message;
    private String notificationType;    // Type such as "Invitation", "Rejection", etc.
    private boolean isRead;             // To track if the notification has been viewed by the user

    // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
    public Notification() {}

    // Constructor with parameters
    public Notification(Date dateTimeStamp, String userToId, String userFromId,
                        String title, String message, String notificationType) {
        this.dateTimeStamp = dateTimeStamp;
        this.userToId = userToId;
        this.userFromId = userFromId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false; // Default to false when a notification is created
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Date and Time
    public Date getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(Date dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    // Recipient's Device ID
    public String getUserToId() {
        return userToId;
    }

    public void setUserToId(String userToId) {
        this.userToId = userToId;
    }

    // Sender's Device ID
    public String getUserFromId() {
        return userFromId;
    }

    public void setUserFromId(String userFromId) {
        this.userFromId = userFromId;
    }

    // Title of the Notification
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Message Content
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Notification Type
    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    // Read Status
    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Method to save the notification to Firestore
    public void saveToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the notification data
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("dateTimeStamp", dateTimeStamp);
        notificationData.put("userToId", userToId);
        notificationData.put("userFromId", userFromId);
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("notificationType", notificationType);
        notificationData.put("isRead", isRead);

        // Add the notification to the "Notifications" collection
        db.collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    setId(documentReference.getId()); // Set the document ID
                    Log.d("Notification", "Notification added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> Log.e("NotificationError", "Error adding notification", e));
    }

    // Method to mark the notification as read
    public void markAsRead() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Notifications").document(id)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> {
                    isRead = true;
                    Log.d("Notification", "Notification marked as read");
                })
                .addOnFailureListener(e -> Log.e("NotificationError", "Error marking notification as read", e));
    }
}
