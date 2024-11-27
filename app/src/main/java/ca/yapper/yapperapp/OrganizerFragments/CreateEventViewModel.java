package ca.yapper.yapperapp.OrganizerFragments;

import android.net.Uri;

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
    public Uri posterImageUri;

    public String posterImageUrl;
    public String eventId;
    public String posterImageBase64;
}