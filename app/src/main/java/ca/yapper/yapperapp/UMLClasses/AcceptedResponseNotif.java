package ca.yapper.yapperapp.UMLClasses;

import java.util.Date;

public class AcceptedResponseNotif extends Notification {
    public String message;   // (?)
    public AcceptedResponseNotif(Date dateTimeStamp, User userTo, User userFrom) {
        super(dateTimeStamp, userTo, userFrom);
        this.message = message;
    }
}
