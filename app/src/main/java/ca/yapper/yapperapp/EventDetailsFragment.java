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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.WaitingListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ViewParticipantsFragment;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.OrganizerFragments.OrganizerQRCodeViewFragment;

// class that pulls up Event details from Firestore for BOTH Entrants & Organizers
public class EventDetailsFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private TextView nameTextView, dateTimeTextView, regDeadlineTextView, facilityNameTextView, facilityLocationTextView, descriptionTextView, capacityTextView, waitListTextView;
    private TextView geolocEnabledTextView;
    boolean geolocationEnabled;

    // Entrant Button:
    private Button joinButton;

    // Organizer Buttons:
    private Button viewParticipantsButton;
    private Button editEventButton;
    private Button viewQRCodeButton;

    private String userDeviceId;
    private Bundle eventParameters;
    private boolean isInEntrantActivity = false;
    private boolean isInOrganizerActivity = false;
    private Bundle QRCodeData;

    // ***TO-DO***:
    // IMPLEMENT EVENT POSTER PART OF EVENT FRAGMENT (UPLOAD EVENT POSTER LOGIC!)
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.event_details, container, false);

        db = FirebaseFirestore.getInstance();
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
        } else {
            Toast.makeText(getContext(), "Error: Invalid event ID", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

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

        // only visible to Entrant:
        joinButton = view.findViewById(R.id.join_button);
        // only visible to Organizer:
        viewParticipantsButton = view.findViewById(R.id.button_view_participants);
        editEventButton = view.findViewById(R.id.button_edit_event);
        viewQRCodeButton = view.findViewById(R.id.button_view_QRCode);
    }

    private void loadEventDetails() {
        Log.d("EventDebug", "Loading event with ID: " + eventId);

        Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
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
                Log.e("EventDetails", "Error loading event: " + error);
            }
        });
    }

    private void setupButtonListeners() {
        if (isInEntrantActivity) {
            checkUserInList();
            // joinButton.setEnabled(true);
            joinButton.setOnClickListener(v -> handleJoinButtonClick());
        } else if (isInOrganizerActivity) {
            viewParticipantsButton.setOnClickListener(v -> handleViewParticipantsButtonClick());
            editEventButton.setOnClickListener(v -> handleEditEventButtonClick());
            viewQRCodeButton.setOnClickListener(v -> viewQRCodeButtonClick());
        }
    }

    private void checkUserInList() {
        if (userDeviceId == null) return;

        db.collection("Events").document(eventId)
                .collection("waitingList")
                .document(userDeviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        setButtonState("Unjoin", Color.GRAY); // User is in the waiting list
                    } else {
                        // Check selected list if not in waiting list
                        db.collection("Events").document(eventId)
                                .collection("selectedList")
                                .document(userDeviceId)
                                .get()
                                .addOnSuccessListener(selectedSnapshot -> {
                                    if (selectedSnapshot.exists()) {
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
    }

    private void handleJoinButtonClick() {
        if (joinButton.getText().equals("Join")) {
            if (geolocationEnabled) {
                showGeolocationWarningDialog();
            } else {
                joinEvent();
            }
        } else {
            unjoinEvent();
        }
    }

    // Helper method to set button state
    private void setButtonState(String text, int color) {
        joinButton.setText(text);
        joinButton.setBackgroundColor(color);
    }

    private void handleViewParticipantsButtonClick() {
        /// Create a new instance of ViewParticipantsFragment and pass the eventId
        ViewParticipantsFragment viewParticipantsFragment = new ViewParticipantsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId); // Pass eventId to ViewParticipantsFragment
        viewParticipantsFragment.setArguments(args);

        // Start the fragment transaction
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, viewParticipantsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void handleEditEventButtonClick() {
        // **TO IMPLEMENT**
    }

    private void viewQRCodeButtonClick() {
        QRCodeData = new Bundle();
        QRCodeData.putString("0", eventId); // Here we pass the eventID to display its QR code value
        // Then we swap to the fragment to view the QR Code
        OrganizerQRCodeViewFragment newFragment = new OrganizerQRCodeViewFragment();
        newFragment.setArguments(QRCodeData);
        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
    }

    private void showGeolocationWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Warning: this event requires geolocation.")
                .setPositiveButton("Continue", (dialog, id) -> joinEvent())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private void joinEvent() {
        if (userDeviceId == null) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("timestamp", FieldValue.serverTimestamp());
        db.collection("Events").document(eventId).collection("waitingList")
                .document(userDeviceId).set(entrantData)
                .addOnSuccessListener(aVoid -> {
                    joinButton.setText("Unjoin");
                    joinButton.setBackgroundColor(Color.GRAY);
                    Toast.makeText(getContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error joining the event. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void unjoinEvent() {
        if (userDeviceId == null) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Events").document(eventId).collection("waitingList")
                .document(userDeviceId).delete()
                .addOnSuccessListener(aVoid -> {
                    joinButton.setText("Join");
                    joinButton.setBackgroundColor(Color.BLUE);
                    Toast.makeText(getContext(), "Successfully unjoined the event.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error unjoining the event. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

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