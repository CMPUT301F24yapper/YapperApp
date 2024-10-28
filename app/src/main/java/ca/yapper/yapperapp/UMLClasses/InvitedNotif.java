package ca.yapper.yapperapp.UMLClasses;

import java.util.Date;

public class InvitedNotif extends Notification {
    public String message;   // (?)
    public InvitedNotif(Date dateTimeStamp, User userTo, User userFrom) {
        super(dateTimeStamp, userTo, userFrom);
        this.message = message;
    }
}
