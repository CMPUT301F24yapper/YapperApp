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

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;

import ca.yapper.yapperapp.Databases.NotificationsDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.R;

public class CustomNotificationFragment extends Fragment {

    private EditText notificationTextBox;
    private Button sendNotificationButton;
    private TabLayout notificationTabLayout;
    private String eventId;
    private String selectedList = "waitingList"; // Default to "waitingList"

    public static CustomNotificationFragment newInstance(String eventId, String eventName) {
        CustomNotificationFragment fragment = new CustomNotificationFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("eventName", eventName); // Pass event name
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
        notificationTabLayout = view.findViewById(R.id.notificationTabLayout);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            String eventName = getArguments().getString("eventName"); // Retrieve event name
            requireActivity().setTitle(eventName); // Set title to the event name
        }

        setupTabLayout();
        sendNotificationButton.setOnClickListener(v -> sendNotification());

        return view;
    }

    private void setupTabLayout() {
        // Add tabs
        notificationTabLayout.addTab(notificationTabLayout.newTab().setText("Waiting"));
        notificationTabLayout.addTab(notificationTabLayout.newTab().setText("Selected"));
        notificationTabLayout.addTab(notificationTabLayout.newTab().setText("Final"));
        notificationTabLayout.addTab(notificationTabLayout.newTab().setText("Cancelled"));

        // Set default selected tab
        notificationTabLayout.selectTab(notificationTabLayout.getTabAt(0));

        // Handle tab selection
        notificationTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        selectedList = "waitingList";
                        break;
                    case 1:
                        selectedList = "selectedList";
                        break;
                    case 2:
                        selectedList = "finalList";
                        break;
                    case 3:
                        selectedList = "cancelledList";
                        break;
                }
                Log.d("CustomNotification", "Selected List: " + selectedList);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
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

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, selectedList, new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                for (String userId : userIdsList) {
                    NotificationsDatabase.saveToDatabase(
                            new Date(),                  // Timestamp
                            userId,                      // User to notify
                            null,                        // Sender ID (optional)
                            "Custom Notification",       // Notification title
                            notificationMessage,         // Notification message
                            "Custom",                    // Notification type
                            false,                       // isRead status
                            eventId,                     // Event ID
                            getArguments().getString("eventName") // Event name
                    );
                }
                Toast.makeText(getContext(), "Notifications sent successfully.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading user list: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }



}