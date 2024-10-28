package ca.yapper.yapperapp.UMLClasses;

import java.util.Date;

public class Notification {
    private Date dateTimeStamp;
    private User userTo;
    private User userFrom;

    public Notification(Date dateTimeStamp, User userTo, User userFrom) {
        this.dateTimeStamp = dateTimeStamp;
        this.userTo = userTo;
        this.userFrom = userFrom;
    }

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
}
