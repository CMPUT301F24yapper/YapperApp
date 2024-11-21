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

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;
/**
 * OrganizerCreateEventFragment provides a form for organizers to create a new event.
 * It allows organizers to specify event details, including name, facility, date, capacity,
 * and geolocation options, and saves the event to Firestore.
 */
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


    /**
     * Inflates the fragment layout, initializes form fields, and sets up date pickers
     * and save button click listeners. Retrieves the user's device ID for event creation.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_createevent, container, false);

        initializeViews(view);
        setupClickListeners();

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        return view;
    }


    /**
     * Initializes the UI components of the event creation form, setting up EditText fields
     * for event details and other necessary views.
     *
     * @param view The root view of the fragment layout.
     */
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
    /**
     * Sets up listeners for the date and deadline fields, displaying a DatePickerDialog
     * when clicked, and configures the save button to trigger event creation.
     */
    private void setupClickListeners() {
        eventDateTimeEditText.setOnClickListener(v -> showDatePickerDialog(eventDateTimeEditText));
        eventDeadlineEditText.setOnClickListener(v -> showDatePickerDialog(eventDeadlineEditText));
        saveEventButton.setOnClickListener(v -> createEvent());
    }
    /**
     * Displays a DatePickerDialog, allowing the user to select a date for the event or registration deadline.
     *
     * @param dateEditText The EditText field where the selected date will be displayed.
     */
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


    /**
     * Validates the input fields and saves the event details to Firestore.
     * Displays confirmation messages if successful or error messages if fields are missing.
     */
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

        OrganizerDatabase.createEventInDatabase(
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