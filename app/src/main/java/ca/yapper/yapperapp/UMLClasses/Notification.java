package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;

import java.util.Date;

import ca.yapper.yapperapp.Databases.NotificationsDatabase;

/**
 * The Notification class represents a notification sent to users about events or updates.
 * It contains details about the sender, recipient, title, message, and type of notification.
 * Notifications can be saved to Firestore, marked as read, and displayed as local notifications.
 */
public class Notification {

    private String id;                  // Firestore ID
    private Date dateTimeStamp;
    private String userToId;            // Recipient's device ID
    private String userFromId;          // Sender's device ID
    private String title;
    private String message;
    private String notificationType;    // Type such as "Invitation", "Rejection", etc.
    private boolean isRead;
    private String eventName;           // Store the event name
    private String eventId;


    /**
     * Default constructor required for Firebase deserialization.
     */
    public Notification() {
        this.id = null;
        this.dateTimeStamp = new Date(); // Default to the current date
        this.userToId = null;
        this.userFromId = null;
        this.title = "";
        this.message = "";
        this.notificationType = "";
        this.isRead = false;
        this.eventId = null;
        this.eventName = null;
    }

    public Notification(Date dateTimeStamp, String userToId, String userFromId,
                        String title, String message, String notificationType, String eventId, String eventName) {
        this.dateTimeStamp = dateTimeStamp;
        this.userToId = userToId;
        this.userFromId = userFromId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.eventId = eventId;
        this.eventName = eventName; // Assign event name
        this.isRead = false;
    }


    /**
     * Saves the notification to Firestore under the "Notifications" collection.
     */
    public void saveToDatabase() {
        if (userToId == null || userToId.isEmpty()) {
            Log.e("NotificationError", "Cannot save notification: userToId is null or empty.");
            return;
        }

        NotificationsDatabase.saveToDatabase(
                dateTimeStamp,
                userToId,
                userFromId,
                title,
                message,
                notificationType,
                isRead,
                eventId,
                eventName // Pass eventName here
        );
    }


    /**
     * Marks the notification as read in Firestore and updates the local read status.
     */
    public void markAsRead() {
        if (id != null && !isRead) {
            NotificationsDatabase.markNotificationAsRead(id);
            isRead = true;
        }
    }

    // Getter and Setter methods
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

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
