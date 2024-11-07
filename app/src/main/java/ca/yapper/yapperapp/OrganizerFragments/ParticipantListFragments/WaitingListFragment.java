package ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersAdapter;

public class WaitingListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> usersWaitingList;
    private FirebaseFirestore db;
    private String eventId;
    private Button redrawButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_waitlist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersWaitingList = new ArrayList<>();
        adapter = new UsersAdapter(usersWaitingList, getContext());
        recyclerView.setAdapter(adapter);

        redrawButton = view.findViewById(R.id.Button);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadWaitingList(eventId);
        } else {
            Toast.makeText(getContext(), "Error: Unable to get event ID", Toast.LENGTH_SHORT).show();
        }

        // Set up button click listener for redrawing
        redrawButton.setOnClickListener(v -> redrawReplacementApplicant());

        return view;
    }

    private void loadWaitingList(String eventId) {
        ArrayList<String> userIdsStringList = new ArrayList<>();
        Log.d("EventDebug", "Loading event with ID: " + eventId);

        Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
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

    private void redrawReplacementApplicant() {
        if (usersWaitingList.isEmpty()) {
            Toast.makeText(getContext(), "No applicants in the waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Randomly select an applicant
        Random random = new Random();
        int index = random.nextInt(usersWaitingList.size());
        User selectedUser = usersWaitingList.get(index);

        // Add selected user to Firestore "selectedList"
        addSelectedUserToFirestore(selectedUser);
    }

    private void addSelectedUserToFirestore(User selectedUser) {
        db.collection("Events").document(eventId).collection("selectedList").document(selectedUser.getDeviceId())
                .set(selectedUser.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Replacement applicant selected: " + selectedUser.getName(), Toast.LENGTH_SHORT).show();
                    usersWaitingList.remove(selectedUser);  // Optionally, remove from waiting list
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error adding to selected list", e));
    }
}
