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

//import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Image;
// new library import (in Gradle dependencies as well) from source: https://guides.codepath.com/android/Displaying-Images-with-the-Glide-Library
public class EntrantEventFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private TextView eventTitle, eventDateTime, eventFacility, eventDescription, availableSlots;
    private Image eventPoster;
    private Button joinButton;
    private boolean geolocationEnabled;
    private String userDeviceId;
    private Bundle eventParameters;

    // To-Do: make sure to implement edge case where User ALREADY joined Event & is viewing same Event
    // from QR code (Join button grey / onclicklistener turned off in this case!)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_event, container, false);

        eventTitle = view.findViewById(R.id.event_title);

        db = FirebaseFirestore.getInstance();
        eventParameters = getArguments();
        eventId = eventParameters.getString("0");
        // Initialize views

        //  eventTitle.setText(eventId);
        eventTitle = view.findViewById(R.id.event_title);
        eventDateTime = view.findViewById(R.id.event_date_time);
        eventFacility = view.findViewById(R.id.facility_name);
        eventDescription = view.findViewById(R.id.event_description);
        availableSlots = view.findViewById(R.id.available_slots);
        // eventPoster = view.findViewById(R.id.event_poster);
        joinButton = view.findViewById(R.id.join_button);

        // load database details for Event
        loadEventDetails();

        // join on click listener
        joinButton.setOnClickListener(v -> handleJoinButtonClick());

        return view;
    }
    private void loadEventDetails() {
        db.collection("Events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve event details
                String eventName = documentSnapshot.getString("eventName");
                String eventDateTimeString = documentSnapshot.getString("eventDateTime");
                // IMPORTANT: have to change how Facility is stored in Firestore through Event fields & update in code as well
                // i.e. it should be a reference to a FacilityId that we THEN retrieve the facilityName from
                // we should also include (in xml, classes, fragments, etc) the LOCATION of facility, which we will also retrieve from the facilityId reference field in Event
                String facilityName = documentSnapshot.getString("eventFacilityName"); // This assumes a string field
                String eventDescriptionString = documentSnapshot.getString("eventDescription");
                int waitingListCapacity = documentSnapshot.getLong("waitingListCapacity").intValue();
                int availableSlotsCount = documentSnapshot.getLong("availableSlots").intValue();
                geolocationEnabled = documentSnapshot.getBoolean("geolocationEnabled");

                // Set UI components
                eventTitle.setText(eventName);
                eventDateTime.setText(eventDateTimeString);
                eventFacility.setText(facilityName);
                eventDescription.setText(eventDescriptionString);
                availableSlots.setText(availableSlotsCount + " / " + waitingListCapacity);

                // Load the event poster image (if a reference is available)
                // loadEventPoster(documentSnapshot);

                // Check if the user is already in the waiting list
                checkUserInWaitingList();
            }
        }).addOnFailureListener(e -> {
            // Handle failure (e.g., show a toast message)
        });
    }

    /**
    private void loadEventPoster(DocumentSnapshot documentSnapshot) {
        // Assuming the poster is a reference to another document
        DocumentReference posterRef = documentSnapshot.getDocumentReference("eventPoster");
        if (posterRef != null) {
            posterRef.get().addOnSuccessListener(posterSnapshot -> {
                if (posterSnapshot.exists()) {
                    String imageUrl = posterSnapshot.getString("imageURI"); // Assuming this field exists
                    // Load the image using a library like Glide or Picasso
                    Glide.with(this).load(imageUrl).into(eventPoster);
                }
            });
        }
    } **/

    private void checkUserInWaitingList() {
        // Check if the user's device ID is in the waiting list
        db.collection("Events").document(eventId).collection("waitingList")
                .document(userDeviceId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User is already in the waiting list
                        joinButton.setText("Unjoin");
                        joinButton.setBackgroundColor(Color.GRAY); // Change button color
                    } else {
                        // User is not in the waiting list
                        joinButton.setText("Join");
                        joinButton.setBackgroundColor(Color.BLUE); // Default color
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
        // Create and show a dialog fragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Warning: this event requires geolocation.")
                .setPositiveButton("Continue", (dialog, id) -> joinEvent())
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private void joinEvent() {
        // Add user's device ID to the waiting list
        db.collection("Events").document(eventId).collection("waitingList").document(userDeviceId)
                .set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                    joinButton.setText("Unjoin");
                    joinButton.setBackgroundColor(Color.GRAY); // Change button color
                }).addOnFailureListener(e -> {
                    // Handle failure (e.g., show a toast message)
                });
    }

    private void unjoinEvent() {
        // Remove user's device ID from the waiting list
        db.collection("Events").document(eventId).collection("waitingList").document(userDeviceId)
                .delete().addOnSuccessListener(aVoid -> {
                    joinButton.setText("Join");
                    joinButton.setBackgroundColor(Color.BLUE); // Reset button color
                }).addOnFailureListener(e -> {
                    // Handle failure (e.g., show a toast message)
                });
    }
}
