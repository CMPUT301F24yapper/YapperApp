package ca.yapper.yapperapp.EntrantFragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

import ca.yapper.yapperapp.R;

public class EntrantEventFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private TextView eventTitle, eventDateTime, eventFacility, eventDescription, availableSlots;
    private Button joinButton;
    private boolean geolocationEnabled;
    private String userDeviceId;
    private Bundle eventParameters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event, container, false);

        db = FirebaseFirestore.getInstance();
        eventParameters = getArguments();

        // Initialize views
        eventTitle = view.findViewById(R.id.event_title);
        eventDateTime = view.findViewById(R.id.event_date_time);
        eventFacility = view.findViewById(R.id.facility_name);
        eventDescription = view.findViewById(R.id.event_description);
        availableSlots = view.findViewById(R.id.available_slots);
        joinButton = view.findViewById(R.id.join_button);

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
            loadEventDetails();
        }

        // Join button click listener
        joinButton.setOnClickListener(v -> handleJoinButtonClick());

        return view;
    }

    private void loadEventDetails() {
        db.collection("Events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Safely get event details with null checks and default values
                String eventDescriptionString = documentSnapshot.getString("eventDescription");
                Long waitingListCapacityLong = documentSnapshot.getLong("waitingListCapacity");
                Long availableSlotsLong = documentSnapshot.getLong("availableSlots");
                Boolean geoEnabled = documentSnapshot.getBoolean("geolocationEnabled");

                // Convert Longs to ints with null checks
                int waitingListCapacity = waitingListCapacityLong != null ? waitingListCapacityLong.intValue() : 0;
                int availableSlotsCount = availableSlotsLong != null ? availableSlotsLong.intValue() : 0;

                // Update geolocationEnabled if value exists in Firestore
                if (geoEnabled != null) {
                    geolocationEnabled = geoEnabled;
                }

                // Set UI components with null checks
                if (eventDescriptionString != null) {
                    eventDescription.setText(eventDescriptionString);
                }

                availableSlots.setText(availableSlotsCount + " / " + waitingListCapacity);

                // Enable join button now that we have the data
                joinButton.setEnabled(true);

                // Check if the user is already in the waiting list
                checkUserInWaitingList();
            }
        }).addOnFailureListener(e -> {
            // Handle failure (e.g., show error message)
            joinButton.setEnabled(false);
            joinButton.setText("Error Loading Event");
        });
    }

    private void checkUserInWaitingList() {
        if (userDeviceId == null) return; // Add early return if userDeviceId is null

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Warning: this event requires geolocation.")
                .setPositiveButton("Continue", (dialog, id) -> joinEvent())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private void joinEvent() {
        if (userDeviceId == null) return; // Add early return if userDeviceId is null

        db.collection("Events").document(eventId).collection("waitingList").document(userDeviceId)
                .set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                    joinButton.setText("Unjoin");
                    joinButton.setBackgroundColor(Color.GRAY);
                }).addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void unjoinEvent() {
        if (userDeviceId == null) return; // Add early return if userDeviceId is null

        db.collection("Events").document(eventId).collection("waitingList").document(userDeviceId)
                .delete().addOnSuccessListener(aVoid -> {
                    joinButton.setText("Join");
                    joinButton.setBackgroundColor(Color.BLUE);
                }).addOnFailureListener(e -> {
                    // Handle failure
                });
    }
}