package ca.yapper.yapperapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.engine.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.OrganizerFragments.CustomNotificationFragment;
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

    private String eventId;
    private Event finalEvent;
    private TextView nameTextView, dateTextView, regDeadlineTextView, facilityNameTextView, facilityLocationTextView, descriptionTextView, capacityTextView, waitListTextView, organizerNameTextView;
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
    private final int wlSpotsLeft = -1;
    private ImageView posterImageView;
    private ImageView worldmap;
    private Button customNotificationButton;

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
        organizerNameTextView = view.findViewById(R.id.organizer_name);
        regDeadlineTextView = view.findViewById(R.id.registration_deadline);
        facilityNameTextView = view.findViewById(R.id.facility_name);
        facilityLocationTextView = view.findViewById(R.id.facility_location);
        descriptionTextView = view.findViewById(R.id.event_description);
        capacityTextView = view.findViewById(R.id.event_number_participants);
        waitListTextView = view.findViewById(R.id.event_wl_capacity);
        // *to add: available slots text view (waitlist capacity - number of users in waitlist)*
        joinButton = view.findViewById(R.id.join_button); // only visible to Entrant:
        viewParticipantsButton = view.findViewById(R.id.button_view_participants); // only visible to Organizer:
        editEventButton = view.findViewById(R.id.button_edit_event);
        viewQRCodeButton = view.findViewById(R.id.button_view_QRCode);
        posterImageView = view.findViewById(R.id.event_image);
        customNotificationButton = view.findViewById(R.id.button_custom_notification);

        worldmap = view.findViewById(R.id.world_map);
    }

    /**
     * Loads the details of the event from the Firestore database and populates the corresponding UI elements.
     * If geolocation is enabled for the event, the UI is updated to show a warning about geolocation requirements.
     */
    private void loadEventDetails() {
        Log.d("loadEventDetails()", "Loading event with ID: " + eventId);
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                if (getContext() == null) return;

                nameTextView.setText(event.getName());
                dateTextView.setText(event.getDate_Time());
                regDeadlineTextView.setText("Registration Deadline: " + event.getRegistrationDeadline());
                facilityNameTextView.setText("Facility: " + event.getFacilityName());
                facilityLocationTextView.setText("Location: " + event.getFacilityLocation());
                String organizerId = event.getOrganizerId();
                OrganizerDatabase.loadOrganizerData(organizerId, new OrganizerDatabase.OnOrganizerDetailsLoadedListener() {
                    @Override
                    public void onOrganizerLoaded(String organizerName) {
                        organizerNameTextView.setText("Organizer: " + organizerName);
                    }

                    @Override
                    public void onError(String message) {
                        // implement error logic here
                    }
                });

                if (event.getWaitListCapacity() == null) {
                    waitListTextView.setText("Not set");
                }
                else {
                    waitListTextView.setText(String.valueOf(event.getWaitListCapacity())); }

                capacityTextView.setText(String.valueOf(event.getCapacity()));
                descriptionTextView.setText(event.getDescription());

                geolocationEnabled = event.isGeolocationEnabled();
                if (geolocationEnabled) {
                    TextView geoLocationRequired = view.findViewById(R.id.geo_location_required);
                    if (geoLocationRequired != null) {
                        geoLocationRequired.setVisibility(View.VISIBLE);
                    }
                }

                String posterBase64 = event.getPosterBase64();
                if (posterBase64 != null && !posterBase64.isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.decode(posterBase64, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        posterImageView.setImageBitmap(decodedBitmap); // Display the decoded image
                    } catch (IllegalArgumentException e) {
                        Log.e("EventDetailsFragment", "Error decoding Base64 image: " + e.getMessage());
                        posterImageView.setBackgroundResource(R.drawable.event_image); // Placeholder image
                    }
                }
                eventId = event.getDocumentId();
                finalEvent = event;
                Log.i("loadEventDetails", "about to call checkUserInList, with finalEventId saved as: " + finalEvent.getDocumentId());
                eventId = event.getDocumentId();
                finalEvent = event;
                Log.i("loadEventDetails", "about to call checkUserInList, with finalEventId saved as: " + finalEvent.getDocumentId());

                // setupButtonListeners();
                Log.i("loadEventDetails", "checkUserInList being called on event");
                checkUserInList();  // Check user is in waiting list first

            }
            @Override
            public void onEventLoadError(String error) {
                Toast.makeText(getContext(), "Error loading event: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Checks if the user is already in the event's waiting list or selected list and updates the button state accordingly.
     */
    private void checkUserInList() {
        // Log.d("checkUserInList", "About to call OrganizerDb.checkUserInEvent with eventId & userId: " + eventId + userDeviceId);
        Log.d("checkUserInList", "About to call OrganizerDb.checkUserInEvent with eventId & userId: " + finalEvent.getDocumentId() + userDeviceId);
        // OrganizerDatabase.checkUserInEvent(event.getDocumentId(), userDeviceId, new OrganizerDatabase.OnUserCheckListener() {
        OrganizerDatabase.checkUserInEvent(finalEvent.getDocumentId(), userDeviceId, new OrganizerDatabase.OnUserCheckListener() {
            @Override
            public void onUserInList(boolean inList) {
                if (inList) {
                    setButtonState("Unjoin", R.color.unjoin_event, false);
                }
                else {
                    checkEventDates();  // then check event dates
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error checking user list: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkEventDates() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            Date currentDate = new Date();
            Date regDeadlineDate = dateFormat.parse(regDeadlineTextView.getText().toString().replace("Registration Deadline: ", ""));
            Date eventDate = dateTimeFormat.parse(dateTextView.getText().toString());

            if ((regDeadlineDate != null && regDeadlineDate.before(currentDate)) ||
                    (eventDate != null && eventDate.before(currentDate))) {
                // setButtonState("Event Passed", Color.RED, false);
                setButtonState("Event Passed", R.color.unjoin_event, false);
            }
            else {
                checkWaitListCapacity();  // Check if waitlist capacity is full
            }
        } catch (ParseException e) {
            Log.e("EventDetailsFragment", "Date parsing error: " + e.getMessage());
        }
    }

    private void checkWaitListCapacity() {
        // check if full
        // OrganizerDatabase.getWaitingListCount(event.getDocumentId(), new OrganizerDatabase.OnWaitListCountLoadedListener() {
        OrganizerDatabase.getWaitingListCount(finalEvent.getDocumentId(), new OrganizerDatabase.OnWaitListCountLoadedListener() {
            @Override
            public void onCountLoaded(int waitListCount) {
                if (finalEvent.getWaitListCapacity() != null
                        && finalEvent.getWaitListCapacity() != 0
                        && (finalEvent.getWaitListCapacity() - waitListCount) <= 0) {
                    // setButtonState("Wait List Full", Color.GRAY, false);
                    setButtonState("Wait List Full", R.color.unjoin_event, false);
                } else {
                    setButtonState("Join", Color.BLUE, true);
                    joinButton.setOnClickListener(v -> handleJoinButtonClick());
                }
            }
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error checking waitlist count: " + errorMessage, Toast.LENGTH_SHORT).show();
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
            // joinButton.setOnClickListener(v -> handleJoinButtonClick());

        } else if (isInOrganizerActivity) {
            Log.d("Activity", "User is in Organizer Activity");
            viewParticipantsButton.setOnClickListener(v -> handleViewParticipantsButtonClick());
            editEventButton.setOnClickListener(v -> handleEditEventButtonClick());
            viewQRCodeButton.setOnClickListener(v -> viewQRCodeButtonClick());
            customNotificationButton.setOnClickListener(v -> handleCustomNotificationButtonClick());
        }
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
    private void setButtonState(String text, int color, boolean enabled) {
        joinButton.setText(text);
        joinButton.setBackgroundColor(color);
        joinButton.setEnabled(enabled);
    }

    /**
     * Handles the "View Participants" button click. Opens a new fragment to display the list of participants.
     */
    private void handleViewParticipantsButtonClick() {
        ViewParticipantsFragment viewParticipantsFragment = new ViewParticipantsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", finalEvent.getDocumentId()); // Pass to ViewParticipantsFragment
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
        bundle.putString("eventId", finalEvent.getDocumentId()); // Pass eventId to the EditEventFragment
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
        OrganizerDatabase.checkQRCodeExists(eventId).addOnSuccessListener(exists -> {
            if (!exists) {
                Toast.makeText(getContext(), "QR code data has been deleted", Toast.LENGTH_SHORT).show();
                return;
            }

            QRCodeData = new Bundle();
            QRCodeData.putString("0", finalEvent.getDocumentId());

            OrganizerQRCodeViewFragment newFragment = new OrganizerQRCodeViewFragment();
            newFragment.setArguments(QRCodeData);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .commit();
        });
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
            Log.d("joinEvent()", "about to call EntrantDb.joinEvent() with eventId & userId: " + finalEvent.getDocumentId() + userDeviceId);
            EntrantDatabase.joinEvent(finalEvent.getDocumentId(), userDeviceId, success -> {
                if (getContext() == null) return;

                if (success) {
                    setButtonState("Unjoin", R.color.unjoin_event, false);
                    Toast.makeText(getContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error joining the event. Please try again.", Toast.LENGTH_SHORT).show();
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
        Log.d("unjoinEvent()", "about to call EntrantDb.unjoinEvent() with eventId & userId: " + finalEvent.getDocumentId() + userDeviceId);
        EntrantDatabase.unjoinEvent(finalEvent.getDocumentId(), userDeviceId, success -> {
            if (getContext() == null) return;

            if (success) {
                joinButton.setText("Join");
                joinButton.setBackgroundColor(Color.BLUE);
                Toast.makeText(getContext(), "Successfully unjoined the event.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error unjoining the event. Please try again.", Toast.LENGTH_SHORT).show();
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
                        UserDatabase.saveLocationToFirestore(finalEvent.getDocumentId(), userDeviceId, latitude, longitude, new UserDatabase.OnLocationSavedListener() {
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
    /**
     * Handles the "Custom Notification" button click.
     * Navigates to the CustomNotificationFragment, passing the event ID as an argument.
     */
    private void handleCustomNotificationButtonClick() {
        if (finalEvent == null || finalEvent.getDocumentId() == null || finalEvent.getName() == null) {
            Toast.makeText(getContext(), "Event details are not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomNotificationFragment customNotificationFragment = CustomNotificationFragment.newInstance(
                finalEvent.getDocumentId(),
                finalEvent.getName() // Pass event name
        );

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, customNotificationFragment)
                .addToBackStack(null) // Allow navigating back to EventDetailsFragment
                .commit();
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
    private void loadUserPins(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Events").document(eventId).collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<float[]> coordinates = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Double latitude = document.getDouble("latitude");
                        Double longitude = document.getDouble("longitude");

                        if (latitude != null && longitude != null) {
                            // Convert latitude/longitude to pixel positions
                            float[] pixelCoordinates = convertGeoToPixel(latitude.floatValue(), longitude.floatValue());
                            coordinates.add(pixelCoordinates);
                        } else {
                            Log.e("Firestore", "Missing coordinates for user: " + document.getId());
                        }
                    }

                    // Display all pins on the map
                    displayPinsOnMap(coordinates);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to load user pins: " + e.getMessage()));
    }

    private float[] convertGeoToPixel(float latitude, float longitude) {
        // Get dimensions of the map image
        int imageWidth = worldmap.getWidth();
        int imageHeight = worldmap.getHeight();

        // Normalize latitude and longitude to pixel positions
        float x = (longitude + 180) / 360 * imageWidth; // Normalize longitude to [0, 360]
        float y = (90 - latitude) / 180 * imageHeight;  // Normalize latitude to [0, 180]

        return new float[]{x, y};
    }

    private void displayPinsOnMap(List<float[]> coordinates) {
        WorldMapPinsOverlay pinsOverlay = getView().findViewById(R.id.pins_overlay);
        if (pinsOverlay != null) {
            pinsOverlay.setPinCoordinates(coordinates); // Pass all coordinates to overlay
        } else {
            Log.e("EventDetailsFragment", "Pins overlay not found!");
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
            worldmap.setVisibility(View.GONE);
            customNotificationButton.setVisibility(View.GONE); // Hide for entrants
        } else if (getActivity() instanceof OrganizerActivity) {
            isInEntrantActivity = false;
            isInOrganizerActivity = true;

            joinButton.setVisibility(View.GONE);
            viewParticipantsButton.setVisibility(View.VISIBLE);
            editEventButton.setVisibility(View.VISIBLE);
            viewQRCodeButton.setVisibility(View.VISIBLE);
            customNotificationButton.setVisibility(View.VISIBLE); // Show for organizers
            setupButtonListeners(); // Ensure Organizer buttons have listeners
            worldmap.setVisibility(View.VISIBLE);
            if(eventId != null) {
                loadUserPins(eventId);
            }

        } else {
            isInEntrantActivity = false;
            isInOrganizerActivity = false;

            joinButton.setVisibility(View.GONE);
            viewParticipantsButton.setVisibility(View.GONE);
            editEventButton.setVisibility(View.GONE);
            viewQRCodeButton.setVisibility(View.GONE);
            customNotificationButton.setVisibility(View.GONE); // Default hidden
        }
    }
}