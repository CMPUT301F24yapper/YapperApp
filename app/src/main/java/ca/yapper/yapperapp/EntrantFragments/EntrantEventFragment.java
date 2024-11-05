package ca.yapper.yapperapp.EntrantFragments;

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

import ca.yapper.yapperapp.R;

public class EntrantEventFragment extends Fragment {
    private FirebaseFirestore db;
    private String eventId;
    private TextView eventTitle, eventDateTime, eventRegDeadline, eventFacilityName, eventFacilityLocation, eventDescription, eventWlAvailableSlots;
    private boolean geolocationEnabled;
    private Button joinButton;
    private String entrantDeviceId;
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
        entrantDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        initializeViews(view);

        if (eventId != null && !eventId.isEmpty()) {
            loadEventDetails();
        } else {
            Toast.makeText(getContext(), "Error: Invalid event ID", Toast.LENGTH_SHORT).show();
        }

        joinButton.setOnClickListener(v -> handleJoinButtonClick());

        return view;
    }

    private void initializeViews(View view) {
        eventTitle = view.findViewById(R.id.event_title);
        eventDateTime = view.findViewById(R.id.event_date_time);
        eventRegDeadline = view.findViewById(R.id.registration_deadline);
        eventFacilityName = view.findViewById(R.id.facility_name);
        eventFacilityLocation = view.findViewById(R.id.facility_location);
        eventDescription = view.findViewById(R.id.event_description);
        eventWlAvailableSlots = view.findViewById(R.id.available_slots);
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

                        int availableSlots = eventWlCapacity;
                        eventWlAvailableSlots.setText("Available Slots: " +
                                availableSlots + "/" + eventWlCapacity);

                        joinButton.setEnabled(true);

                        if (geolocationEnabled) {
                            TextView geoLocationRequired = view.findViewById(R.id.geo_location_required);
                            if (geoLocationRequired != null) {
                                geoLocationRequired.setVisibility(View.VISIBLE);
                            }
                        }

                        checkUserInWaitingList();
                    } else {
                        Log.d("EventDebug", "No such document");
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventDebug", "Error loading event", e);
                    Toast.makeText(getContext(), "Error loading event details", Toast.LENGTH_SHORT).show();
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
        if (entrantDeviceId == null) {
            Toast.makeText(getContext(), "Error: Device ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> entrantData = new HashMap<>();
        entrantData.put("timestamp", FieldValue.serverTimestamp());
        db.collection("Events").document(eventId).collection("waitingList")
                .document(entrantDeviceId).set(entrantData)
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
                    Toast.makeText(getContext(), "Error unjoining the event. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserInWaitingList() {
        if (entrantDeviceId == null) return;

        db.collection("Events").document(eventId).collection("waitingList")
                .document(entrantDeviceId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        joinButton.setText("Unjoin");
                        joinButton.setBackgroundColor(Color.GRAY);
                    } else {
                        joinButton.setText("Join");
                        joinButton.setBackgroundColor(Color.BLUE);
                    }
                });
    }
}