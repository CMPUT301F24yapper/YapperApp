package ca.yapper.yapperapp.UMLClasses;

import com.google.zxing.WriterException;

public class Event {
    private int capacity;
    private String date_Time;
    private String facilityLocation;
    private String facilityName;
    private boolean isGeolocationEnabled;
    private String name;
    private qrCode QRCode;
    private String registrationDeadline;
    private int waitListCapacity;

    public Event(String name, String date_Time, String registrationDeadline, String facilityName, String facilityLocation, int capacity, int eventWlCapacity, int eventWlSeatsLeft, boolean isGeolocationEnabled) throws WriterException {
        this.capacity = capacity;
        this.date_Time = date_Time;
        this.facilityLocation = facilityLocation;
        this.facilityName = facilityName;
        this.isGeolocationEnabled = isGeolocationEnabled;
        this.name = name;
        this.QRCode = new qrCode(this.name);
        this.registrationDeadline = registrationDeadline;
        this.waitListCapacity = eventWlCapacity;
    }

    public qrCode getQRCode() {
        return this.QRCode;
    }

    public void setQRCode(qrCode QRCode) {
        this.QRCode = QRCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_Time() {
        return date_Time;
    }

    public void setDate_Time(String date_Time) {
        this.date_Time = date_Time;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityLocation() {
        return facilityLocation;
    }

    public void setFacilityLocation(String facilityLocation) {
        this.facilityLocation = facilityLocation;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWaitListCapacity() {
        return waitListCapacity;
    }

    public void setWaitListCapacity(int waitListCapacity) {
        this.waitListCapacity = waitListCapacity;
    }

    public boolean isGeolocationEnabled() {
        return isGeolocationEnabled;
    }

    public void setGeolocationEnabled(boolean geolocationEnabled) {
        this.isGeolocationEnabled = geolocationEnabled;
    }
}