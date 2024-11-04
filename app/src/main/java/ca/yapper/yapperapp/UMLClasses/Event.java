package ca.yapper.yapperapp.UMLClasses;

import android.util.Log;

import com.google.zxing.WriterException;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    private qrCode eventQRCode;
    // eventQRCode: string that UNIQUELY IDENTITIES EVENT in database?! -> confirm
    private String uniqueEventString;
    
    private String eventName;
    private String eventDateTime;
    private String eventRegDeadline;

    private String eventFacilityName;
    private String eventFacilityLocation;
    // important re eventFacility: in the Firestore document for this Event (Events collection), the field re facility will be a reference...
    //... to an already created document for facility under the Facilities collection referencing its id (facilityId),...
    //... in turn gives us access to facility details such as facilityName, facilityLocation, facilityPic...

    // private Poster eventPoster
    // important re eventPoster: in the Firestore document for this Event (Events collection), the field re poster will be a reference...
    //... to a document for the poster under the Images collection referencing its id (posterId)...
    //... in turn gives us access to poster details such as the imageURI (for actual display of image)
    //... HOWEVER, this document won't have been created UNTIL the Organizer has uploaded a poster upon creating the Event
    //... which means we might want to keep this attribute declaration commented out for Event class... provide logic for the eventPoster as a conditional function within the OrganizerCreateEventFragment (if they decide to press upload!)
    //... & then when or if Organizer uploads poster, that info is stored in Firestore as an additional field

    private int eventAttendees;
    // eventAttendees: required attribute to create Event (number of people/capacity Organizer declares for Event)
    private int eventWlCapacity;
    // eventWlCapacity: OPTIONAL attribute for waiting list capacity

    private boolean eventGeolocEnabled;
    // eventGeolocEnabled: required attribute to create Event
    private ArrayList<User> waitingList;
    private ArrayList<User> selectedList;
    private ArrayList<User> finalList;
    private ArrayList<User> cancelledList;
    // deserialize constructor for Firebase
    public Event() {}
    // constructor version with parameters
    public Event(String eventName, String eventDateTime, String eventRegDeadline, String eventFacilityName, String eventFacilityLocation, int eventAttendees, int eventWlCapacity, int eventWlSeatsLeft, boolean eventGeolocEnabled, ArrayList<User> waitingList, ArrayList<User> selectedList, ArrayList<User> finalList, ArrayList<User> cancelledList) throws WriterException {
        this.eventName = eventName;
        this.eventDateTime = eventDateTime;
        this.eventRegDeadline = eventRegDeadline;
        this.eventFacilityName = eventFacilityName;
        this.eventFacilityLocation = eventFacilityLocation;
        this.eventAttendees = eventAttendees;
        this.eventWlCapacity = eventWlCapacity;
        this.eventGeolocEnabled = eventGeolocEnabled;
        this.eventQRCode = new qrCode(this.eventName);
        this.waitingList = waitingList;
        this.selectedList = selectedList;
        this.finalList = finalList;
        this.cancelledList = cancelledList;
    }

    public qrCode getEventQRCode() {
        return this.eventQRCode;
    }

    public void setEventQRCode(qrCode eventQRCode) {
        this.eventQRCode = eventQRCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(String eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getEventRegDeadline() {
        return eventRegDeadline;
    }

    public void setEventRegDeadline(String eventRegDeadline) {
        this.eventRegDeadline = eventRegDeadline;
    }

    public String getEventFacilityName() {
        return eventFacilityName;
    }

    public void setEventFacilityName(String eventFacilityName) {
        this.eventFacilityName = eventFacilityName;
    }

    public String getEventFacilityLocation() {
        return eventFacilityLocation;
    }

    public void setEventFacilityLocation(String eventFacilityLocation) {
        this.eventFacilityLocation = eventFacilityLocation;
    }

    public int getEventAttendees() {
        return eventAttendees;
    }

    public void setEventAttendees(int eventAttendees) {
        this.eventAttendees = eventAttendees;
    }

    public int getEventWlCapacity() {
        return eventWlCapacity;
    }

    public void setEventWlCapacity(int eventWlCapacity) {
        this.eventWlCapacity = eventWlCapacity;
    }

    public boolean isEventGeolocEnabled() {
        return eventGeolocEnabled;
    }

    public void setEventGeolocEnabled(boolean eventGeolocEnabled) {
        this.eventGeolocEnabled = eventGeolocEnabled;
    }

    public ArrayList<User> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<User> waitingList) {
        this.waitingList = waitingList;
    }

    public ArrayList<User> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(ArrayList<User> selectedList) {
        this.selectedList = selectedList;
    }

    public ArrayList<User> getFinalList() {
        return finalList;
    }

    public void setFinalList(ArrayList<User> finalList) {
        this.finalList = finalList;
    }

    public ArrayList<User> getCancelledList() {
        return cancelledList;
    }

    public void setCancelledList(ArrayList<User> cancelledList) {
        this.cancelledList = cancelledList;
    }
}
