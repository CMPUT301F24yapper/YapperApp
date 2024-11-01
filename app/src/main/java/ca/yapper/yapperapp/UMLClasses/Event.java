package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.Date;

public class Event {
    private String eventName;
    // private Facility eventFacility;
    private String eventFacility;
    // private Date eventDate;
    private String eventDate;
    // private Date registrationDeadline;
    private String registrationDeadline;
    // private Poster poster;
    private qrCode eventQRCode;
    private int eventAttendees;
    private int wlCapacity;
    private int wlSeatsAvailable;
    private boolean geolocationEnabled;

    // deserialize constructor for Firebase
    public Event() {}
    // constructor version with parameters
    public Event(String eventName, String eventFacility, String eventDate, String registrationDeadline, int eventAttendees, int wlCapacity, int wlSeatsAvailable, boolean geolocationEnabled) throws WriterException {
        this.eventName = eventName;
        this.eventFacility = eventFacility;
        this.eventDate = eventDate;
        this.eventQRCode = new qrCode(this.eventName);
        this.registrationDeadline = registrationDeadline;
        this.eventAttendees = eventAttendees;
        // this.poster = poster;
        this.wlCapacity = wlCapacity;
        this.wlSeatsAvailable = wlSeatsAvailable;
        this.geolocationEnabled = geolocationEnabled;
    }

    public qrCode getEventQRCode() { return this.eventQRCode; }

    public void setEventQRCode(qrCode eventQRCode) { this.eventQRCode = eventQRCode; }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventFacility() {
        return eventFacility;
    }

    public void setEventFacility(String eventFacility) {
        this.eventFacility = eventFacility;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public int getEventAttendees() {
        return eventAttendees;
    }

    public void setEventAttendees(int eventAttendees) {
        this.eventAttendees = eventAttendees;
    }

    /** public Poster getPoster() {
        return poster;
    }

    public void setPoster(Poster poster) {
        this.poster = poster;
    } **/

    public int getWlCapacity() {
        return wlCapacity;
    }

    public void setWlCapacity(int wlCapacity) {
        this.wlCapacity = wlCapacity;
    }

    public int getWlSeatsAvailable() {
        return wlSeatsAvailable;
    }

    public void setWlSeatsAvailable(int wlSeatsAvailable) {
        this.wlSeatsAvailable = wlSeatsAvailable;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.geolocationEnabled = geolocationEnabled;
    }
}
