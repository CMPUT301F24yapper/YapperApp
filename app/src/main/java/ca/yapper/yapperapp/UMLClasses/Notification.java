package ca.yapper.yapperapp.UMLClasses;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.R;

public class Notification {

    private String id;                  // Firestore ID
    private Date dateTimeStamp;
    private String userToId;            // Recipient's device ID
    private String userFromId;          // Sender's device ID
    private String title;
    private String message;
    private String notificationType;    // Type such as "Invitation", "Rejection", etc.
    private boolean isRead;
    private static final String channel_Id = "event_notifications";
    private static final String channel_Name = "event_notifications";
    private static final String channel_desc = "event_notifications";



    public Notification() {}

    public Notification(Date dateTimeStamp, String userToId, String userFromId,
                        String title, String message, String notificationType) {
        this.dateTimeStamp = dateTimeStamp;
        this.userToId = userToId;
        this.userFromId = userFromId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false;
    }

    public Notification(Date dateTimeStamp, String title, String message, String notificationType) {
        this.dateTimeStamp = dateTimeStamp;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false;
    }



    public void displayNotification(Context context) {
        Log.d("Notification", "Attempting to display notification");

        if (context == null) {
            Log.e("NotificationError", "Context is null, cannot display notification");
            return;
        }

        Log.d("Notification", "Title: " + title);
        Log.d("Notification", "Message: " + message);
        Log.d("Notification", "Channel ID: " + channel_Id);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_Id)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Log.d("Notification", "NotificationManagerCompat initialized");

        int notificationId = (int) System.currentTimeMillis();
        Log.d("Notification", "Displaying notification with ID: " + notificationId);

        notificationManager.notify(notificationId, builder.build());
        Log.d("Notification", "Notification displayed successfully");

        saveToDatabase();
    }



    public void saveToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                    setId(documentReference.getId()); // Set the document ID
                    Log.d("Notification", "Notification added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> Log.e("NotificationError", "Error adding notification", e));
    }



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



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(Date dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public String getUserToId() {
        return userToId;
    }

    public void setUserToId(String userToId) {
        this.userToId = userToId;
    }

    public String getUserFromId() {
        return userFromId;
    }

    public void setUserFromId(String userFromId) {
        this.userFromId = userFromId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {this.notificationType = notificationType;}

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
