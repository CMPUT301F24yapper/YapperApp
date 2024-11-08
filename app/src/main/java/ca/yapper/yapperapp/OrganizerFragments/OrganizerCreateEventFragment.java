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
    private EditText eventFacilityLocationEditText;
    private EditText eventDateTimeEditText;
    private EditText eventDeadlineEditText;
    private EditText eventCapacityEditText;
    private EditText eventWaitListCapacityEditText;
    private EditText eventDescriptionEditText;
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
        eventDateTimeEditText = view.findViewById(R.id.date_input);
        eventDeadlineEditText = view.findViewById(R.id.deadline_input);
        eventCapacityEditText = view.findViewById(R.id.attendees_input);
        eventWaitListCapacityEditText = view.findViewById(R.id.wl_capacity_input);
        geolocationSwitch = view.findViewById(R.id.geo_location_toggle);
        saveEventButton = view.findViewById(R.id.save_event_button);
    }

    private void setupClickListeners() {
        eventDateTimeEditText.setOnClickListener(v -> showDatePickerDialog(eventDateTimeEditText));
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
        String name = eventNameEditText.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Event name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTime = eventDateTimeEditText.getText().toString();
        if (dateTime.isEmpty()) {
            Toast.makeText(getActivity(), "Event date is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String regDeadline = eventDeadlineEditText.getText().toString();
        if (regDeadline.isEmpty()) {
            Toast.makeText(getActivity(), "Registration deadline is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacityInt;
        int waitListCapacityInt;
        String capacityString = eventCapacityEditText.getText().toString();
        if (capacityString.isEmpty()) {
            Toast.makeText(getActivity(), "Number of attendees is required", Toast.LENGTH_SHORT).show();
            return;
        } else {
            capacityInt = Integer.parseInt(capacityString);
        }

        String waitListCapacityString = eventWaitListCapacityEditText.getText().toString();
        if (waitListCapacityString.isEmpty()) {
            waitListCapacityInt = 0;
        } else {
            waitListCapacityInt = Integer.parseInt(waitListCapacityString);
        }

        boolean geolocationEnabled = geolocationSwitch.isChecked();

        Event.createEventInDatabase(
                capacityInt,
                dateTime,
                "",  // Empty description
                "Default Location",  // Default facility location
                "Default Facility", // Default facility name
                geolocationEnabled,
                name,
                regDeadline,
                waitListCapacityInt,
                userDeviceId
        );
    }
}