package ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersAdapter;

public class SelectedListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> selectedList;
    private FirebaseFirestore db;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_waitlist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedList = new ArrayList<>();
        adapter = new UsersAdapter(selectedList, getContext());
        recyclerView.setAdapter(adapter);
        // TO-DO: SET 'EVENTID' TO BUNDLE PARAMETER #1 SENT FROM HOMEPAGE TO SPECIFIC EVENT CLICK NAVIGATION!
        // add in event parameters bundle... etc
        db = FirebaseFirestore.getInstance();
        loadUsersFromFirebase(eventId);

        return view;
    }

    private void loadUsersFromFirebase(String eventId) {
        // Access the "waitingList" subcollection for the specific event
        db.collection("Events").document(eventId).collection("selectedList").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userDocument : task.getResult()) {
                            String userId = userDocument.getId(); // Get user ID (entrantDeviceId)
                            String userName = userDocument.getString("name"); // Assuming 'name' field exists
                            String userPhoneNum = userDocument.getString("phone");
                            String userEmail = userDocument.getString("email");
                            Boolean userIsEntrant = userDocument.getBoolean("isEntrant");
                            Boolean userIsOrganizer = userDocument.getBoolean("isOrganizer");
                            Boolean userIsAdmin = userDocument.getBoolean("isAdmin");

                            // Create a User object and add to the waiting list
                            //     public User(String name, String deviceId, String email, String phoneNum, Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin) {
                            User user = new User(userId, userName, userEmail, userPhoneNum, userIsEntrant, userIsOrganizer, userIsAdmin);
                            selectedList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Error getting selectedList subcollection for eventId: " + eventId, task.getException());
                    }
                });
    }
}