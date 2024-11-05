package ca.yapper.yapperapp.UMLClasses;

import java.util.Date;

public class InvitedNotif extends Notification {
    private String message; // Holds the invitation message

    public InvitedNotif(Date dateTimeStamp, User userTo, User userFrom, String message) {
        super(dateTimeStamp, userTo, userFrom, "Event Invitation", message, "Invitation");
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

    // Method to customize the invitation message
    public void setInvitationMessage(String eventName) {
        this.message = "You have been invited to join the event: " + eventName + ".";
        setMessage(this.message);
    }
}
