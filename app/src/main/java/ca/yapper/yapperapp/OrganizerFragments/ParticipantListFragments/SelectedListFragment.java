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
import androidx.viewpager2.widget.ViewPager2;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ca.yapper.yapperapp.EventParticipantsViewPagerAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersAdapter;

public class SelectedListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> selectedList;
    private FirebaseFirestore db;
    private String eventId;
    private Button redrawButton;
    private int eventCapacity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_selectedlist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedList = new ArrayList<>();
        adapter = new UsersAdapter(selectedList, getContext());
        recyclerView.setAdapter(adapter);

        redrawButton = view.findViewById(R.id.button_redraw);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventCapacity();
            loadSelectedList();
        }

        redrawButton.setOnClickListener(v -> redrawApplicant());
        return view;
    }

    private void loadEventCapacity() {
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        eventCapacity = documentSnapshot.getLong("capacity").intValue();
                    }
                });
    }

    public void refreshList() {
        if (getContext() == null) return;

        selectedList.clear();
        adapter.notifyDataSetChanged();

        db.collection("Events").document(eventId)
                .collection("selectedList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String userId = document.getId();
                        User.loadUserFromDatabase(userId, new User.OnUserLoadedListener() {
                            @Override
                            public void onUserLoaded(User user) {
                                if (getContext() == null) return;
                                selectedList.add(user);
                                adapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onUserLoadError(String error) {
                                Log.e("SelectedList", "Error loading user: " + error);
                            }
                        });
                    }
                });
    }

    private void loadSelectedList() {
        refreshList();
    }

    private void redrawApplicant() {
        if (selectedList.isEmpty()) {
            Toast.makeText(getContext(), "No users in selected list", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedList.size() < eventCapacity) {
            drawFromWaitingList();
            return;
        }

        Random random = new Random();
        int index = random.nextInt(selectedList.size());
        User selectedUser = selectedList.get(index);
        moveUserToWaitingList(selectedUser);
    }

    private void drawFromWaitingList() {
        db.collection("Events").document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(waitingListSnapshot -> {
                    if (waitingListSnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int remainingSlots = eventCapacity - selectedList.size();
                    int availableUsers = waitingListSnapshot.size();
                    int drawCount = Math.min(remainingSlots, availableUsers);
                    final int[] completedMoves = {0};

                    List<DocumentSnapshot> waitingUsers = new ArrayList<>(waitingListSnapshot.getDocuments());

                    for (int i = 0; i < drawCount; i++) {
                        if (!waitingUsers.isEmpty()) {
                            Random random = new Random();
                            int index = random.nextInt(waitingUsers.size());
                            DocumentSnapshot userDoc = waitingUsers.get(index);
                            String userId = userDoc.getId();
                            waitingUsers.remove(index);

                            User.loadUserFromDatabase(userId, new User.OnUserLoadedListener() {
                                @Override
                                public void onUserLoaded(User user) {
                                    moveToSelectedList(user, () -> {
                                        completedMoves[0]++;
                                        if (completedMoves[0] == drawCount) {
                                            refreshAllFragments();
                                        }
                                    });
                                }
                                @Override
                                public void onUserLoadError(String error) {
                                    completedMoves[0]++;
                                }
                            });
                        }
                    }
                });
    }

    private void moveToSelectedList(User user, Runnable onComplete) {
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        DocumentReference waitingListRef = db.collection("Events").document(eventId)
                .collection("waitingList").document(user.getDeviceId());
        DocumentReference selectedListRef = db.collection("Events").document(eventId)
                .collection("selectedList").document(user.getDeviceId());

        db.runTransaction(transaction -> {
            transaction.delete(waitingListRef);
            transaction.set(selectedListRef, timestamp);
            return null;
        }).addOnSuccessListener(aVoid -> {
            onComplete.run();
        });
    }

    private void moveUserToWaitingList(User user) {
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        DocumentReference selectedListRef = db.collection("Events").document(eventId)
                .collection("selectedList").document(user.getDeviceId());
        DocumentReference waitingListRef = db.collection("Events").document(eventId)
                .collection("waitingList").document(user.getDeviceId());

        db.runTransaction(transaction -> {
            transaction.delete(selectedListRef);
            transaction.set(waitingListRef, timestamp);
            return null;
        }).addOnSuccessListener(aVoid -> {
            refreshAllFragments();
            drawFromWaitingList();
        });
    }

    private void refreshAllFragments() {
        ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
        EventParticipantsViewPagerAdapter pagerAdapter = (EventParticipantsViewPagerAdapter) viewPager.getAdapter();
        if (pagerAdapter != null) {
            pagerAdapter.refreshAllLists();
        }
    }
}