package ca.yapper.yapperapp.EntrantFragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ca.yapper.yapperapp.Databases.NotificationsDatabase;
import ca.yapper.yapperapp.NotificationAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;

/**
 * Fragment that displays the notification page for the entrant
 */
public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    public List<Notification> notificationList;
    private String userDeviceId;
    private TextView noNotificationsText;

    /**
     *
     * Inflates the fragments layout, sets up views, and starts showing the users notifications
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_notifications, container, false);

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        noNotificationsText = view.findViewById(R.id.no_notifications_text);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        loadNotifications();

        return view;
    }


    /**
     * This function updates the UI for the notifications page, depending on if the user has any notifications
     */
    private void loadNotifications() {
        NotificationsDatabase.loadNotifications(userDeviceId, new NotificationsDatabase.OnNotificationsLoadedListener() {
            /**
             * This function updates the UI when given notifications
             *
             * @param notifications a list of the users notifications
             */
            @Override
            public void onNotificationsLoaded(List<Notification> notifications) {
                notificationList.clear();
                notificationList.addAll(notifications);
                notificationAdapter.notifyDataSetChanged();
                toggleEmptyState();
            }

            /**
             * This function prints an error message in the log
             *
             * @param error a given error message
             */
            @Override
            public void onError(String error) {
                Log.e("NotificationsError", error);
            }
        });
    }
    /**
     * This function updates the UI depending on if a user has notifications or not
     */
    public void toggleEmptyState() {
        View noNotificationsLayout = getView().findViewById(R.id.no_notifications_layout);

        if (notificationList.isEmpty()) {
            noNotificationsLayout.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
        } else {
            noNotificationsLayout.setVisibility(View.GONE);
            notificationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}