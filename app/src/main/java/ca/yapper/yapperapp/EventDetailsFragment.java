package ca.yapper.yapperapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.OrganizerFragments.OrganizerCreateEditEventFragment;
import ca.yapper.yapperapp.OrganizerFragments.ViewParticipantsFragment;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.OrganizerFragments.OrganizerQRCodeViewFragment;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;

/**
 * The EventDetailsFragment class displays detailed information about a specific event.
 * This fragment retrieves event details from Firestore and displays them to the user.
 */
public class EventDetailsFragment extends Fragment {

    //private FirebaseFirestore db;
    private String eventId;
    private TextView nameTextView, dateTextView, regDeadlineTextView, facilityNameTextView, facilityLocationTextView, descriptionTextView, capacityTextView, waitListTextView, timeTextView;
    private TextView geolocEnabledTextView;
    boolean geolocationEnabled;
    private Button joinButton; // Entrant Button:
    private Button viewParticipantsButton; // Organizer Buttons:
    private Button editEventButton;
    private Button viewQRCodeButton;
    private String userDeviceId;
    private boolean isInEntrantActivity = false;
    private boolean isInOrganizerActivity = false;
    private Bundle QRCodeData;
    private View view;
    private boolean geolocationPermitted = false;

    /**
     * Inflates the layout for the event details, initializes views, and loads event details from the database.
     * It also sets visibility for buttons based on the activity (Entrant or Organizer).
     *
     * @param inflater The LayoutInflater object to inflate the layout.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState The saved instance state, if any, from the previous instance of the fragment.
     * @return The root view of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.event_details, container, false);

        //db = FirebaseFirestore.getInstance();
        Bundle args = getArguments();
        if (args == null || !args.containsKey("0")) {
            Toast.makeText(getContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
            return view;
        }

        eventId = args.getString("0");
        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        initializeViews(view);
        setVisibilityBasedOnActivity();  // This now sets the boolean values

        if (eventId != null && !eventId.isEmpty()) {
            loadEventDetails();
        }
        else {
            Toast.makeText(getContext(), "Error: Invalid event ID", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Initializes all the UI elements (TextViews, Buttons) from the fragment layout.
     *
     * @param view The root view of the fragment's layout.
     */
    private void initializeViews(View view) {
        nameTextView = view.findViewById(R.id.event_title);
        dateTextView = view.findViewById(R.id.event_date_time);
        regDeadlineTextView = view.findViewById(R.id.registration_deadline);
        facilityNameTextView = view.findViewById(R.id.facility_name);
        facilityLocationTextView = view.findViewById(R.id.facility_name);
        descriptionTextView = view.findViewById(R.id.event_description);
        capacityTextView = view.findViewById(R.id.event_number_participants);
        waitListTextView = view.findViewById(R.id.event_wl_capacity);
        // *to add: available slots text view (waitlist capacity - number of users in waitlist)*

        joinButton = view.findViewById(R.id.join_button); // only visible to Entrant:
        viewParticipantsButton = view.findViewById(R.id.button_view_participants); // only visible to Organizer:
        editEventButton = view.findViewById(R.id.button_edit_event);
        viewQRCodeButton = view.findViewById(R.id.button_view_QRCode);
    }

    /**
     * Loads the details of the event from the Firestore database and populates the corresponding UI elements.
     * If geolocation is enabled for the event, the UI is updated to show a warning about geolocation requirements.
     */
    private void loadEventDetails() {
        Log.d("EventDebug", "Loading event with ID: " + eventId);
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                if (getContext() == null) return;

                nameTextView.setText(event.getName());
                dateTextView.setText(event.getDate_Time());
                regDeadlineTextView.setText("Registration Deadline: " + event.getRegistrationDeadline());
                facilityNameTextView.setText("Facility: " + event.getFacilityName());
                facilityLocationTextView.setText("Location: " + event.getFacilityLocation());
                waitListTextView.setText("Waiting List Capacity: " + event.getWaitListCapacity());
                capacityTextView.setText("No. of attendees: " + event.getCapacity());
                descriptionTextView.setText("Description: " + event.getDescription());

                geolocationEnabled = event.isGeolocationEnabled();
                if (geolocationEnabled) {
                    TextView geoLocationRequired = view.findViewById(R.id.geo_location_required);
                    if (geoLocationRequired != null) {
                        geoLocationRequired.setVisibility(View.VISIBLE);
                    }
                }

                String description = event.getDescription();
                if (description != null && !description.isEmpty()) {
                    descriptionTextView.setText(description);
                } else {
                    descriptionTextView.setText("No description provided.");
                }

                setupButtonListeners();
            }

            @Override
            public void onEventLoadError(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Error loading event: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up button listeners based on the activity type (Entrant or Organizer).
     *
     * If the user is in the Entrant activity, it enables the join/unjoin button and checks if the user is already in the list.
     * If the user is in the Organizer activity, it enables buttons to view participants, edit the event, or view the QR code.
     */
    private void setupButtonListeners() {
        Log.d("setupbuttonlisteners", "Setting up button listeners");

        if (isInEntrantActivity) {
            Log.d("Activity", "User is in Entrant Activity");
            checkUserInList();
            joinButton.setEnabled(true);
            joinButton.setOnClickListener(v -> handleJoinButtonClick());
        } else if (isInOrganizerActivity) {
            viewParticipantsButton.setOnClickListener(v -> handleViewParticipantsButtonClick());
            editEventButton.setOnClickListener(v -> handleEditEventButtonClick());
            viewQRCodeButton.setOnClickListener(v -> viewQRCodeButtonClick());
        }
    }

    /**
     * Checks if the user is already in the event's waiting list or selected list and updates the button state accordingly.
     */
    private void checkUserInList() {
        OrganizerDatabase.checkUserInEvent(eventId, userDeviceId, new OrganizerDatabase.OnUserCheckListener() {
            @Override
            public void onUserInList(boolean inList) {
                if (inList) {
                    setButtonState("Unjoin", Color.GRAY);
                } else {
                    setButtonState("Join", Color.BLUE);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error checking user list: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the join button click. If the user clicks "Join", it checks if geolocation is required.
     * If geolocation is required, it prompts the user to confirm. If not, it proceeds to join the event.
     * If the button displays "Unjoin", it unjoins the user from the event.
     */
    private void handleJoinButtonClick() {
        Log.d("EventDetailsFragment", "Join button clicked");
        if (joinButton.getText().equals("Join")) {
            if (geolocationEnabled) {
                Log.d("EventDetailsFragment", "Geolocation required, showing dialog");
                showGeolocationWarningDialog();
            } else {
                Log.d("EventDetailsFragment", "Joining event directly");
                joinEvent();
            }
        } else {
            Log.d("EventDetailsFragment", "Unjoining event");
            unjoinEvent();
        }
    }

    /**
     * Sets the state of the join button (text and background color).
     *
     * @param text The text to be displayed on the button.
     * @param color The background color of the button.
     */
    private void setButtonState(String text, int color) {
        joinButton.setText(text);
        joinButton.setBackgroundColor(color);
    }

    /**
     * Handles the "View Participants" button click. Opens a new fragment to display the list of participants.
     */
    private void handleViewParticipantsButtonClick() {
        ViewParticipantsFragment viewParticipantsFragment = new ViewParticipantsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId); // Pass to ViewParticipantsFragment
        viewParticipantsFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, viewParticipantsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Handles the "Edit Event" button click. (This feature is currently to be implemented.)
     */
    private void handleEditEventButtonClick() {
        OrganizerCreateEditEventFragment editEventFragment = new OrganizerCreateEditEventFragment();
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId); // Pass eventId to the EditEventFragment
        editEventFragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editEventFragment)
                .addToBackStack(null) // Add to backstack to allow returning to EventDetailsFragment
                .commit();
    }


    /**
     * Handles the "View QR Code" button click. Opens a fragment to display the event's QR code.
     */
    private void viewQRCodeButtonClick() {
        QRCodeData = new Bundle();
        QRCodeData.putString("0", eventId);

        OrganizerQRCodeViewFragment newFragment = new OrganizerQRCodeViewFragment();
        newFragment.setArguments(QRCodeData);
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
    }

    /**
     * Shows a dialog to warn the user that geolocation is required for this event.
     * If the user confirms, the event will be joined.
     */
    private void showGeolocationWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Warning: this event requires geolocation.")
                .setPositiveButton("Continue", (dialog, id) -> requestLocationPermission()) // confirm this still works
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * Joins the user to the event. It adds the user to the event's waiting list and to the user's list of joined events.
     * If successful, it updates the join button to display "Unjoin" and shows a success message.
     */
    private void joinEvent() {
            if (userDeviceId == null) {
                Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
                return;
            }
            if (geolocationEnabled && !geolocationPermitted) {
                showGeolocationWarningDialog();
                return;
            }
            /**
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                }
                            }
                        });
            }**/
            EntrantDatabase.joinEvent(eventId, userDeviceId, new EntrantDatabase.OnOperationCompleteListener() {
                @Override
                public void onComplete(boolean success) {
                    if (getContext() == null) return;

                    if (success) {
                        joinButton.setText("Unjoin");
                        joinButton.setBackgroundColor(Color.GRAY);
                        Toast.makeText(getContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error joining the event. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    /**
     * Unjoins the user from the event. It removes the user from both the event's waiting list and their list of joined events.
     * If successful, it updates the join button to display "Join" and shows a success message.
     */
    private void unjoinEvent() {
        if (userDeviceId == null) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        EntrantDatabase.unjoinEvent(eventId, userDeviceId, new EntrantDatabase.OnOperationCompleteListener() {
            @Override
            public void onComplete(boolean success) {
                if (getContext() == null) return;

                if (success) {
                    joinButton.setText("Join");
                    joinButton.setBackgroundColor(Color.BLUE);
                    Toast.makeText(getContext(), "Successfully unjoined the event.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error unjoining the event. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        // Call UserDatabase to save the location
                        UserDatabase.saveLocationToFirestore(eventId, userDeviceId, latitude, longitude, new UserDatabase.OnLocationSavedListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Location saved successfully!", Toast.LENGTH_SHORT).show();
                                joinEvent(); // Proceed to join the event
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to retrieve location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        } else {
            geolocationPermitted = true;
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation(); // Retry getting location
            } else {
                Toast.makeText(getContext(), "Permission denied. Cannot join geolocation-enabled events.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sets the visibility of the UI elements based on the type of activity the user is in (Entrant or Organizer).
     *
     * If the user is in the Entrant activity, it shows the join button and hides organizer-specific buttons.
     * If the user is in the Organizer activity, it shows buttons for viewing participants, editing the event, and viewing the QR code.
     */
    private void setVisibilityBasedOnActivity() {
        if (getActivity() instanceof EntrantActivity) {
            isInEntrantActivity = true;
            isInOrganizerActivity = false;
            joinButton.setVisibility(View.VISIBLE);
            viewParticipantsButton.setVisibility(View.GONE);
            editEventButton.setVisibility(View.GONE);
            viewQRCodeButton.setVisibility(View.GONE);
            setupButtonListeners();
        } else if (getActivity() instanceof OrganizerActivity) {
            isInEntrantActivity = false;
            isInOrganizerActivity = true;
            joinButton.setVisibility(View.GONE);
            viewParticipantsButton.setVisibility(View.VISIBLE);
            editEventButton.setVisibility(View.VISIBLE);
            viewQRCodeButton.setVisibility(View.VISIBLE);
            setupButtonListeners();
        } else {
            isInEntrantActivity = false;
            isInOrganizerActivity = false;
            joinButton.setVisibility(View.GONE);
            viewParticipantsButton.setVisibility(View.GONE);
            editEventButton.setVisibility(View.GONE);
            viewQRCodeButton.setVisibility(View.GONE);
        }
    }
}