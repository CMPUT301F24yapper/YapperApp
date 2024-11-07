package ca.yapper.yapperapp.UMLClasses;

import java.util.Date;

public class RejectionNotif extends Notification {
    private String message; // Holds the rejection message

    public RejectionNotif(Date dateTimeStamp, String userTo, String userFrom, String message) {
        super(dateTimeStamp, userTo, userFrom, "Event Rejection", message, "Rejection");
        this.message = message;
    }

    // Getter and setter for message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        setMessage(message); // Update the base class message attribute as well
    }

    // Method to customize the rejection message
    public void setRejectionMessage(String eventName) {
        this.message = "We regret to inform you that you were not selected for the event: " + eventName + ".";
        setMessage(this.message);
    }
}
