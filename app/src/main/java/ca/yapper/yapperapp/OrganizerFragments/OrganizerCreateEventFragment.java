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
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;

public class OrganizerCreateEventFragment extends Fragment {
    private EditText eventNameEditText;
    private EditText eventFacilityEditText;
    private EditText eventDateEditText;
    private EditText eventDeadlineEditText;
    private EditText eventNumberOfAttendeesEditText;
    private EditText eventWlCapacityEditText;
    private Switch geolocationSwitch;
    private Button saveEventButton;
    private String userDeviceId;
    private int selectedYear, selectedMonth, selectedDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_createevent, container, false);

        initializeViews(view);
        setupClickListeners();

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        return view;
    }

    private void initializeViews(View view) {
        eventNameEditText = view.findViewById(R.id.event_name_input);
        eventFacilityEditText = view.findViewById(R.id.event_facility_input);
        eventDateEditText = view.findViewById(R.id.date_input);
        eventDeadlineEditText = view.findViewById(R.id.deadline_input);
        eventNumberOfAttendeesEditText = view.findViewById(R.id.attendees_input);
        eventWlCapacityEditText = view.findViewById(R.id.wl_capacity_input);
        geolocationSwitch = view.findViewById(R.id.geo_location_toggle);
        saveEventButton = view.findViewById(R.id.save_event_button);
    }

    private void setupClickListeners() {
        eventDateEditText.setOnClickListener(v -> showDatePickerDialog(eventDateEditText));
        eventDeadlineEditText.setOnClickListener(v -> showDatePickerDialog(eventDeadlineEditText));
        saveEventButton.setOnClickListener(v -> createEvent());
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

    private void createEvent() {

        // First check if user is authorized
        User organizer = User.loadUserFromDatabase(userDeviceId);
        if (organizer == null || !organizer.isOrganizer()) {
            Toast.makeText(getActivity(), "Error: User not authorized to create events", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all inputs
        String name = eventNameEditText.getText().toString();
        String dateTime = eventDateEditText.getText().toString();
        String regDeadline = eventDeadlineEditText.getText().toString();
        String facilityName = eventFacilityEditText.getText().toString();
        String facilityLocation = ""; // Placeholder
        String description = ""; // You might want to add a description field to your form

        int capacity = eventNumberOfAttendeesEditText.getText().toString().isEmpty() ? 0 :
                Integer.parseInt(eventNumberOfAttendeesEditText.getText().toString());
        int waitListCapacity = eventWlCapacityEditText.getText().toString().isEmpty() ? 0 :
                Integer.parseInt(eventWlCapacityEditText.getText().toString());
        boolean geolocationEnabled = geolocationSwitch.isChecked();

        // Create event using static method with correct parameter order
        Event newEvent = Event.createEventInDatabase(
                capacity, dateTime, description, facilityLocation,
                facilityName, geolocationEnabled, name,
                regDeadline, waitListCapacity, userDeviceId
        );

        if (newEvent != null) {
            Toast.makeText(getActivity(), "Event created successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        } else {
            Toast.makeText(getActivity(), "Error creating event", Toast.LENGTH_SHORT).show();
        }
    }
}