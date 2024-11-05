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
        /** joinButton = view.findViewById(R.id.join_button);
        // only visible to Organizer:
        viewParticipantsButton = view.findViewById(R.id.button_view_participants);
        editEventButton = view.findViewById(R.id.button_edit_event); **/
    }

    private void loadEventDetails() {
        Log.d("EventDebug", "Loading event with ID: " + eventId);

        db.collection("Events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // get Event details
                String eventNameString = documentSnapshot.getString("eventName");
                String eventDateTimeString = documentSnapshot.getString("eventDateTime");
                String eventRegDeadlineString = documentSnapshot.getString("eventRegDeadline");
                String eventFacilityName = documentSnapshot.getString("eventFacilityName");
                String eventFacilityLocation = documentSnapshot.getString("eventFacilityLocation");
                String eventDescriptionString = documentSnapshot.getString("eventDescription");
                int eventWlCapacity = documentSnapshot.contains("waitListCapacity") ?
                                documentSnapshot.getLong("waitListCapacity").intValue() : 0;
                int eventCapacity = documentSnapshot.contains("capacity") ?
                                documentSnapshot.getLong("capacity").intValue() : 0;
                geolocationEnabled = documentSnapshot.getBoolean("isGeolocationEnabled") != null ?
                                documentSnapshot.getBoolean("isGeolocationEnabled") : false;

                titleTextView..setText(eventNameString != null ? eventNameString : "");
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
        eventTitle = view.findViewById(R.id.event_title);
        eventDateTime = view.findViewById(R.id.event_date_time);
        eventRegDeadline = view.findViewById(R.id.registration_deadline);
        eventFacilityName = view.findViewById(R.id.facility_name);
        eventFacilityLocation = view.findViewById(R.id.facility_location);
        eventDescription = view.findViewById(R.id.event_description);
        joinButton = view.findViewById(R.id.join_button);
        joinButton.setEnabled(false);
    }

    private void loadEventDetails() {
        Log.d("EventDebug", "Loading event with ID: " + eventId);

        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("EventDebug", "Document data: " + documentSnapshot.getData());

                        String eventNameString = documentSnapshot.getString("name");
                        String eventDateTimeString = documentSnapshot.getString("date_Time");
                        String eventRegDeadlineString = documentSnapshot.getString("registrationDeadline");
                        String facilityNameString = documentSnapshot.getString("facilityName");
                        String facilityLocationString = documentSnapshot.getString("facilityLocation");
                        int eventWlCapacity = documentSnapshot.contains("waitListCapacity") ?
                                documentSnapshot.getLong("waitListCapacity").intValue() : 0;
                        int eventCapacity = documentSnapshot.contains("capacity") ?
                                documentSnapshot.getLong("capacity").intValue() : 0;
                        geolocationEnabled = documentSnapshot.getBoolean("isGeolocationEnabled") != null ?
                                documentSnapshot.getBoolean("isGeolocationEnabled") : false;

                        eventTitle.setText(eventNameString != null ? eventNameString : "");
                        eventDateTime.setText(eventDateTimeString != null ? eventDateTimeString : "");
                        eventRegDeadline.setText("Registration Deadline: " +
                                (eventRegDeadlineString != null ? eventRegDeadlineString : ""));

                        eventFacilityName.setText("Facility: " +
                                (facilityNameString != null ? facilityNameString : ""));
                        eventFacilityLocation.setText("Location: " +
                                (facilityLocationString != null ? facilityLocationString : ""));

//                        int availableSlots = eventWlCapacity;
//                        eventWlAvailableSlots.setText("Available Slots: " +
//                                availableSlots + "/" + eventWlCapacity);

                        joinButton.setEnabled(true);

                        if (geolocationEnabled) {
                            TextView geoLocationRequired = view.findViewById(R.id.geo_location_required);
                            if (geoLocationRequired != null) {
                                geoLocationRequired.setVisibility(View.VISIBLE);
                            }
                        }

                        //checkUserInWaitingList();
                    } else {
                        Log.d("EventDebug", "No such document");
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDebug", "Error loading event", e);
                    Toast.makeText(getContext(), "Error loading event details", Toast.LENGTH_SHORT).show();
                });

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
                        // User is not in the selected list
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

//    private void checkUserInWaitingList() {
//        if (entrantDeviceId == null) return;
//
//        db.collection("Events").document(eventId).collection("waitingList")
//                .document(entrantDeviceId).get().addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        joinButton.setText("Unjoin");
//                        joinButton.setBackgroundColor(Color.GRAY);
//                    } else {
//                        joinButton.setText("Join");
//                        joinButton.setBackgroundColor(Color.BLUE);
//                    }
//                });
//    }

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