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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Button drawButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_waitlist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersWaitingList = new ArrayList<>();
        adapter = new UsersAdapter(usersWaitingList, getContext());
        recyclerView.setAdapter(adapter);

        drawButton = view.findViewById(R.id.button_draw);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadWaitingList(eventId);
        } else {
            Toast.makeText(getContext(), "Error: Unable to get event ID", Toast.LENGTH_SHORT).show();
        }

        drawButton.setOnClickListener(v -> drawReplacementApplicant());

        return view;
    }

    private void loadWaitingList(String eventId) {
        Log.d("EventDebug", "Loading event with ID: " + eventId);

        Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                if (getContext() == null) return;

                event.loadUserIdsFromSubcollection(db, eventId, "waitingList", new Event.OnUserIdsLoadedListener() {
                    @Override
                    public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                        for (String userId : userIdsList) {
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
                });
            }

            @Override
            public void onEventLoadError(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Error loading event: " + error, Toast.LENGTH_SHORT).show();
                Log.e("EventDetails", "Error loading event: " + error);
            }
        });
    }

    private void drawReplacementApplicant() {
        if (usersWaitingList.isEmpty()) {
            Toast.makeText(getContext(), "No applicants in the waiting list", Toast.LENGTH_SHORT).show();
            return;
        }

        Random random = new Random();
        int index = random.nextInt(usersWaitingList.size());
        User selectedUser = usersWaitingList.get(index);

        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Events").document(eventId)
                .collection("selectedList")
                .document(selectedUser.getDeviceId())
                .set(timestamp)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Applicant selected: " + selectedUser.getName(), Toast.LENGTH_SHORT).show();
                    usersWaitingList.remove(selectedUser);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error adding to selected list", e));
    }
}