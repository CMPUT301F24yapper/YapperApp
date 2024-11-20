package ca.yapper.yapperapp;

import android.graphics.Color;
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
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.WaitingListFragment;
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
    private TextView nameTextView, dateTimeTextView, regDeadlineTextView, facilityNameTextView, facilityLocationTextView, descriptionTextView, capacityTextView, waitListTextView;
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
        dateTimeTextView = view.findViewById(R.id.event_date_time);
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

        /** Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                if (getContext() == null) return;

                nameTextView.setText(event.getName());
                dateTimeTextView.setText(event.getDate_Time());
                regDeadlineTextView.setText("Registration Deadline: " + event.getRegistrationDeadline());
                facilityNameTextView.setText("Facility: " + event.getFacilityName());
                facilityLocationTextView.setText("Location: " + event.getFacilityLocation());
                waitListTextView.setText(String.valueOf(event.getWaitListCapacity()));
                capacityTextView.setText(String.valueOf(event.getCapacity()));

                geolocationEnabled = event.isGeolocationEnabled();
                if (geolocationEnabled) {
                    TextView geoLocationRequired = view.findViewById(R.id.geo_location_required);
                    if (geoLocationRequired != null) {
                        geoLocationRequired.setVisibility(View.VISIBLE);
                    }
                }
                Log.d("EventLoadSuccess", "Loaded event with ID: " + eventId);
                setupButtonListeners();
            }

            @Override
            public void onEventLoadError(String error) {
                if (getContext() == null) return;

                Toast.makeText(getContext(), "Error loading event: " + error, Toast.LENGTH_SHORT).show();
                Log.e("EventDetails", "Error loading event: " + error);
            }
        }); **/
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                if (getContext() == null) return;

                nameTextView.setText(event.getName());
                dateTimeTextView.setText(event.getDate_Time());
                regDeadlineTextView.setText("Registration Deadline: " + event.getRegistrationDeadline());
                facilityNameTextView.setText("Facility: " + event.getFacilityName());
                facilityLocationTextView.setText("Location: " + event.getFacilityLocation());
                waitListTextView.setText(String.valueOf(event.getWaitListCapacity()));
                capacityTextView.setText(String.valueOf(event.getCapacity()));

                geolocationEnabled = event.isGeolocationEnabled();
                if (geolocationEnabled) {
                    TextView geoLocationRequired = view.findViewById(R.id.geo_location_required);
                    if (geoLocationRequired != null) {
                        geoLocationRequired.setVisibility(View.VISIBLE);
                    }
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
        // if (userDeviceId == null) return;
        /** Log.d("checkuserinlist", "checking if user in list (waiting or selected)");
        db.collection("Events").document(eventId)
                .collection("waitingList")
                .document(userDeviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("checkuserinlist", "user in wait list, setting button to unjoin");
                        setButtonState("Unjoin", Color.GRAY); // User is in the waiting list
                    } else {
                        // Check selected list if not in waiting list
                        db.collection("Events").document(eventId)
                                .collection("selectedList")
                                .document(userDeviceId)
                                .get()
                                .addOnSuccessListener(selectedSnapshot -> {
                                    if (selectedSnapshot.exists()) {
                                        Log.d("checkuserinlist", "user in selected list, setting button to unjoin");
                                        setButtonState("Unjoin", Color.GRAY); // User is in the selected list
                                    } else {
                                        setButtonState("Join", Color.BLUE); // User not found in either list
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EventDetails", "Error checking selected list: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDetails", "Error checking waiting list: " + e.getMessage());
                });
        Log.d("checkuserinlist", "user not in any list"); **/

    }


    /**
     * Handles the join button click. If the user clicks "Join", it checks if geolocation is required.
     * If geolocation is required, it prompts the user to confirm. If not, it proceeds to join the event.
     * If the button displays "Unjoin", it unjoins the user from the event.
     */
    private void handleJoinButtonClick() {
        Log.d("EventDetailsFragment", "Join button clicked");
        /**
        if (joinButton.getText().equals("Join")) {
            if (geolocationEnabled) {
                showGeolocationWarningDialog();
            } else {
                OrganizerDatabase.joinEvent(eventId, userDeviceId, success -> {
                    if (success) {
                        setButtonState("Unjoin", Color.GRAY);
                        Toast.makeText(getContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error joining the event.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            OrganizerDatabase.unjoinEvent(eventId, userDeviceId, success -> {
                if (success) {
                    setButtonState("Join", Color.BLUE);
                    Toast.makeText(getContext(), "Successfully unjoined the event.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error unjoining the event.", Toast.LENGTH_SHORT).show();
                }
            });
        } **/
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
        // **TO IMPLEMENT**
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
                .setPositiveButton("Continue", (dialog, id) -> joinEvent())
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
        OrganizerDatabase.OnOperationCompleteListener listener = null;
        OrganizerDatabase.joinEvent(eventId, userDeviceId, listener);
        /** 
        // Create timestamp data
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("timestamp", FieldValue.serverTimestamp());

        // Start a batch write
        WriteBatch batch = db.batch();

        // Add to event's waiting list
        DocumentReference eventWaitingListRef = db.collection("Events")
                .document(eventId)
                .collection("waitingList")
                .document(userDeviceId);
        batch.set(eventWaitingListRef, entrantData);

        // Add to user's joined events
        DocumentReference userJoinedEventsRef = db.collection("Users")
                .document(userDeviceId)
                .collection("joinedEvents")
                .document(eventId);
        batch.set(userJoinedEventsRef, entrantData); **/

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    joinButton.setText("Unjoin");
                    joinButton.setBackgroundColor(Color.GRAY);
                    Toast.makeText(getContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error joining the event. Please try again.", Toast.LENGTH_SHORT).show();
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

        // Start a batch write
        WriteBatch batch = db.batch();

        // Remove from event's waiting list
        DocumentReference eventWaitingListRef = db.collection("Events")
                .document(eventId)
                .collection("waitingList")
                .document(userDeviceId);
        batch.delete(eventWaitingListRef);

        // Remove from user's joined events
        DocumentReference userJoinedEventsRef = db.collection("Users")
                .document(userDeviceId)
                .collection("joinedEvents")
                .document(eventId);
        batch.delete(userJoinedEventsRef);

        // Commit the batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    joinButton.setText("Join");
                    joinButton.setBackgroundColor(Color.BLUE);
                    Toast.makeText(getContext(), "Successfully unjoined the event.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error unjoining the event. Please try again.", Toast.LENGTH_SHORT).show();
                });
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