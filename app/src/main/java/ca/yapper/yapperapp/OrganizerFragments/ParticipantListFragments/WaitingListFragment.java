package ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UsersAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;

public class WaitingListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> usersWaitingList;
    private FirebaseFirestore db;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_waitlist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersWaitingList = new ArrayList<>();
        adapter = new UsersAdapter(usersWaitingList, getContext());
        recyclerView.setAdapter(adapter);
        // add in event parameters bundle... etc
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            // loadWaitingList(eventId);
        }
        else {
            Toast.makeText(getContext(), "Error: Unable to get event ID", Toast.LENGTH_SHORT).show();
            // something else?
        }
        // to-do: else statement for errors
        // loadUsersFromFirebase(eventId);
        /** Log.d("EventDebug", "Loading event with ID: " + eventId);

         Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
        @Override
        public void onEventLoaded(Event event) {
        if (getContext() == null) return; **/

        return view;
    }

    /** private void loadWaitingList(String eventId) {
        Log.d("EventDebug", "Loading event with ID: " + eventId);
        db.collection("Events").document(eventId).collection("waitingList").get().addOnSuccessListener(queryDocumentSnapshots -> {
            usersWaitingList.clear();
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(getContext(), "No users in waiting list found", Toast.LENGTH_SHORT).show();
                return;
            }

            for (DocumentSnapshot document : queryDocumentSnapshots) {
                String userId = document.getId();

                User.loadUserFromDatabase(userId, new User.OnU() {
        })
        Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
        }
    } **/

    /** private void loadEventsFromFirebase() {
     db.collection("Users")
     .document(userDeviceId)
     .collection("joinedEvents")
     .get()
     .addOnSuccessListener(queryDocumentSnapshots -> {
     eventList.clear();

     if (queryDocumentSnapshots.isEmpty()) {
     Toast.makeText(getContext(), "No joined events found", Toast.LENGTH_SHORT).show();
     return;
     }

     for (DocumentSnapshot document : queryDocumentSnapshots) {
     String eventId = document.getId();

     Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
    @Override
    public void onEventLoaded(Event event) {
    if (getContext() == null) return;

    eventList.add(event);
    adapter.notifyDataSetChanged();
    }

    @Override
    public void onEventLoadError(String error) {
    if (getContext() == null) return;

    Log.e("JoinedEvents", "Error loading event " + eventId + ": " + error);
    }
    });
     }
     })
     .addOnFailureListener(e -> {
     if (getContext() != null) {
     Toast.makeText(getContext(),
     "Error loading joined events: " + e.getMessage(),
     Toast.LENGTH_SHORT).show();
     }
     });
     }
     } **/

    /**  private static void loadUserIdsFromSubcollection(FirebaseFirestore db, String eventId, String subcollectionName, ArrayList<String> userIdsList) {
     db.collection("Events").document(eventId).collection(subcollectionName)
     .get()
     .addOnSuccessListener(queryDocumentSnapshots -> {
     for (DocumentSnapshot doc : queryDocumentSnapshots) {
     // Assuming each document in the subcollection is a reference to a User in the "Users" collection
     String userIdRef = doc.getId(); // Get the document ID in the subcollection (reference to User ID)

     // Retrieve the User document to get the deviceId
     db.collection("Users").document(userIdRef).get()
     .addOnSuccessListener(userDoc -> {
     if (userDoc.exists()) {
     String deviceId = userDoc.getString("deviceId");
     if (deviceId != null) {
     userIdsList.add(deviceId); // Add the deviceId to the respective list
     }
     }
     });
     }
     });
     }
     **/

//
}

