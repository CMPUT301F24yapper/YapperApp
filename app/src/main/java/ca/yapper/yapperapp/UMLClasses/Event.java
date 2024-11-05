package ca.yapper.yapperapp.UMLClasses;

import com.google.zxing.WriterException;
// TO-DO: ADD IN THE (INITIALLY NULL) ARRAYLIST<USER> (SUBCOLLECTIONS IN FS) WAITINGLIST, SELECTEDLIST, FINALLIST, CANCELLEDLIST!
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

    public Event(String name, String date_Time, String registrationDeadline, String facilityName, String facilityLocation, int capacity, int eventWlCapacity, boolean isGeolocationEnabled) throws WriterException {
        this.name = name;
        this.date_Time = date_Time;
        this.registrationDeadline = registrationDeadline;
        this.facilityName = facilityName;
        this.facilityLocation = facilityLocation;
        this.capacity = capacity;
        this.waitListCapacity = eventWlCapacity;
        this.isGeolocationEnabled = isGeolocationEnabled;
        this.QRCode = new qrCode(this.name);
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

//    public ArrayList<User> getWaitingList() {
//        return waitingList;
//    }
//
//    public void setWaitingList(ArrayList<User> waitingList) {
//        this.waitingList = waitingList;
//    }
//
//    public ArrayList<User> getSelectedList() {
//        return selectedList;
//    }
//
//    public void setSelectedList(ArrayList<User> selectedList) {
//        this.selectedList = selectedList;
//    }
//
//    public ArrayList<User> getFinalList() {
//        return finalList;
//    }
//
//    public void setFinalList(ArrayList<User> finalList) {
//        this.finalList = finalList;
//    }
//
//    public ArrayList<User> getCancelledList() {
//        return cancelledList;
//    }
}