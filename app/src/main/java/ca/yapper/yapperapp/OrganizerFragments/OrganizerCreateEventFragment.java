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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

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

    private FirebaseFirestore db;
    private int selectedYear, selectedMonth, selectedDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_createevent, container, false);

        eventNameEditText = view.findViewById(R.id.event_name_input);
        eventFacilityEditText = view.findViewById(R.id.event_facility_input);
        eventDateEditText = view.findViewById(R.id.date_input);
        eventDeadlineEditText = view.findViewById(R.id.deadline_input);
        eventNumberOfAttendeesEditText = view.findViewById(R.id.attendees_input);
        eventWlCapacityEditText = view.findViewById(R.id.wl_capacity_input);
        geolocationSwitch = view.findViewById(R.id.geo_location_toggle);
        saveEventButton = view.findViewById(R.id.save_event_button);
        db = FirebaseFirestore.getInstance();

        eventDateEditText.setOnClickListener(v -> showDatePickerDialog(eventDateEditText));
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    dateEditText.setText(selectedDate);
                }, selectedYear, selectedMonth, selectedDay);

        datePickerDialog.show();
    }

    private void createEvent() throws WriterException {
        String eventName = eventNameEditText.getText().toString();
        String eventFacilityName = eventFacilityEditText.getText().toString();
        String eventFacilityLocation = ""; // Placeholder
        String eventDateTime = eventDateEditText.getText().toString();
        String eventRegDeadline = eventDeadlineEditText.getText().toString();

        int eventAttendees = eventNumberOfAttendeesEditText.getText().toString().isEmpty() ? 0 :
                Integer.parseInt(eventNumberOfAttendeesEditText.getText().toString());
        int eventWlCapacity = eventWlCapacityEditText.getText().toString().isEmpty() ? 0 :
                Integer.parseInt(eventWlCapacityEditText.getText().toString());
        int eventWlSeatsAvailable = eventWlCapacity;
        boolean geolocationEnabled = geolocationSwitch.isChecked();

        if (eventName.isEmpty() || eventDateTime.isEmpty() || eventFacilityName.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(eventName, eventDateTime, eventRegDeadline,
                eventFacilityName, eventFacilityLocation, eventAttendees,
                eventWlCapacity, eventWlSeatsAvailable, geolocationEnabled);

        Log.d("EVENT", "FireBase Storage Begun");
        eventId = Integer.toString(event.getQRCode().getHashData());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name", event.getName());
        eventData.put("facilityName", event.getFacilityName());
        eventData.put("facilityLocation", event.getFacilityLocation());
        eventData.put("date_Time", event.getDate_Time());
        eventData.put("registrationDeadline", event.getRegistrationDeadline());
        eventData.put("capacity", event.getCapacity());
        eventData.put("waitListCapacity", event.getWaitListCapacity());
        eventData.put("isGeolocationEnabled", event.isGeolocationEnabled());
        eventData.put("qrCode_hashData", event.getQRCode().getQRCodeValue());

        DocumentReference eventRef = db.collection("Events").document(eventId);
        eventRef.set(eventData).addOnSuccessListener(aVoid -> {
            initializeEventSubcollections(eventRef);
            Toast.makeText(getActivity(), "Event created successfully", Toast.LENGTH_SHORT).show();
            // Could add navigation back to home screen here
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