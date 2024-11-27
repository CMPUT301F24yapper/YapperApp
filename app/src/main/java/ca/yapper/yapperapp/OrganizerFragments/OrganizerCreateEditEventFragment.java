package ca.yapper.yapperapp.OrganizerFragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.ProfileFragment;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

/**
 * OrganizerCreateEditEventFragment provides a form for organizers to create a new event.
 * It allows organizers to specify event details, including name, facility, date, capacity,
 * and geolocation options, and saves the event to Firestore.
 */
public class OrganizerCreateEditEventFragment extends Fragment {

    //------------------Constants----------------------------------------------------------------
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "hh:mm a";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
//-------------------------------------------UI Components------------------------------------------------
    private CreateEventViewModel viewModel;
    private TextView dateTextView, timeTextView, regDeadlineTextView;
    private EditText eventNameEditText, eventCapacityEditText, eventWaitListCapacityEditText, eventDescriptionEditText;
    private String userDeviceId, selectedDate, selectedTime, regDeadline, facilityNameFinal, facilityAddressFinal;
    private Button dateButton, timeButton, regDeadlineButton, saveEventButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch geolocationSwitch;


    /**
     * Inflates the fragment layout, initializes form fields, and sets up date pickers
     * and save button click listeners. Retrieves the user's device ID for event creation.
     *
     * @param inflater           LayoutInflater used to inflate the fragment layout.
     * @param container          The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @SuppressLint("HardwareIds")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_create_edit_event, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(CreateEventViewModel.class);

        initializeFields(view);
        setupClickListeners();

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("eventId")) {
            String eventId = bundle.getString("eventId");
            loadEventDetails(eventId);
        }

        saveEventButton.setOnClickListener(v -> {
            saveOrUpdateEvent();
        });

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeFields(view);
        restoreCachedData();
        setupClickListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToCache();
    }

    /**
     * Initializes the UI components of the event creation form, setting up EditText fields
     * for event details and other necessary views.
     *
     * @param view The root view of the fragment layout.
     */

    //---------------------------------------Initialization--------------------------------------------------------
    private void initializeFields(View view) {
        eventNameEditText = view.findViewById(R.id.event_name_input);
        eventDescriptionEditText = view.findViewById(R.id.event_description);
        dateButton = view.findViewById(R.id.date_button);
        timeButton = view.findViewById(R.id.time_button);
        regDeadlineButton = view.findViewById(R.id.reg_deadline_button);
        dateTextView = view.findViewById(R.id.date_textview);
        timeTextView = view.findViewById(R.id.time_textview);
        regDeadlineTextView = view.findViewById(R.id.reg_deadline_textview);
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
        dateButton.setOnClickListener(v -> openDatePicker());
        timeButton.setOnClickListener(v -> openTimePicker());
        regDeadlineButton.setOnClickListener(v -> openRegDeadlinePicker());
    }

    //------------------------------------Time Picker----------------------------------------------------------
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(calendar.getTime());
                    dateTextView.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()); // Disable past dates
        datePickerDialog.show();

    }

    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    selectedTime = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(calendar.getTime());
                    timeTextView.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void openRegDeadlinePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    regDeadline = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(calendar.getTime());
                    regDeadlineTextView.setText(regDeadline);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    //------------------------------------DataBase Operation-------------------------------------------------

    private void loadEventDetails(String eventId) {
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                eventNameEditText.setText(event.getName());
                regDeadlineTextView.setText(event.getRegistrationDeadline());
                eventCapacityEditText.setText(String.valueOf(event.getCapacity()));

                if (event.getWaitListCapacity() != null) {
                    eventWaitListCapacityEditText.setText(String.valueOf(event.getWaitListCapacity()));
                }

                eventDescriptionEditText.setText(event.getDescription());
                geolocationSwitch.setChecked(event.isGeolocationEnabled());

                // Parse the concatenated dateTime string
                String dateTime = event.getDate_Time();
                if (dateTime != null && dateTime.contains(" ")) {
                    String[] dateTimeParts = dateTime.split(" ");
                    selectedDate = dateTimeParts[0]; // Extract date
                    selectedTime = dateTimeParts[1]; // Extract time

                    // Update the UI with the parsed values
                    dateTextView.setText(selectedDate);
                    timeTextView.setText(selectedTime);
                }
            }

            @Override
            public void onEventLoadError(String error) {
                // error handling here!
                Toast.makeText(getContext(), "Error loading event: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrUpdateEvent() {
        saveEventButton.setEnabled(false);

        OrganizerDatabase.loadFacilityData(userDeviceId, new OrganizerDatabase.OnFacilityDataLoadedListener() {
            @Override
            public void onFacilityDataLoaded(String facilityName, String facilityAddress) {
                if (TextUtils.isEmpty(facilityName) || TextUtils.isEmpty(facilityAddress)) {
                    promptAddFacilityDetails();
                    saveEventButton.setEnabled(true);
                    return;
                }
                facilityNameFinal = facilityName;
                facilityAddressFinal = facilityAddress;

                try {
                    processEventSave();
                } catch (WriterException e) {
                    // Handle WriterException gracefully
                    showToast("An error occurred while saving the event. Please try again.");
                }

                saveEventButton.setEnabled(true);
            }

            @Override
            public void onError(String error) {
                showToast("Error loading facility details. Please try again.");
                saveEventButton.setEnabled(true);
            }
        });
    }

    private void processEventSave() throws WriterException {
        // Validate all inputs
        if (!validateInputs()) {
            return;
        }

        // Concatenate Date and Time for the Event
        String dateTime = selectedDate + " " + selectedTime;

        // Check Dates
        if (!validateDates(dateTime, regDeadline)) {
            return;
        }

        // Parse numeric fields
        int capacityInt = Integer.parseInt(eventCapacityEditText.getText().toString());
        Integer waitListCapacityInt = parseOptionalInt(eventWaitListCapacityEditText.getText().toString());

        // Validate waitlist capacity
        if (waitListCapacityInt != null && waitListCapacityInt < capacityInt) {
            showToast("Waiting list capacity must be greater than or equal to the number of attendees.");
            return;
        } else if (waitListCapacityInt == null) {
            waitListCapacityInt = 0; // Default to 0 if no value is provided
        }

        // Save event to database
        OrganizerDatabase.createEventInDatabase(
                capacityInt,
                dateTime,
                eventDescriptionEditText.getText().toString(),
                facilityAddressFinal,
                facilityNameFinal,
                geolocationSwitch.isChecked(),
                eventNameEditText.getText().toString(),
                regDeadline,
                waitListCapacityInt,
                userDeviceId
        );

        showToast("Event saved successfully!");
    }


    private void promptAddFacilityDetails() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Missing Facility Details")
                .setMessage("Your profile is missing facility details. Please update your profile before creating an event.")
                .setPositiveButton("Update Facility", (dialog, which) -> {
                    // Programmatically select the Profile Fragment in the BottomNavigationView
                    BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
                    bottomNavigationView.setSelectedItemId(R.id.nav_organizer_profile);

                    // Switch to Profile Fragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    // Helper to validate input fields
    private boolean validateInputs() {
        if (TextUtils.isEmpty(eventNameEditText.getText())) {
            showToast("Event name is required.");
            return false;
        }
        if (TextUtils.isEmpty(selectedDate) || TextUtils.isEmpty(selectedTime)) {
            showToast("Please select both date and time for the event.");
            return false;
        }
        if (TextUtils.isEmpty(regDeadline)) {
            showToast("Please select a registration deadline.");
            return false;
        }
        if (TextUtils.isEmpty(eventCapacityEditText.getText())) {
            showToast("Number of attendees is required.");
            return false;
        }
        return true;
    }

    // Helper to validate dates
    private boolean validateDates(String dateTime, String regDeadline) {
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Calendar currentCalendar = Calendar.getInstance();
            Calendar eventCalendar = Calendar.getInstance();
            Calendar regDeadlineCalendar = Calendar.getInstance();

            eventCalendar.setTime(dateTimeFormat.parse(dateTime));
            regDeadlineCalendar.setTime(dateFormat.parse(regDeadline));

            if (!regDeadlineCalendar.after(currentCalendar)) {
                showToast("Registration deadline must be in the future.");
                return false;
            }
            if (!regDeadlineCalendar.before(eventCalendar)) {
                showToast("Registration deadline must be before the event date.");
                return false;
            }
            if (!eventCalendar.after(currentCalendar)) {
                showToast("Event date and time must be in the future.");
                return false;
            }
        } catch (Exception e) {
            showToast("Invalid date or time format.");
            return false;
        }
        return true;
    }

    // Helper to parse optional integers
    private Integer parseOptionalInt(String input) {
        return TextUtils.isEmpty(input) ? null : Integer.parseInt(input);
    }

    // Helper to show Toast messages
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

//----------------------------------------------------- Cache------------------------------------------

    private void restoreCachedData() {
        if (viewModel.eventName != null) eventNameEditText.setText(viewModel.eventName);
        if (viewModel.eventDescription != null) eventDescriptionEditText.setText(viewModel.eventDescription);
        if (viewModel.selectedDate != null) dateTextView.setText(viewModel.selectedDate);
        if (viewModel.selectedTime != null) timeTextView.setText(viewModel.selectedTime);
        if (viewModel.regDeadline != null) regDeadlineTextView.setText(viewModel.regDeadline);
        if (viewModel.capacity != null) eventCapacityEditText.setText(String.valueOf(viewModel.capacity));
        if (viewModel.waitListCapacity != null) {
            eventWaitListCapacityEditText.setText(String.valueOf(viewModel.waitListCapacity));
        }
        geolocationSwitch.setChecked(viewModel.geolocationEnabled);
    }

    private void saveToCache() {
        viewModel.eventName = eventNameEditText.getText().toString();
        viewModel.eventDescription = eventDescriptionEditText.getText().toString();
        viewModel.selectedDate = selectedDate;
        viewModel.selectedTime = selectedTime;
        viewModel.regDeadline = regDeadline;
        viewModel.capacity = TextUtils.isEmpty(eventCapacityEditText.getText())
                ? null : Integer.parseInt(eventCapacityEditText.getText().toString());
        viewModel.waitListCapacity = parseOptionalInt(eventWaitListCapacityEditText.getText().toString());
        viewModel.geolocationEnabled = geolocationSwitch.isChecked();
    }
}