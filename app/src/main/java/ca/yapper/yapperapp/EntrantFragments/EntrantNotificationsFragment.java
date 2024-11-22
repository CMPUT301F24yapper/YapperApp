package ca.yapper.yapperapp.EntrantFragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import ca.yapper.yapperapp.Databases.NotificationsDatabase;
import ca.yapper.yapperapp.NotificationAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;

public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private String userDeviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_notifications, container, false);

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        NotificationsDatabase.loadNotifications(userDeviceId, new NotificationsDatabase.OnNotificationsLoadedListener() {
            @Override
            public void onNotificationsLoaded(List<Notification> notifications) {
                notificationList.clear();
                notificationList.addAll(notifications);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Log.e("NotificationsError", error);
            }
        });
    }


}