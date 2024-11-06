package ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.Event.OnEventLoadedListener;
import ca.yapper.yapperapp.UsersAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UMLClasses.User.OnUserLoadedListener;

public class WaitingListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> usersWaitingList;
    // private List<>
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
            loadWaitingList(eventId);
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

    private void loadWaitingList(String eventId) {
        ArrayList<String> userIdsStringList = new ArrayList<>();
        Log.d("EventDebug", "Loading event with ID: " + eventId);

        Event.loadEventFromDatabase(eventId, new OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                if (getContext() == null) return;

                // Load user IDs from Firestore
                event.loadUserIdsFromSubcollection(db, eventId, "waitingList", userIdsStringList);

                // Assuming user objects are created after loading IDs
                for (String userId : userIdsStringList) {
                    User.loadUserFromDatabase(userId, new User.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(User user) {
                            usersWaitingList.add(user);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onUserLoadError(String error) {
                            Log.e("UserLoadError", error);
                        }
                    });
                }
            }

            @Override
            public void onEventLoadError(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Error loading event: " + error, Toast.LENGTH_SHORT).show();
                Log.e("EventDetails", "Error loading event: " + error);
            }
        });
    }
}