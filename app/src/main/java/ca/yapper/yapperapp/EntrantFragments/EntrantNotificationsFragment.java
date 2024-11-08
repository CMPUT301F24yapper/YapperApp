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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.NotificationAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;

public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private String userDeviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_notifications, container, false);

        db = FirebaseFirestore.getInstance();
        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        loadNotificationsFromFirestore();

        return view;
    }

    private void loadNotificationsFromFirestore() {
        db.collection("Notifications")
                .whereEqualTo("userToId", userDeviceId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Notification notification = document.toObject(Notification.class);
                        notification.setId(document.getId());
                        notificationList.add(notification);
                    }
                    notificationAdapter.notifyDataSetChanged();
                    markNotificationsAsRead(notificationList);
                })
                .addOnFailureListener(e -> Log.e("NotificationsError", "Error loading notifications", e));
    }

    private void markNotificationsAsRead(List<Notification> notifications) {
        for (Notification notification : notifications) {
            db.collection("Notifications").document(notification.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> Log.d("Notifications", "Notification marked as read"))
                    .addOnFailureListener(e -> Log.e("NotificationsError", "Error marking notification as read", e));
        }
    }
}