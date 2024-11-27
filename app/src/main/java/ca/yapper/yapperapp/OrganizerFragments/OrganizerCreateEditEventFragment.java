package ca.yapper.yapperapp.OrganizerFragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

/**
 * OrganizerCreateEditEventFragment provides a form for organizers to create a new event.
 * It allows organizers to specify event details, including name, facility, date, capacity,
 * and geolocation options, and saves the event to Firestore.
 */
public class OrganizerCreateEditEventFragment extends Fragment {

//-------------------------------------------UI Components------------------------------------------------

    private TextView dateTextView, timeTextView, regDeadlineTextView;
    private EditText eventNameEditText;
    private EditText eventCapacityEditText;
    private EditText eventWaitListCapacityEditText;
    private EditText eventDescriptionEditText;
    private String userDeviceId;
    private String eventId;
    private String facilityNameFinal;
    private String facilityAddressFinal;
    private String selectedDate, selectedTime, regDeadline;
    private Button dateButton, timeButton, regDeadlineButton;
    private Switch geolocationSwitch;
    private Button saveEventButton;
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
        View view = inflater.inflate(R.layout.organizer_create_edit_event, container, false);

        initializeFields(view);
        setupClickListeners();

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("eventId")) {
            eventId = bundle.getString("eventId");
            loadEventDetails(eventId);
        }

        saveEventButton.setOnClickListener(v -> {
            try {
                saveOrUpdateEvent();
            } catch (WriterException e) {
                throw new RuntimeException(e);
            }
        });

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        return view;
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    // Store the selected date
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = dateFormat.format(calendar.getTime());
                    // Update the TextView with the selected date
                    dateTextView.setText(selectedDate);                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()); // Disable past dates
        datePickerDialog.show();
    }

    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // Use a spinner-style time picker
                (view, hourOfDay, minute) -> {
                    // Format and save selected time
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    selectedTime = timeFormat.format(calendar.getTime());

                    // Update the TextView with the selected time
                    timeTextView.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void openRegDeadlinePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    // Store the registration deadline
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    regDeadline = dateFormat.format(calendar.getTime());
                    // Update the TextView with the selected registration deadline
                    regDeadlineTextView.setText(regDeadline);                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()); // Disable past dates
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
                // implement /edit error logic
                Toast.makeText(getContext(), "Error loading facility data: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        String eventName = eventNameEditText.getText().toString();
        if (eventName.isEmpty()) {
            Toast.makeText(getActivity(), "Event name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        /*** String dateTime = eventDateEditText.getText().toString();
        if (dateTime.isEmpty()) {
            Toast.makeText(getActivity(), "Event date is required", Toast.LENGTH_SHORT).show();
            return;
        } **/

        if (TextUtils.isEmpty(selectedDate) || TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(getContext(), "Please select both date and time for the event", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(regDeadline)) {
            Toast.makeText(getContext(), "Please select a registration deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        // Concatenate Date and Time for the Event
        String dateTime = selectedDate + " " + selectedTime;

        // Parse Dates
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar currentCalendar = Calendar.getInstance();
        Calendar eventCalendar = Calendar.getInstance();
        Calendar regDeadlineCalendar = Calendar.getInstance();

        try {
            eventCalendar.setTime(dateTimeFormat.parse(dateTime));
            regDeadlineCalendar.setTime(dateFormat.parse(regDeadline));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Registration Deadline Validity
        if (!regDeadlineCalendar.after(currentCalendar)) {
            Toast.makeText(getContext(), "Registration deadline must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!regDeadlineCalendar.before(eventCalendar)) {
            Toast.makeText(getContext(), "Registration deadline must be before the event date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check Event Date and Time Validity
        if (!eventCalendar.after(currentCalendar)) {
            Toast.makeText(getContext(), "Event date and time must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = eventDescriptionEditText.getText().toString();
        if (description.isEmpty()) {
            Toast.makeText(getActivity(), "Event description is required", Toast.LENGTH_SHORT).show();
            return;
        }

        /**String regDeadline = eventDeadlineEditText.getText().toString();
        if (regDeadline.isEmpty()) {
            Toast.makeText(getActivity(), "Registration deadline is required", Toast.LENGTH_SHORT).show();
            return;
        }**/

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
            if (waitListCapacityInt < capacityInt) {
                Toast.makeText(getActivity(), "Waiting list capacity must be greater than or equal to the number of attendees.", Toast.LENGTH_SHORT).show();
                return;
            }
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