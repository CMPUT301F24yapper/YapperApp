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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;
import ca.yapper.yapperapp.OrganizerFragments.OrganizerQRCodeViewFragment;

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
    private Button viewQRCodeButton;

    private String userDeviceId;
    private Bundle eventParameters;
    private Boolean isInEntrantActivity;
    private Boolean isInOrganizerActivity;
    private Bundle QRCodeData;

    // ***TO-DO***:
    // IMPLEMENT EVENT POSTER PART OF EVENT FRAGMENT (UPLOAD EVENT POSTER LOGIC!)
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.entrant_event, container, false);

        db = FirebaseFirestore.getInstance();
        Bundle args = getArguments();
        if (args == null || !args.containsKey("0")) {
            Toast.makeText(getContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
            return view;
        }

        eventId = args.getString("0");
        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize views (both Entrant & Organizer will see)
        initializeViews(view);

        setVisibilityBasedOnActivity();

        // load Event details
        loadEventDetails();

        if (eventId != null && !eventId.isEmpty()) {
            loadEventDetails();
        } else {
            Toast.makeText(getContext(), "Error: Invalid event ID", Toast.LENGTH_SHORT).show();
        }

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
        joinButton = view.findViewById(R.id.join_button);
        // only visible to Organizer:
        viewParticipantsButton = view.findViewById(R.id.button_view_participants);
        editEventButton = view.findViewById(R.id.button_edit_event);
        viewQRCodeButton = view.findViewById(R.id.button_view_QRCode);
    }

    private void loadEventDetails() {
        Log.d("EventDebug", "Loading event with ID: " + eventId);

        db.collection("Events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // get Event details
                String eventNameString = documentSnapshot.getString("name");
                String eventDateTimeString = documentSnapshot.getString("dateTime");
                String eventRegDeadlineString = documentSnapshot.getString("regDeadline");
                String eventFacilityName = documentSnapshot.getString("facilityName");
                String eventFacilityLocation = documentSnapshot.getString("facilityLocation");
                String eventDescriptionString = documentSnapshot.getString("description");
                String eventWlCapacity = documentSnapshot.contains("wLCapacity") ?
                                Long.toString(documentSnapshot.getLong("wLCapacity")) : "0";
                String eventCapacity = documentSnapshot.contains("capacity") ?
                                Long.toString(documentSnapshot.getLong("capacity")) : "0";
                geolocationEnabled = documentSnapshot.getBoolean("isGeolocationEnabled") != null ?
                                documentSnapshot.getBoolean("isGeolocationEnabled") : false;
                int finalCapacity = Integer.parseInt(eventCapacity);
                int finalWlCapacity = Integer.parseInt(eventWlCapacity);

                titleTextView.setText(eventNameString != null ? eventNameString : "");
                dateTimeTextView.setText(eventDateTimeString != null ? eventDateTimeString : "");
                regDeadlineTextView.setText("Registration Deadline: " +
                (eventRegDeadlineString != null ? eventRegDeadlineString : ""));
                descriptionTextView.setText(eventDescriptionString);
                facilityNameTextView.setText("Facility: " + (eventFacilityName != null ? eventFacilityName : ""));           
                facilityLocationTextView.setText("Location: " + (eventFacilityLocation != null ? eventFacilityLocation : ""));
                wlCapacityTextView.setText(eventWlCapacity);
                attendeesTextView.setText(eventCapacity);

                if (geolocationEnabled) {
                    TextView geoLocationRequired = view.findViewById(R.id.geo_location_required);
                    if (geoLocationRequired != null) {
                        geoLocationRequired.setVisibility(View.VISIBLE);
                    }
                }

                // TO-DO: implement WL seats left (based on amount of seats left in waiting list capacity) logic:

                // IF ENTRANT:
                if (isInEntrantActivity == true) {
                    // check User in (any) event List before allowing Join
                    checkUserInList();
                    // Enable join button now that we have the data (confirm below!):
                    joinButton.setEnabled(true);
                    // Join button click listener
                    joinButton.setOnClickListener(v -> handleJoinButtonClick());
                }

                // IF ORGANIZER:
                else if (isInOrganizerActivity == true) {
                    viewParticipantsButton.setOnClickListener(v -> handleViewParticipantsButtonClick());
                    editEventButton.setOnClickListener(v -> handleEditEventButtonClick());
                    viewQRCodeButton.setOnClickListener(v -> viewQRCodeButtonClick());
                }
            }
            else {
                Log.d("EventDebug", "No such document");
                Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
            }

        }).addOnFailureListener(e -> {
            Log.e("EventDebug", "Error loading event", e);
            Toast.makeText(getContext(), "Error loading event details", Toast.LENGTH_SHORT).show();        });
    }

    private void checkUserInList() {
        if (userDeviceId == null) return; // Add early return if userDeviceId is null
        // Check if the user's device ID is in the waiting list (SUBCOLLECTION!)
        // CollectionReference eventsRef = db.collection("Events");
        db.collection("Events").document(eventId).collection("waitingList").document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User is already in the waiting list
                joinButton.setText("Unjoin");
                joinButton.setBackgroundColor(Color.GRAY);
            } else {
                // User is not in the waiting list
                joinButton.setText("Join");
                joinButton.setBackgroundColor(Color.BLUE);
            }
            // add log / toast messages for unsuccessful retrieval of document
        });

        db.collection("Events").document(eventId).collection("selectedList").document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User is already in the selected list
                joinButton.setText("Unjoin");
                joinButton.setBackgroundColor(Color.GRAY);
            } else {
                // User is not in the selected list
                joinButton.setText("Join");
                joinButton.setBackgroundColor(Color.BLUE);
            }
            // add log / toast messages for unsuccessful retrieval of document
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
            viewQRCodeButton.setVisibility(View.VISIBLE);
        } else {
            joinButton.setVisibility(View.GONE);
            viewParticipantsButton.setVisibility(View.GONE);
            editEventButton.setVisibility(View.GONE);
        }
    }
}