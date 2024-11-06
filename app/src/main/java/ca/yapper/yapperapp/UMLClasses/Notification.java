package ca.yapper.yapperapp.UMLClasses;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Notification {
    private Date dateTimeStamp;
    private User userTo;
    private User userFrom;
    private String title;
    private String message;
    private String notificationType; // Type such as "Invitation", "Rejection", "Acceptance", etc.
    private boolean isRead; // To track if the notification has been viewed by the user

    // Constructor with parameters
    public Notification(Date dateTimeStamp, User userTo, User userFrom, String title, String message, String notificationType) {
        this.dateTimeStamp = dateTimeStamp;
        this.userTo = userTo;
        this.userFrom = userFrom;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false; // Default to false when a notification is created
    }

    // Getters and Setters
    public Date getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(Date dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public User getUserTo() {
        return userTo;
    }

    public void setUserTo(User userTo) {
        this.userTo = userTo;
    }

    public User getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(User userFrom) {
        this.userFrom = userFrom;
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

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    // Method to format the notification content based on details
    public void formatMessage(String eventName, String customMessage) {
        this.message = String.format("Event: %s | Details: %s", eventName, customMessage);
    }

    // Placeholder for sending notification logic
    public void sendNotification() {
        if (userTo != null && !userTo.getIsOptedOut()) { // Check if user is not opted out
            // Logic to send notification via Firebase Cloud Messaging (FCM)
            System.out.println("Notification sent to " + userTo.getName());
        }
    }

    // Method to mark the notification as read
    public void markAsRead() {
        this.isRead = true;
    }

    // Method to save the notification to Firestore
    public void saveToDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Prepare the notification data
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("dateTimeStamp", dateTimeStamp);
        notificationData.put("userToId", userTo); // Assuming User has a getId() method for user ID
        notificationData.put("userFromId", userFrom); // Assuming User has a getId() method for user ID
        notificationData.put("title", title);
        notificationData.put("message", message);
        notificationData.put("notificationType", notificationType);
        notificationData.put("isRead", isRead);

        // Add the notification to the "Notifications" collection
        db.collection("Notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> System.out.println("Notification added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> System.err.println("Error adding notification: " + e));
    }
}
