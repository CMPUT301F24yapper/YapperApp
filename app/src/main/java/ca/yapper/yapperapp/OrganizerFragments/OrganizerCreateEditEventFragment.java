package ca.yapper.yapperapp.OrganizerFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import ca.yapper.yapperapp.Databases.OrganizerDatabase.OnOperationCompleteListener;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.EntrantFragments.ProfileFragment;
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
    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    //-------------------------------------------UI Components------------------------------------------------
    private CreateEventViewModel viewModel;
    private TextView dateTextView, timeTextView, regDeadlineTextView;
    private EditText eventNameEditText, eventCapacityEditText, eventWaitListCapacityEditText, eventDescriptionEditText;
    private String userDeviceId, selectedDate, selectedTime, regDeadline, facilityNameFinal, facilityAddressFinal;
    private Button dateButton, timeButton, regDeadlineButton, saveEventButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch geolocationSwitch;
    private boolean isCreatingEvent;


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



        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("eventId")) {
            isCreatingEvent = false;
            String eventId = bundle.getString("eventId");
            loadEventDetails(eventId);

        }
        else{
            isCreatingEvent = true;
        }

        initializeFields(view);
        setupClickListeners();

        saveEventButton.setOnClickListener(v -> {
            saveOrUpdateEvent();
        });

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            ImageView posterImageView = getView().findViewById(R.id.poster_image);
                            posterImageView.setImageURI(selectedImageUri); // Set the image URI
                            posterImageView.setTag(selectedImageUri); // Store the URI in the tag
                            viewModel.posterImageUri = selectedImageUri; // Cache the URI
                        }
                    }
                }
        );

        setupChoosePosterButton(view);
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

    /**
     * This function initializes the UI elements to create an event
     *
     * @param view the parent view
     */
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
        if (!isCreatingEvent) {
            eventCapacityEditText.setEnabled(false);
            eventWaitListCapacityEditText.setEnabled(false);
            //eventNameEditText.setEnabled((false));
        }
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
//--------------------------------------IMage-----------------------------------------------------------

    /**
     * This function sets up the button that allows users to upload a custom poster for their event
     *
     * @param view the parent view
     */
    private void setupChoosePosterButton(View view) {
        Button choosePosterButton = view.findViewById(R.id.choose_poster_button);
        choosePosterButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pickImageLauncher.launch(intent);
            } else {
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        });
    }

    /**
     * This function deals with the result from an activity
     *
     * @param requestCode The integer request code
     * @param resultCode The integer result code
     * @param data An Intent
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                ImageView posterImageView = getView().findViewById(R.id.poster_image);
                posterImageView.setImageURI(selectedImageUri);
            }
        }
    }

    /**
     * This function converts a poster to a bitmap and compresses, then stores it.
     *
     * @param posterUri Value of the poster to be stored
     * @param listener handles the outcome of the operation
     */
    private void uploadPosterImageAsBase64(Uri posterUri, OnOperationCompleteListener listener) {
        if (posterUri == null) {
            listener.onComplete(true); // No image to upload, continue saving other data
            return;
        }

        try {
            // Convert the image to Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(posterUri));

            // Compress the Bitmap
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Compress to 50% quality
            byte[] byteArray = outputStream.toByteArray();

            // Convert to Base64
            viewModel.posterImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            listener.onComplete(true);
        } catch (Exception e) {
            showToast("Failed to process poster image. Please try again.");
            listener.onComplete(false);
        }
    }


    //------------------------------------Time Picker----------------------------------------------------------


    /**
     * This function displays the pop up for a user to pick a date
     */
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
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();

    }


    /**
     * This function displays the pop up for a user to pick a time
     */
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


    /**
     * This function displays the pop up for a user to pick a deadline
     */
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


    /**
     * This function obtains the details for an event, and updates UI values
     *
     * @param eventId The unique id for the event, created from the QR code.
     */
    private void loadEventDetails(String eventId) {
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                regDeadline = event.getRegistrationDeadline();
                if (!TextUtils.isEmpty(regDeadline)) {
                    regDeadlineTextView.setText(regDeadline);
                } else {
                    regDeadlineTextView.setText("");
                }

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
                    try {
                        String[] dateTimeParts = dateTime.split(" ");
                        selectedDate = dateTimeParts[0];
                        selectedTime = dateTimeParts[1];

                        dateTextView.setText(selectedDate);
                        timeTextView.setText(selectedTime);
                    } catch (Exception e) {
                        selectedDate = null;
                        selectedTime = null;
                        dateTextView.setText("");
                        timeTextView.setText("");
                        Log.e("EventDetailsFragment", "Failed to parse date and time: " + e.getMessage());
                    }
                } else {
                    Log.w("EventDetailsFragment", "dateTime is null or not formatted correctly.");
                    selectedDate = null;
                    selectedTime = null;
                    dateTextView.setText("");
                    timeTextView.setText("");
                }

                // Load the poster image
                String posterBase64 = event.getPosterBase64();
                if (posterBase64 != null) {
                    byte[] decodedString = Base64.decode(posterBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ImageView posterImageView = requireView().findViewById(R.id.poster_image);
                    posterImageView.setImageBitmap(decodedBitmap); // Set the bitmap
                }
            }

            @Override
            public void onEventLoadError(String error) {
                Toast.makeText(getContext(), "Error loading event: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * This function saves an event based on what is happening to it, or it updates the event.
     */
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

                if (isCreatingEvent) {
                    createEvent();
                } else {
                    updateEvent();
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


    /**
     * This function contains the event saving process
     */
    private void processEventSave() {
        if (!validateInputs()) {
            return;
        }

        String dateTime = selectedDate + " " + selectedTime;

        if (!validateDates(dateTime, regDeadline)) {
            return;
        }

        int capacityInt = Integer.parseInt(eventCapacityEditText.getText().toString());
        Integer waitListCapacityInt = parseOptionalInt(eventWaitListCapacityEditText.getText().toString());

        if (waitListCapacityInt != null && waitListCapacityInt < capacityInt) {
            showToast("Waiting list capacity must be greater than or equal to the number of attendees.");
            return;
        }

        // Gather all event data
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        boolean isGeolocationEnabled = geolocationSwitch.isChecked();

        // Upload poster first
        uploadPosterImageAsBase64(viewModel.posterImageUri, success -> {
            if (!success) {
                saveEventButton.setEnabled(true);
                return;
            }

            // Use OrganizerDatabase to create the event and generate eventId
            OrganizerDatabase.createEventInDatabase(
                    capacityInt,
                    dateTime,
                    eventDescription,
                    facilityAddressFinal,
                    facilityNameFinal,
                    isGeolocationEnabled,
                    eventName,
                    regDeadline,
                    waitListCapacityInt != null ? waitListCapacityInt : 0,
                    userDeviceId,
                    viewModel.posterImageBase64,
                    success1 -> {
                        if (success1) {
                            showToast("Event saved successfully!");
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new OrganizerHomeFragment())
                                    .commit();
                        } else {
                            showToast("Failed to save event. Please try again.");
                        }
                        saveEventButton.setEnabled(true);
                    }
            );
        });
    }


    /**
     * This function creates an event in the database with all the relevant information
     */
    private void createEvent() {
        if (!validateInputs()) return;

        String dateTime = selectedDate + " " + selectedTime;
        if (!validateDates(dateTime, regDeadline)) return;

        int capacityInt = Integer.parseInt(eventCapacityEditText.getText().toString());
        Integer waitListCapacityInt = parseOptionalInt(eventWaitListCapacityEditText.getText().toString());

        if (waitListCapacityInt != null && waitListCapacityInt < capacityInt) {
            showToast("Waiting list capacity must be greater than or equal to the number of attendees.");
            return;
        }

        // Gather all event details
        String eventName = eventNameEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();
        boolean isGeolocationEnabled = geolocationSwitch.isChecked();

        // Upload poster first
        uploadPosterImageAsBase64(viewModel.posterImageUri, success -> {
            if (!success) {
                saveEventButton.setEnabled(true);
                return;
            }

            OrganizerDatabase.createEventInDatabase(
                    capacityInt,
                    dateTime,
                    eventDescription,
                    facilityAddressFinal,
                    facilityNameFinal,
                    isGeolocationEnabled,
                    eventName,
                    regDeadline,
                    waitListCapacityInt != null ? waitListCapacityInt : 0,
                    userDeviceId,
                    viewModel.posterImageBase64,
                    success1 -> {
                        if (success1) {
                            showToast("Event created successfully!");
                            navigateToHome();
                        } else {
                            showToast("Failed to create event. Please try again.");
                        }
                    }
            );
        });
    }


    /**
     * This function changes the fragment to the home fragment
     */
    private void navigateToHome() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OrganizerHomeFragment())
                .commit();
    }


    /**
     * This function prompts the user for facility details in the situation where they are missing required fields
     */
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


    /**
     * This function updates event details in the database
     */
    private void updateEvent() {
        if (!validateInputs()) return;

        String dateTime = selectedDate + " " + selectedTime;
        if (!validateDates(dateTime, regDeadline)) return;

        // Gather updated event details
        String updatedName = eventNameEditText.getText().toString();
        String updatedDescription = eventDescriptionEditText.getText().toString();
        boolean updatedGeolocationEnabled = geolocationSwitch.isChecked();

        // Upload updated poster
        uploadPosterImageAsBase64(viewModel.posterImageUri, success -> {
            if (!success) {
                saveEventButton.setEnabled(true);
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", updatedName);
            updates.put("description", updatedDescription);
            updates.put("date_Time", dateTime);
            updates.put("registrationDeadline", regDeadline);
            updates.put("isGeolocationEnabled", updatedGeolocationEnabled);

            if (viewModel.posterImageBase64 != null) {
                updates.put("posterBase64", viewModel.posterImageBase64);
            }

            FirebaseFirestore.getInstance().collection("Events").document(getArguments().getString("eventId"))
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Event updated successfully!");
                        navigateToHome();
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to update event: " + e.getMessage());
                    });
        });
    }


    /**
     * This function validates certain event inputs, including name, date, deadline, and capacity
     *
     * @return true if all fields are valid
     */
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


    /**
     * This function does extra validation on the date and registration deadline for the event
     *
     * @param dateTime event date
     * @param regDeadline registration deadline
     * @return true if all checks passed
     */
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

    private Integer parseOptionalInt(String input) {
        return TextUtils.isEmpty(input) ? null : Integer.parseInt(input);
    }

    /**
     * This function shows messages on the screen using Toast
     *
     * @param message a given message
     */
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    /**
     * This function allows users to upload images as posters to events
     *
     * @param posterUri the value for the poster
     * @param eventId The unique id for the event, created from the QR code.
     * @param listener handles the outcome of the operation
     */
    private void uploadPosterImage(Uri posterUri, String eventId, OnOperationCompleteListener listener) {
        if (posterUri == null) {
            listener.onComplete(true); // No image to upload, continue saving other data
            return;
        }

        String storagePath = "posters/" + eventId + ".jpg"; // Use eventId as the file name
        FirebaseStorage.getInstance().getReference(storagePath)
                .putFile(posterUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        viewModel.posterImageUrl = downloadUri.toString(); // Save URL in ViewModel
                        listener.onComplete(true);
                    });
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to upload poster image. Please try again.");
                    listener.onComplete(false);
                });
    }

    /**
     * Obtains an event id with the database
     *
     * @return generated event id
     */
    private String generateEventId() {
        return FirebaseFirestore.getInstance().collection("Events").document().getId();
    }

//----------------------------------------------------- Cache------------------------------------------

    /**
     * This function obtains cached data and updates the UI elements with it
     */
    private void restoreCachedData() {
        if (viewModel.eventName != null) eventNameEditText.setText(viewModel.eventName);
        if (viewModel.eventDescription != null) eventDescriptionEditText.setText(viewModel.eventDescription);

        if (viewModel.selectedDate != null) {
            dateTextView.setText(viewModel.selectedDate);
            selectedDate = viewModel.selectedDate; // Restore the variable
        }

        if (viewModel.selectedTime != null) {
            timeTextView.setText(viewModel.selectedTime);
            selectedTime = viewModel.selectedTime; // Restore the variable
        }

        if (viewModel.regDeadline != null) {
            regDeadlineTextView.setText(viewModel.regDeadline);
            regDeadline = viewModel.regDeadline; // Restore the variable
        }

        if (viewModel.capacity != null) eventCapacityEditText.setText(String.valueOf(viewModel.capacity));

        if (viewModel.waitListCapacity != null) {
            eventWaitListCapacityEditText.setText(String.valueOf(viewModel.waitListCapacity));
        }

        geolocationSwitch.setChecked(viewModel.geolocationEnabled);

        if (viewModel.posterImageUri != null) {
            ImageView posterImageView = requireView().findViewById(R.id.poster_image);
            posterImageView.setImageURI(viewModel.posterImageUri); // Set the image URI
            posterImageView.setTag(viewModel.posterImageUri); // Set the tag
        }
    }


    /**
     * This function obtains data from the UI elements and stores it in the view model
     */
    private void saveToCache() {
        viewModel.eventName = eventNameEditText.getText().toString();
        viewModel.eventDescription = eventDescriptionEditText.getText().toString();

        viewModel.selectedDate = dateTextView.getText().toString(); // Save the date from the TextView
        selectedDate = viewModel.selectedDate; // Update the variable

        viewModel.selectedTime = timeTextView.getText().toString(); // Save the time from the TextView
        selectedTime = viewModel.selectedTime; // Update the variable

        viewModel.regDeadline = regDeadlineTextView.getText().toString(); // Save the deadline
        regDeadline = viewModel.regDeadline; // Update the variable

        viewModel.capacity = TextUtils.isEmpty(eventCapacityEditText.getText())
                ? null : Integer.parseInt(eventCapacityEditText.getText().toString());

        viewModel.waitListCapacity = parseOptionalInt(eventWaitListCapacityEditText.getText().toString());
        viewModel.geolocationEnabled = geolocationSwitch.isChecked();

        ImageView posterImageView = requireView().findViewById(R.id.poster_image);
        viewModel.posterImageUri = (Uri) posterImageView.getTag(); // Save the tag holding the URI
    }
}