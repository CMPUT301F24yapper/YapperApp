package ca.yapper.yapperapp;

import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;

// class that pulls up Event details from Firestore for BOTH Entrants & Organizers
public class EventDetailsFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private TextView titleTextView, dateTimeTextView, regDeadlineTextView, facilityNameTextView, facilityLocationTextView, descriptionTextView, attendeesTextView, wlCapacityTextView;
    private TextView geolocEnabledTextView;
    Boolean geolocationEnabled;
    // Entrant Button:

    private Button joinButton;
    // Organizer Buttons:
    private Button viewParticipantsButton;
    private Button editEventButton;
    // later, we can implement Button QR Code view for Organizer
    private String userDeviceId;
    private Bundle eventParameters;
    private Boolean isInEntrantActivity;
    private Boolean isInOrganizerActivity;

    // ***TO-DO***:
    // IMPLEMENT EVENT POSTER PART OF EVENT FRAGMENT (UPLOAD EVENT POSTER LOGIC!)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event, container, false);

        db = FirebaseFirestore.getInstance();
        eventParameters = getArguments();
        eventId = eventParameters.getString("0");
        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize views (both Entrant & Organizer will see)
        initializeViews(view);

        setVisibilityBasedOnActivity();

        // load Event details
        loadEventDetails();

        return view;
    }

    private void initializeViews(View view) {
        titleTextView = view.findViewById(R.id.event_title);
        dateTimeTextView = view.findViewById(R.id.event_date_time);
        regDeadlineTextView = view.findViewById(R.id.registration_deadline);
        facilityNameTextView = view.findViewById(R.id.facility_name);
        facilityLocationTextView = view.findViewById(R.id.facility_name);
        descriptionTextView = view.findViewById(R.id.event_description);
        attendeesTextView = view.findViewById(R.id.event_number_participants);
        wlCapacityTextView = view.findViewById(R.id.event_wl_capacity);

        // only visible to Entrant:
        /** joinButton = view.findViewById(R.id.join_button);
        // only visible to Organizer:
        viewParticipantsButton = view.findViewById(R.id.button_view_participants);
        editEventButton = view.findViewById(R.id.button_edit_event); **/
    }

    private void loadEventDetails() {
        db.collection("Events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // get Event details
                String eventNameString = documentSnapshot.getString("eventName");
                String eventDateTimeString = documentSnapshot.getString("eventDateTime");
                String eventRegDeadlineString = documentSnapshot.getString("eventRegDeadline");
                // eventWlCapacity, eventWlSeatsLeft;
                String eventFacilityName = documentSnapshot.getString("eventFacilityName");
                String eventFacilityLocation = documentSnapshot.getString("eventFacilityLocation");
                String eventDescriptionString = documentSnapshot.getString("eventDescription");
                int eventWlCapacity = documentSnapshot.getLong("eventWlCapacity").intValue();
                int eventAttendees = documentSnapshot.getLong("eventWlAttendees").intValue();
                geolocationEnabled = documentSnapshot.getBoolean("geolocationEnabled");

                titleTextView.setText(eventNameString);
                dateTimeTextView.setText(eventDateTimeString);
                regDeadlineTextView.setText(eventRegDeadlineString);
                descriptionTextView.setText(eventDescriptionString);
                facilityNameTextView.setText(eventFacilityName);
                facilityLocationTextView.setText(eventFacilityLocation);
                wlCapacityTextView.setText(eventWlCapacity);
                attendeesTextView.setText(eventAttendees);
                if (geolocationEnabled == true) {
                    geolocEnabledTextView.setText("Geolocation Required");
                }

                // TO-DO: implement WL seats left (based on amount of seats left in waiting list capacity) logic:

                // IF ENTRANT:
                if (isInEntrantActivity) {
                    // check User in (any) event List before allowing Join
                    checkUserInList();
                    // Enable join button now that we have the data (confirm below!):
                    joinButton.setEnabled(true);
                    // Join button click listener
                    joinButton.setOnClickListener(v -> handleJoinButtonClick());
                }

                // IF ORGANIZER:
                if (isInOrganizerActivity) {
                    viewParticipantsButton.setOnClickListener(v -> handleViewParticipantsButtonClick());
                    editEventButton.setOnClickListener(v -> handleEditEventButtonClick());
                }

            }
        }).addOnFailureListener(e -> {
            // ***Handle failure (e.g., show error Toast message!)***
        });
    }

    private void checkUserInList() {
        if (userDeviceId == null) return; // Add early return if userDeviceId is null
        // Check if the user's device ID is in the waiting list (SUBCOLLECTION!)
        db.collection("Events").document(eventId).collection("waitingList")
                .document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User is already in the waiting list
                        joinButton.setText("Unjoin");
                        joinButton.setBackgroundColor(Color.GRAY);
                    } else {
                        // User is not in the waiting list
                        joinButton.setText("Join");
                        joinButton.setBackgroundColor(Color.BLUE);
                    }
                });

        db.collection("Events").document(eventId).collection("selectedList")
                .document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User is already in the selected list
                        joinButton.setText("Unjoin");
                        joinButton.setBackgroundColor(Color.GRAY);
                    } else {
                        // User is not in the waiting list
                        joinButton.setText("Join");
                        joinButton.setBackgroundColor(Color.BLUE);
                    }
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

    private void handleViewParticipantsButtonClick() {
        // **TO IMPLEMENT**
    }

    private void handleEditEventButtonClick() {
        // **TO IMPLEMENT**
    }

    private void showGeolocationWarningDialog() {
        // Create and show a dialog fragment (pop-up re geolocation enabled warning)
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Warning: this event requires geolocation.")
                .setPositiveButton("Continue", (dialog, id) -> joinEvent())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }
    // ***TO-DO FOR BELOW (JOIN) METHODS***:
    // IMPLEMENT THE ADDITIONAL DETAIL THAT WHEN ENTRANT JOINS/UNJOINS EVENT, THIS WILL UPDATE IN FIRESTORE + HOMEPAGE VIEW
    // ...RE. ENTRANT EVENT LISTS (REGISTERED EVENTS, MISSED OUT EVENTS)...
    private void joinEvent() {
        if (userDeviceId == null) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("timestamp", FieldValue.serverTimestamp()); // Add a server timestamp
        db.collection("Events").document(eventId).collection("waitingList")
                .document(userDeviceId).set(entrantData)
                .addOnSuccessListener(aVoid -> {
                    joinButton.setText("Unjoin");
                    joinButton.setBackgroundColor(Color.GRAY);
                    Toast.makeText(getContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
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
                    // Handle failure
                    Toast.makeText(getContext(), "Error unjoining the event. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setVisibilityBasedOnActivity() {
        // TO-DO: IMPLEMENT MAP VISIBILITY FOR ORGANIZER, AS WELL AS QR CODE VIEW BUTTON FOR ORGANIZER
        if (getActivity() instanceof EntrantActivity) {
            isInEntrantActivity = true;
            joinButton.setVisibility(View.VISIBLE);
            viewParticipantsButton.setVisibility(View.GONE);
            editEventButton.setVisibility(View.GONE);
        } else if (getActivity() instanceof OrganizerActivity) {
            isInOrganizerActivity = true;
            joinButton.setVisibility(View.GONE);
            viewParticipantsButton.setVisibility(View.VISIBLE);
            editEventButton.setVisibility(View.VISIBLE);
        } else {
            joinButton.setVisibility(View.GONE);
            viewParticipantsButton.setVisibility(View.GONE);
            editEventButton.setVisibility(View.GONE);
        }
    }
}