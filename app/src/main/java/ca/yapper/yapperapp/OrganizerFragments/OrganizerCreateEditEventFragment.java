package ca.yapper.yapperapp.OrganizerFragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.WriterException;

import java.util.Calendar;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

/**
 * OrganizerCreateEditEventFragment provides a form for organizers to create a new event.
 * It allows organizers to specify event details, including name, facility, date, capacity,
 * and geolocation options, and saves the event to Firestore.
 */
public class OrganizerCreateEditEventFragment extends Fragment {

    private EditText eventNameEditText;
    private TextView facilityNameTextView;
    private TextView facilityAddressTextView;
    private EditText eventDateEditText;
    private EditText eventTimeEditText;
    private EditText eventDeadlineEditText;
    private EditText eventCapacityEditText;
    private EditText eventWaitListCapacityEditText;
    private EditText eventDescriptionEditText;
    private Switch geolocationSwitch;
    private Button saveEventButton;
    private String userDeviceId;
    private String eventId;
    private String facilityNameFinal;
    private String facilityAddressFinal;
    private int selectedYear, selectedMonth, selectedDay;
    //private boolean exitCreation = false;

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
        View view = inflater.inflate(R.layout.organizer_create_edit_event, container, false);

        initializeFields(view);

        // Check if eventId is passed to edit an event
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("eventId")) {
            eventId = bundle.getString("eventId");
            loadEventDetails(eventId); // Load event details if editing
        }

        saveEventButton.setOnClickListener(v -> {
            try {
                saveOrUpdateEvent();
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }
        });

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
    private void initializeFields(View view) {
        eventNameEditText = view.findViewById(R.id.event_name_input);
        eventDescriptionEditText = view.findViewById(R.id.event_description);
        eventDateEditText = view.findViewById(R.id.date_input);
        eventTimeEditText = view.findViewById(R.id.time_input);
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
        eventDateEditText.setOnClickListener(v -> showDatePickerDialog(eventDateEditText));
        eventTimeEditText.setOnClickListener(v -> showTimePickerDialog(eventTimeEditText));
        eventDeadlineEditText.setOnClickListener(v -> showDatePickerDialog(eventDeadlineEditText));
        // save event click listener should be invoked AFTER all the (required) details have been entered in by organizer
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

    private void showTimePickerDialog(EditText timeEditText) {
        final Calendar calendar = Calendar.getInstance();
        int selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        int selectedMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                (view, hourOfDay, minute) -> {
                    // Format the time as HH:mm
                    String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                    timeEditText.setText(selectedTime);
                }, selectedHour, selectedMinute, true);  // true for 24-hour format, false for 12-hour format

        timePickerDialog.show();
    }

    private void setButtonState(String text, int color) {
        saveEventButton.setText(text);
        saveEventButton.setBackgroundColor(color);
    }

    /**
     * Validates the input fields and saves the event details to Firestore.
     * Displays confirmation messages if successful or error messages if fields are missing.
     */
    private void createEvent() {
        // Load facility details from Firestore
        OrganizerDatabase.loadFacilityData(userDeviceId, new OrganizerDatabase.OnFacilityDataLoadedListener() {
            @Override
            public void onFacilityDataLoaded(String facilityName, String facilityAddress) {
                if (facilityName == null || facilityAddress == null || facilityName.isEmpty() || facilityAddress.isEmpty()) {
                    Toast.makeText(getContext(), "Error: Facility details are required to create an event. Please update this in your profile.", Toast.LENGTH_LONG).show();
                    return;
                }
                String eventName = eventNameEditText.getText().toString();
                if (eventName.isEmpty()) {
                    Toast.makeText(getActivity(), "Event name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String dateTime = eventDateEditText.getText().toString();
                if (dateTime.isEmpty()) {
                    Toast.makeText(getActivity(), "Event date is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                String description = eventDescriptionEditText.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getActivity(), "Event description is required", Toast.LENGTH_SHORT).show();
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

                // Proceed with the rest of the event creation logic

                OrganizerDatabase.createEventInDatabase(
                        capacityInt,
                        dateTime,
                        description,
                        facilityAddress,
                        facilityName,
                        geolocationEnabled,
                        eventName,
                        regDeadline,
                        waitListCapacityInt,
                        userDeviceId
                );

                // Disable the save button and change color after event creation
                saveEventButton.setEnabled(false);
                setButtonState("Event saved", Color.GRAY);

                // Show success message
                Toast.makeText(getActivity(), "Event created successfully!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: you have not saved your Facility details. Please update this in your profile.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadEventDetails(String eventId) {
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                eventNameEditText.setText(event.getName());
                eventDateEditText.setText(event.getDate_Time());
                eventDeadlineEditText.setText(event.getRegistrationDeadline());
                eventCapacityEditText.setText(event.getCapacity());
                if (event.getWaitListCapacity() != null) {
                    eventWaitListCapacityEditText.setText(event.getWaitListCapacity());
                }
                geolocationSwitch.setChecked(event.isGeolocationEnabled());
            }
            @Override
            public void onEventLoadError(String error) {
                // error handling here!
            }
        });
    }

    private void saveOrUpdateEvent() throws WriterException {
        // Load facility details from Firestore
        OrganizerDatabase.loadFacilityData(userDeviceId, new OrganizerDatabase.OnFacilityDataLoadedListener() {
            @Override
            public void onFacilityDataLoaded(String facilityName, String facilityAddress) {
                facilityNameFinal = facilityName;
                facilityAddressFinal = facilityAddress;
                }
            @Override
            public void onError(String error) {
                // implement error logic
            }
        });

        String eventName = eventNameEditText.getText().toString();
        if (eventName.isEmpty()) {
            Toast.makeText(getActivity(), "Event name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTime = eventDateEditText.getText().toString();
        if (dateTime.isEmpty()) {
            Toast.makeText(getActivity(), "Event date is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = eventDescriptionEditText.getText().toString();
        if (description.isEmpty()) {
            Toast.makeText(getActivity(), "Event description is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String regDeadline = eventDeadlineEditText.getText().toString();
        if (regDeadline.isEmpty()) {
            Toast.makeText(getActivity(), "Registration deadline is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int capacityInt;
        Integer waitListCapacityInt;
        String capacityString = eventCapacityEditText.getText().toString();
        if (capacityString.isEmpty()) {
            Toast.makeText(getActivity(), "Number of attendees is required", Toast.LENGTH_SHORT).show();
            return;
        } else {
            capacityInt = Integer.parseInt(capacityString);
        }

        String waitListCapacityString = eventWaitListCapacityEditText.getText().toString();
        if (waitListCapacityString.isEmpty()) {
            waitListCapacityInt = null;
        } else {
            waitListCapacityInt = Integer.parseInt(waitListCapacityString);
        }
        boolean geolocationEnabled = geolocationSwitch.isChecked();

        OrganizerDatabase.createEventInDatabase(
                capacityInt,
                dateTime,
                description,
                facilityAddressFinal,
                facilityNameFinal,
                geolocationEnabled,
                eventName,
                regDeadline,
                waitListCapacityInt,
                userDeviceId
        );
    }
}