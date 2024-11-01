package ca.yapper.yapperapp;

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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.Facility;

public class OrganizerCreateEventFragment extends Fragment {
    private EditText eventNameEditText;
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
        // code for event poster
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
        String eventFacilityName = eventFacilityEditText.getText().toString();
        // TO-DO: how does event facility work here? i.e. if they've used this facility before / exists in facility profile already, we shouldn't be creating
        // new instance of facility?!
        // Facility eventFacility = new Facility(eventFacilityName);
        String eventDate = eventDateEditText.getText().toString();
        String registrationDeadline = eventDeadlineEditText.getText().toString();
        Integer eventAttendees = Integer.valueOf(eventNumberOfAttendeesEditText.getText().toString());
        Integer eventWlCapacity = Integer.valueOf(eventWlCapacityEditText.getText().toString());
        Integer eventWlSeatsAvailable = eventWlCapacity;
        boolean geolocationEnabled = geolocationSwitch.isChecked();


        // **don't forget to make it clear for user on screen which fields are required**

        if (eventName.isEmpty() || eventDate.isEmpty() || eventFacilityName.isEmpty() || eventAttendees == null) {
            Toast.makeText(getActivity(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create instance of Event NOTE: QR Codes are automatically generated at event creation
        Event event = new Event(eventName, eventFacilityName, eventDate, registrationDeadline, eventAttendees, eventWlCapacity, eventWlSeatsAvailable, geolocationEnabled);
        // Create map to store Event data

        // Generating QR Code
        eventId = Integer.toString(event.getEventQRCode().getHashData()); // now the eventId is the "unique" qr hash value

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", event.getEventName());
        eventData.put("facilityName", event.getEventFacility());
        eventData.put("eventDateTime", event.getEventDate());
        eventData.put("registrationDeadline", event.getRegistrationDeadline());
        eventData.put("eventAttendees", event.getEventAttendees());
        eventData.put("eventWlCapacity", event.getWlCapacity());
        eventData.put("eventWlSeatsAvailable", event.getWlSeatsAvailable());
        eventData.put("geolocationEnabled", event.isGeolocationEnabled());
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
