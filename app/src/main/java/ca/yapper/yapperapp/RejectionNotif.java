package ca.yapper.yapperapp;

import java.util.Date;

public class RejectionNotif extends Notification {
    public String message;   // (?)
    public RejectionNotif(Date dateTimeStamp, User userTo, User userFrom) {
        super(dateTimeStamp, userTo, userFrom);
        this.message = message;
    }
}
