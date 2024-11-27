package ca.yapper.yapperapp.OrganizerFragments;

import androidx.lifecycle.ViewModel;

public class CreateEventViewModel extends ViewModel {
    public String eventName;
    public String eventDescription;
    public String selectedDate;
    public String selectedTime;
    public String regDeadline;
    public Integer capacity;
    public Integer waitListCapacity;
    public boolean geolocationEnabled;

}