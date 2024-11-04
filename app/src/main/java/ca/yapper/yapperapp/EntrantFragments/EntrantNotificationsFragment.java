package ca.yapper.yapperapp.EntrantFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ca.yapper.yapperapp.NotificationAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;

public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_notifications, container, false);

        // Initialize RecyclerView
        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load data (this can be fetched from Firestore or a local database)
        notificationList = loadNotifications(); // Replace with actual data-fetching logic
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        return view;
    }

    private List<Notification> loadNotifications() {
        // Placeholder for loading notifications; replace with Firestore fetching logic
        List<Notification> notifications = new ArrayList<>();
        // Add sample notifications

        return notifications;
    }
}
