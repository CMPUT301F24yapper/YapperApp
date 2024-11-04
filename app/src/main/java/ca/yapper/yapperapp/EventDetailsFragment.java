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

public class EventDetailsFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private TextView eventTitle, eventDateTime, eventRegDeadline, eventFacilityName, eventFacilityLocation, eventDescription, eventWlAvailableSlots;
    private boolean geolocationEnabled;
    private Button joinButton;
    private String entrantDeviceId;
    private Bundle eventParameters;
    // ***TO-DO***:
    // IMPLEMENT EVENT POSTER PART OF EVENT FRAGMENT (UPLOAD EVENT POSTER LOGIC!)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event, container, false);

        db = FirebaseFirestore.getInstance();
        eventParameters = getArguments();
        eventId = eventParameters.getString("0");
        entrantDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize views
        eventTitle = view.findViewById(R.id.event_title);
        eventDateTime = view.findViewById(R.id.event_date_time);
        eventRegDeadline = view.findViewById(R.id.registration_deadline);
        eventFacilityName = view.findViewById(R.id.facility_name);
        eventFacilityLocation = view.findViewById(R.id.facility_name);
        eventDescription = view.findViewById(R.id.event_description);
        eventWlAvailableSlots = view.findViewById(R.id.available_slots);
        joinButton = view.findViewById(R.id.join_button);
        /**
        if (eventParameters != null) {
            // Get data from bundle
            eventId = eventParameters.getString("eventId", "");
            String name = eventParameters.getString("eventName", "");
            String dateTime = eventParameters.getString("eventDateTime", "");
            String facility = eventParameters.getString("eventFacility", "");
            geolocationEnabled = eventParameters.getBoolean("geolocationEnabled", false);

            // Set views with bundle data
            eventTitle.setText(name);
            eventDateTime.setText(dateTime);
            eventFacility.setText("Facility: " + facility);

            // Load additional details from Firestore
            loadEventDetails(); **/
        loadEventDetails();

        // Join button click listener
        joinButton.setOnClickListener(v -> handleJoinButtonClick());

        return view;
    }

    private void loadEventDetails() {
        db.collection("Events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // get Event details
                String eventNameString = documentSnapshot.getString("eventName");
                String eventDateTimeString = documentSnapshot.getString("eventDateTime");
                String eventRegDeadlineString = documentSnapshot.getString("eventRegDeadline");
                // eventFacilityName, eventFacilityLocation, eventDescription, eventWlCapacity, eventWlSeatsLeft;
                String eventDescriptionString = documentSnapshot.getString("eventDescription");
                int eventWlCapacity = documentSnapshot.getLong("eventWlCapacity").intValue();
                int eventWlSeatsLeft = documentSnapshot.getLong("eventWlSeatsLeft").intValue();
                geolocationEnabled = documentSnapshot.getBoolean("geolocationEnabled");

                eventTitle.setText(eventNameString);
                eventDateTime.setText(eventDateTimeString);
                eventRegDeadline.setText(eventRegDeadlineString);
                eventDescription.setText(eventDescriptionString);
                eventWlAvailableSlots.setText(eventWlSeatsLeft + "/" + eventWlCapacity);

                // retrieve facility details for firestore (reference to facilityId):
                DocumentReference facilityRef = documentSnapshot.getDocumentReference("facilityId");
                if (facilityRef != null) {
                    facilityRef.get().addOnSuccessListener(facilitySnapshot -> {
                        if (facilitySnapshot.exists()) {
                            // Retrieve facility details
                            String facilityNameString = facilitySnapshot.getString("facilityName");
                            String facilityLocationString = facilitySnapshot.getString("facilityLocation");

                            // Set UI components for facility details
                            eventFacilityName.setText(facilityNameString);
                            eventFacilityLocation.setText(facilityLocationString);
                        } else {
                            // eventFacility.setText("Facility details not found");
                        }
                    }).addOnFailureListener(e -> {
                        // eventFacility.setText("Error loading facility details");
                    });
                } else {
                    // eventFacility.setText("Facility reference not found");
                }

                checkUserInWaitingList();

                // Enable join button now that we have the data
                joinButton.setEnabled(true);

                // Check if the user is already in the waiting list
            }
        }).addOnFailureListener(e -> {
            // ***Handle failure (e.g., show error Toast message!)***
        });
    }

    private void checkUserInWaitingList() {
        if (entrantDeviceId == null) return; // Add early return if userDeviceId is null
        // Check if the user's device ID is in the waiting list (SUBCOLLECTION!)
        db.collection("Events").document(eventId).collection("waitingList")
                .document(entrantDeviceId).get().addOnSuccessListener(documentSnapshot -> {
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
        if (entrantDeviceId == null) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("timestamp", FieldValue.serverTimestamp()); // Add a server timestamp
        db.collection("Events").document(eventId).collection("waitingList")
                .document(entrantDeviceId).set(entrantData)
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
        if (entrantDeviceId == null) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Events").document(eventId).collection("waitingList")
                .document(entrantDeviceId).delete()
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
}