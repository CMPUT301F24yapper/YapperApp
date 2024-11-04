package ca.yapper.yapperapp.OrganizerFragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;

public class OrganizerCreateEventFragment extends Fragment {
    private EditText eventNameEditText;
    // for below facility details, only the name is needed because Facility should already be stored in firestore for Organizer...
    //... can later retrieve the Facility details such as location
    private EditText eventFacilityEditText;
    private EditText eventDateEditText;
    private EditText eventDeadlineEditText;
    private EditText eventNumberOfAttendeesEditText;
    private EditText eventWlCapacityEditText;
    private Switch geolocationSwitch;
    private Button saveEventButton;
    private String eventId;

    // Firebase Firestore instance
    private FirebaseFirestore db;

    // Date variables
    private int selectedYear, selectedMonth, selectedDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // return inflater.inflate(R.layout.organizer_createevent, container, false);
        View view = inflater.inflate(R.layout.organizer_createevent, container, false);

        eventNameEditText = view.findViewById(R.id.event_name_input);
        eventFacilityEditText = view.findViewById(R.id.event_facility_input);
        eventDateEditText = view.findViewById(R.id.date_input);
        eventDeadlineEditText = view.findViewById(R.id.deadline_input);
        // ***TO-DO: code for event poster
        eventNumberOfAttendeesEditText = view.findViewById(R.id.attendees_input);
        eventWlCapacityEditText = view.findViewById(R.id.wl_capacity_input);
        geolocationSwitch = view.findViewById(R.id.geo_location_toggle);
        saveEventButton = view.findViewById(R.id.save_event_button);
        db = FirebaseFirestore.getInstance();

        // Set up date picker
        eventDateEditText.setOnClickListener(v -> showDatePickerDialog(eventDateEditText));

        // Set up registration deadline picker
        eventDeadlineEditText.setOnClickListener(v -> showDatePickerDialog(eventDeadlineEditText));

        saveEventButton.setOnClickListener(v -> {
            try {
                createEvent();
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }
        });

        return view;
    }

    private void showDatePickerDialog(EditText dateEditText) {
        final Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year; // Format date
                    dateEditText.setText(selectedDate); // Set selected date to the respective EditText
                }, selectedYear, selectedMonth, selectedDay);

        datePickerDialog.show();
    }

    private void createEvent() throws WriterException { // writer exception from QR Code
        String eventName = eventNameEditText.getText().toString();
        // should facility be pre-written in for the Organizer (since they can only have 1 facility?)
        String eventFacilityName = eventFacilityEditText.getText().toString();
        String eventFacilityLocation = ""; // Placeholder, adjust if needed
        String eventDateTime = eventDateEditText.getText().toString();
        String eventRegDeadline = eventDeadlineEditText.getText().toString();

        // Ensure to parse these values safely
        int eventAttendees = eventNumberOfAttendeesEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(eventNumberOfAttendeesEditText.getText().toString());
        int eventWlCapacity = eventWlCapacityEditText.getText().toString().isEmpty() ? 0 : Integer.parseInt(eventWlCapacityEditText.getText().toString());
        int eventWlSeatsAvailable = eventWlCapacity; // Assuming seats available initially equals the capacity
        boolean geolocationEnabled = geolocationSwitch.isChecked();

        // create empty User lists
        ArrayList<User> waitingList = null;
        ArrayList<User> selectedList = null;
        ArrayList<User> finalList = null;
        ArrayList<User> cancelledList = null;

        // Check for required fields
        if (eventName.isEmpty() || eventDateTime.isEmpty() || eventFacilityName.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create instance of Event NOTE: QR Codes are automatically generated at event creation
        Event event = new Event(eventName, eventDateTime, eventRegDeadline, eventFacilityName, eventFacilityLocation, eventAttendees, eventWlCapacity, eventWlSeatsAvailable, geolocationEnabled, waitingList, selectedList, finalList, cancelledList);

        // getHashData() to confirm if this hashData is unique for eventId, if not then add 1s to id
        // hashing logic:
        // if (event.getHashData() )

        // Create map to store Event data
        // Generating QR Code
        Log.d("EVENT", "FireBase Storage Begun");
        eventId = Integer.toString(event.getEventQRCode().getHashData()); // now the eventId is the "unique" qr hash value

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", event.getEventName());
        eventData.put("facilityName", event.getEventFacilityName());
        eventData.put("eventDateTime", event.getEventDateTime());
        eventData.put("registrationDeadline", event.getEventRegDeadline());
        eventData.put("eventAttendees", event.getEventAttendees());
        eventData.put("eventWlCapacity", event.getEventWlCapacity());
        eventData.put("geolocationEnabled", event.isEventGeolocEnabled());
        eventData.put("qrCodeValue",event.getEventQRCode().getQRCodeValue());
        eventData.put("hashData",event.getEventQRCode().getHashData());
        //eventData.put("qrCodeBitMatrix",event.getEventQRCode().getQrCode());

        // Add a new document to Events collection
        DocumentReference eventRef = db.collection("Events").document(eventId); // eventID is the hash value from qr code
        eventRef.set(eventData).addOnSuccessListener(aVoid -> {
            // Initialize subcollections after creating the Event document
            initializeEventSubcollections(eventRef);
            Toast.makeText(getActivity(), "Event created successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w("Firestore", "Error creating event document", e);
            Toast.makeText(getActivity(), "Error creating event", Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeEventSubcollections(DocumentReference eventRef) {
        CollectionReference waitingListRef = eventRef.collection("waitingList");
        CollectionReference selectedListRef = eventRef.collection("selectedList");
        CollectionReference cancelledListRef = eventRef.collection("cancelledList");
        CollectionReference finalListRef = eventRef.collection("finalList");

        Map<String, Object> placeholder = new HashMap<>();
        placeholder.put("placeholder", true);

        waitingListRef.document("placeholder").set(placeholder);
        selectedListRef.document("placeholder").set(placeholder);
        cancelledListRef.document("placeholder").set(placeholder);
        finalListRef.document("placeholder").set(placeholder);

    }
}
