package ca.yapper.yapperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.UMLClasses.Event;

public class AdminRemoveEventFragment extends Fragment {
    private String eventId;
    private TextView eventTitle, eventDate, facilityName, organizerName;
    private Button removeEventButton;

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
        organizerName = view.findViewById(R.id.organizer_name);
        removeEventButton = view.findViewById(R.id.btn_remove_event);
    }

    private void loadEventDetails() {
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                eventTitle.setText(event.getName());
                eventDate.setText(event.getDate_Time());
                facilityName.setText("Facility: " + event.getFacilityName());
            }
            @Override
            public void onEventLoadError(String error) {}
        });
    }

    private void setupButtons() {
        removeEventButton.setOnClickListener(v -> {
            AdminDatabase.removeEvent(eventId).addOnSuccessListener(aVoid -> {
                getParentFragmentManager().popBackStack();
            });
        });
    }
}