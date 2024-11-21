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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.EventParticipantsViewPagerAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersAdapter;
/**
 * SelectedListFragment displays the list of users selected to participate in a specific event.
 * This fragment allows for re-selection by drawing users from the waiting list as needed.
 * The list is retrieved from Firestore and displayed in a RecyclerView.
 */
public class SelectedListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> selectedList;
    //private FirebaseFirestore db;
    private String eventId;
    private Button redrawButton;
    private int eventCapacity;


    /**
     * Inflates the fragment layout, initializes Firestore, RecyclerView, adapter, and UI components,
     * and loads the selected list for the specified event. Sets up a redraw button for re-selection.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
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

        //db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventCapacity();
            loadSelectedList();
        }

        redrawButton.setOnClickListener(v -> redrawApplicant());
        return view;
    }


    /**
     * Loads the capacity of the event from Firestore, setting the maximum number of selected participants.
     */
    private void loadEventCapacity() {
        OrganizerDatabase.loadEventCapacity(eventId);
        /**db.collection("Events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        eventCapacity = documentSnapshot.getLong("capacity").intValue();
                    }
                }); **/
    }


    /**
     * Refreshes the selected list by reloading data from Firestore and updating the RecyclerView.
     */
    public void refreshList() {
        if (getContext() == null) return;

        selectedList.clear();
        adapter.notifyDataSetChanged();

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "selectedList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                for (String userId : userIdsList) {
                    // For each userId, fetch the corresponding User object
                    EntrantDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(User user) {
                            if (getContext() == null) return;

                            // Add the User to the cancelledList and notify the adapter
                            selectedList.add(user);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onUserLoadError(String error) {
                            if (getContext() == null) return;

                            Log.e("SelectedList", "Error loading user: " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading user IDs: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        /** db.collection("Events").document(eventId)
                .collection("selectedList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String userId = document.getId();
                        EntrantDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
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
                }); **/
    }


    /**
     * Loads the selected list from the "selectedList" subcollection of the event document in Firestore.
     * Updates the RecyclerView adapter with the retrieved users.
     */
    private void loadSelectedList() {
        refreshList();
    }

    /**
     * Allows re-selection of participants by randomly choosing from the waiting list and moving users to the selected list,
     * based on the event's capacity. Updates the UI with newly selected participants.
     */
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


    /**
     * Draws multiple users from the waiting list if the selected list has space remaining,
     * moving users from the waiting list to the selected list in Firestore.
     */
    private void drawFromWaitingList() {
        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "waitingList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                if (userIdsList.isEmpty()) {
                    Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
                    return;
                }

                int remainingSlots = eventCapacity - selectedList.size();
                int availableUsers = userIdsList.size();
                int drawCount = Math.min(remainingSlots, availableUsers);
                final int[] completedMoves = {0};

                List<String> waitingUserIds = new ArrayList<>(userIdsList);

                for (int i = 0; i < drawCount; i++) {
                    if (!waitingUserIds.isEmpty()) {
                        Random random = new Random();
                        int index = random.nextInt(waitingUserIds.size());
                        String userId = waitingUserIds.get(index);
                        waitingUserIds.remove(index);

                        EntrantDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
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
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading waiting list: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

        /** db.collection("Events").document(eventId)
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

                            EntrantDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
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
                }); **/


    /**
     * Moves a user from the waiting list to the selected list in Firestore.
     *
     * @param user The user to be moved.
     * @param onComplete Runnable executed once the move is complete.
     */
    private void moveToSelectedList(User user, Runnable onComplete) {
        Notification notification = new Notification(
                new Date(),
                "Selected for Event",
                "You have been selected from the waiting list",
                "Selection"
        );
        notification.saveToDatabase(user.getDeviceId());

        OrganizerDatabase.moveUserToSelectedList(eventId, user.getDeviceId(), new OrganizerDatabase.OnOperationCompleteListener() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    onComplete.run();
                } else {
                    Toast.makeText(getContext(), "Failed to move user to selected list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
        /** Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        DocumentReference waitingListRef = db.collection("Events").document(eventId)
                .collection("waitingList").document(user.getDeviceId());
        DocumentReference selectedListRef = db.collection("Events").document(eventId)
                .collection("selectedList").document(user.getDeviceId());

        // Create and save notification
        Notification notification = new Notification(
                new Date(),
                "Selected for Event",
                "You have been selected from the waiting list",
                "Selection"
        );
        notification.saveToDatabase(user.getDeviceId());

        db.runTransaction(transaction -> {
            transaction.delete(waitingListRef);
            transaction.set(selectedListRef, timestamp);
            return null;
        }).addOnSuccessListener(aVoid -> {
            onComplete.run();
        });
    } **/


    /**
     * Moves a user from the selected list back to the waiting list in Firestore.
     * Refreshes the list after the user is moved.
     *
     * @param user The user to be moved.
     */
    private void moveUserToWaitingList(User user) {
        OrganizerDatabase.moveUserToWaitingList(eventId, user.getDeviceId(), new OrganizerDatabase.OnOperationCompleteListener() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    refreshAllFragments();
                    drawFromWaitingList();
                } else {
                    Toast.makeText(getContext(), "Failed to move user to waiting list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
        /**Map<String, Object> timestamp = new HashMap<>();
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
    }**/



    /**
     * Refreshes all fragments displaying participant lists for the event,
     * ensuring that UI updates are reflected across all views.
     */
    private void refreshAllFragments() {
        ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
        EventParticipantsViewPagerAdapter pagerAdapter = (EventParticipantsViewPagerAdapter) viewPager.getAdapter();
        if (pagerAdapter != null) {
            pagerAdapter.refreshAllLists();
        }
    }
}