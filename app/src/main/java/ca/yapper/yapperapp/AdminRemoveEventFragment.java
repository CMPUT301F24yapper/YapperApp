package ca.yapper.yapperapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ca.yapper.yapperapp.AdminFragments.AdminSearchFragment;
import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.UMLClasses.Event;

public class AdminRemoveEventFragment extends Fragment {
    private String eventId;
    private TextView eventTitle, eventDate, facilityName, facilityLocation, organizerName,
            eventDescription, eventCapacity, waitlistCapacity;
    private Button removeEventButton, removeQRDataButton;
    private ImageView eventImage;
    private TextView changePicture, removePicture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_removeevent, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        initializeViews(view);
        loadEventDetails();
        setupButtons();

        return view;
    }

    private void initializeViews(View view) {
        eventTitle = view.findViewById(R.id.event_title);
        eventDate = view.findViewById(R.id.event_date);
        facilityName = view.findViewById(R.id.facility_name);
        facilityLocation = view.findViewById(R.id.facility_location);
        organizerName = view.findViewById(R.id.organizer_name);
        eventDescription = view.findViewById(R.id.event_description);
        eventCapacity = view.findViewById(R.id.event_capacity);
        waitlistCapacity = view.findViewById(R.id.waitlist_capacity);
        removeEventButton = view.findViewById(R.id.btn_remove_event);
        removeQRDataButton = view.findViewById(R.id.btn_remove_qr_data);
        eventImage = view.findViewById(R.id.event_image);
        changePicture = view.findViewById(R.id.change_picture);
        removePicture = view.findViewById(R.id.remove_picture);
    }

    private void loadEventDetails() {
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                eventTitle.setText(event.getName());
                eventDate.setText(event.getDate_Time());
                facilityName.setText(event.getFacilityName());
                facilityLocation.setText(event.getFacilityLocation());
                eventDescription.setText(event.getDescription());
                eventCapacity.setText(String.valueOf(event.getCapacity()));
                waitlistCapacity.setText(String.valueOf(event.getWaitListCapacity()));

                String posterBase64 = event.getPosterBase64();
                if (posterBase64 != null) {
                    byte[] decodedString = Base64.decode(posterBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    eventImage.setImageBitmap(bitmap);
                } else {
                    eventImage.setImageResource(R.drawable.event_image);
                }

                AdminDatabase.getOrganizerName(event.getOrganizerId(), name -> {
                    organizerName.setText(name);
                });
            }
            @Override
            public void onEventLoadError(String error) {}
        });
    }

    private void setupButtons() {
        removeEventButton.setOnClickListener(v -> handleEventDeletion());
        removeQRDataButton.setOnClickListener(v -> handleQRDataRemoval());
        changePicture.setOnClickListener(v -> handleChangePicture());
        removePicture.setOnClickListener(v -> handleRemovePicture());
    }

    private void handleEventDeletion() {
        AdminDatabase.removeEvent(eventId).addOnSuccessListener(aVoid -> {
            FragmentManager fm = getParentFragmentManager();
            fm.popBackStack();
            Fragment searchFragment = new AdminSearchFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .commit();
        });
    }

    private void handleQRDataRemoval() {
//        AdminDatabase.removeQRCodeData(eventId).addOnSuccessListener(aVoid -> {
//            // Optionally show a success message
//        });
    }

    private void handleChangePicture() {
        // Similar to profile picture change
        // This would typically open an image picker
    }

    private void handleRemovePicture() {
//        AdminDatabase.removeEventImage(eventId).addOnSuccessListener(aVoid -> {
//            eventImage.setImageResource(R.drawable.event_image);
//        });
    }
}