package ca.yapper.yapperapp.OrganizerFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.R;

public class CustomNotificationFragment extends Fragment {

    private EditText notificationTextBox;
    private Button sendNotificationButton;
    private String eventId;
    private String selectedList; // e.g., "waitingList", "selectedList", "cancelledList", "finalList"

    public static CustomNotificationFragment newInstance(String eventId, String selectedList) {
        CustomNotificationFragment fragment = new CustomNotificationFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("selectedList", selectedList);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_enter_notif, container, false);

        // Initialize views
        notificationTextBox = view.findViewById(R.id.notificationTextBox);
        sendNotificationButton = view.findViewById(R.id.sendNotificationButton);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            selectedList = getArguments().getString("selectedList");
        }

        sendNotificationButton.setOnClickListener(v -> sendNotification());

        return view;
    }

    private void sendNotification() {
        String notificationMessage = notificationTextBox.getText().toString().trim();

        if (notificationMessage.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a notification message.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (eventId == null || selectedList == null) {
            Toast.makeText(getContext(), "Invalid event or list selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch users from the selected list and send notifications
        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, selectedList, new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                for (String userId : userIdsList) {
                    OrganizerDatabase.sendNotificationToUser(eventId, userId, notificationMessage, success -> {
                        if (success) {
                            Toast.makeText(getContext(), "Notification sent successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to send notification.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading user list: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
