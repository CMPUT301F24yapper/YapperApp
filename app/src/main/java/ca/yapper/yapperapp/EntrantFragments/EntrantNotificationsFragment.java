package ca.yapper.yapperapp.EntrantFragments;

import android.os.Bundle;
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

public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_notifications, container, false);

        // Initialize Firestore and RecyclerView
        db = FirebaseFirestore.getInstance();
        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the notification list and adapter
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        // Load unread notifications
        loadUnreadNotificationsFromFirestore();

        return view;
    }

    private void loadUnreadNotificationsFromFirestore() {
//        db.collection("Notifications")
//                .whereEqualTo("userTo", currentUser.getId()) // Adjust to match your structure
//                .whereEqualTo("isRead", false) // Only fetch unread notifications
//                .orderBy("dateTimeStamp", Query.Direction.DESCENDING)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        notificationList.clear();
//                        for (QueryDocumentSnapshot doc : task.getResult()) {
//                            Notification notification = doc.toObject(Notification.class);
//                            notificationList.add(notification);
//                        }
//                        notificationAdapter.notifyDataSetChanged();
//
//                        // Mark all displayed notifications as read
//                        markNotificationsAsRead(notificationList);
//                    } else {
//                        Log.w("NotificationFragment", "Error getting documents: ", task.getException());
//                    }
//                });
    }

    private void markNotificationsAsRead(List<Notification> notifications) {
//        for (Notification notification : notifications) {
//            db.collection("Notifications").document(notification.getId()) // Ensure `getId()` exists or use `doc.getId()` directly
//                    .update("isRead", true)
//                    .addOnSuccessListener(aVoid -> Log.d("NotificationFragment", "Notification marked as read"))
//                    .addOnFailureListener(e -> Log.w("NotificationFragment", "Error updating notification", e));
//        }
    }
}