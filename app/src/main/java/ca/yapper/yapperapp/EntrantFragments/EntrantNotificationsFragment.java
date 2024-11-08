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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import ca.yapper.yapperapp.NotificationAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;
/**
 * EntrantNotificationsFragment displays notifications related to the user, such as updates
 * on events they have registered for or joined. It loads unread notifications from Firestore
 * and displays them in a RecyclerView.
 */
public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private String userDeviceId;


    /**
     * Inflates the fragment layout, initializes Firestore, RecyclerView, and adapter components,
     * and loads unread notifications from Firestore.
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

        db = FirebaseFirestore.getInstance();
        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        loadUnreadNotificationsFromFirestore();

        return view;
    }


    /**
     * Loads unread notifications from the "Notifications" collection in Firestore, filtering by
     * the user's device ID and displaying the results in the RecyclerView.
     * Once loaded, notifications are marked as read.
     */
    private void loadUnreadNotificationsFromFirestore() {
        db.collection("Notifications")
                .whereEqualTo("userToId", userDeviceId)  // Query by device ID
                .whereEqualTo("isRead", false)
                .orderBy("dateTimeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Notification notification = document.toObject(Notification.class);
                        notification.setId(document.getId());
                        notificationList.add(notification);
                    }
                    notificationAdapter.notifyDataSetChanged();
                    markNotificationsAsRead(notificationList);
                })
                .addOnFailureListener(e -> Log.e("NotificationsError", "Error loading notifications", e));
    }


    /**
     * Marks notifications as read in Firestore once they have been loaded and displayed
     * in the RecyclerView.
     *
     * @param notifications The list of notifications to be marked as read.
     */
    private void markNotificationsAsRead(List<Notification> notifications) {
        for (Notification notification : notifications) {
            db.collection("Notifications")
                    .document(notification.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> Log.d("Notifications", "Notification marked as read"))
                    .addOnFailureListener(e -> Log.e("NotificationsError", "Error marking notification as read", e));
        }
    }
}

