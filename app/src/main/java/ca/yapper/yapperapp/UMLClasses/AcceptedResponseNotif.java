package ca.yapper.yapperapp.UMLClasses;

import java.util.Date;

public class AcceptedResponseNotif extends Notification {
    private String message; // Holds the acceptance message

    public AcceptedResponseNotif(Date dateTimeStamp, String userTo, String userFrom, String message) {
        super(dateTimeStamp, userTo, userFrom, "Accepted Response", message, "Acceptance");
        this.message = message;
    }

    // Getter and setter for message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        super.setMessage(message); // Update the base class message attribute
    }

    // Method to customize the acceptance message
    public void setAcceptanceMessage(String eventName) {
        this.message = "Your response to the event '" + eventName + "' has been accepted.";
        super.setMessage(this.message); // Update the base class message attribute
    }
}
