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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.EventParticipantsViewPagerAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersAdapter;
/**
 * WaitingListFragment displays the list of users on the waiting list for a specific event.
 * This fragment allows drawing from the waiting list to fill available slots in the selected list.
 * The list is retrieved from Firestore and displayed in a RecyclerView.
 */
public class WaitingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> usersWaitingList;
    private String eventId;
    private Button drawButton;
    private int eventCapacity;

    /**
     * Inflates the fragment layout, initializes Firestore, RecyclerView, adapter, and UI components,
     * and loads the waiting list for the specified event. Sets up a draw button to move users to the selected list.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
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

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventCapacity();
            loadWaitingList();
        }

        drawButton.setOnClickListener(v -> drawMultipleApplicants());
        return view;
    }

    /**
     * Loads the capacity of the event from Firestore, setting the maximum number of participants allowed.
     */
    private void loadEventCapacity() {
        OrganizerDatabase.loadEventCapacity(eventId, new OrganizerDatabase.OnEventCapLoadedListener() {
            @Override
            public void onCapacityLoaded(int capacity) {
                eventCapacity = capacity;
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading capacity: " + errorMessage, Toast.LENGTH_SHORT).show();    }
    });
    }

    /**
     * Refreshes the waiting list by reloading data from Firestore and updating the RecyclerView.
     */
    public void refreshList() {
        if (getContext() == null) return;

        usersWaitingList.clear();
        adapter.notifyDataSetChanged();

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "waitingList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                for (String userId : userIdsList) {
                    // For each userId, fetch the corresponding User object
                    UserDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(User user) {
                            if (getContext() == null) return;

                            // Add the User to the cancelledList and notify the adapter
                            usersWaitingList.add(user);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onUserLoadError(String error) {
                            if (getContext() == null) return;

                            Log.e("WaitingList", "Error loading user: " + error);
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
    }

    /**
     * Loads the waiting list from the "waitingList" subcollection of the event document in Firestore.
     * Updates the RecyclerView adapter with the retrieved users.
     */
    private void loadWaitingList() {
        refreshList();
    }

    /**
     * Draws multiple users from the waiting list, filling available slots in the selected list
     * until the event's capacity is reached. Updates Firestore and the UI with the moved users.
     */
    private void drawMultipleApplicants() {
        // make sure eventCapacity updated
        loadEventCapacity(); // should update int eventCapacity (confirm)
        Log.e("draw multiple applicants", "eventCapacity: " + eventCapacity);
        OrganizerDatabase.getSelectedListCount(eventId, new OrganizerDatabase.OnDataFetchListener<Integer>() {
            @Override
            public void onFetch(Integer currentSelectedCount) {
                int remainingSlots = eventCapacity - currentSelectedCount;
                Log.e("draw multiple applicants", "Remaining slots: " + remainingSlots);


                if (remainingSlots <= 0) {
                    Toast.makeText(getContext(), "Event capacity is full", Toast.LENGTH_SHORT).show();
                    return;
                }
                int drawCount = Math.min(remainingSlots, usersWaitingList.size());  // returns the smaller of the two
                final int[] completedMoves = {0};

                for (int i = 0; i < drawCount; i++) {
                    if (!usersWaitingList.isEmpty()) {
                        Random random = new Random();
                        int index = random.nextInt(usersWaitingList.size());
                        User selectedUser = usersWaitingList.remove(index);

                        // Create notification
                        Notification notification = new Notification(
                                new Date(),
                                "Selected for Event",
                                "You have been selected from the waiting list",
                                "Selection"
                        );
                        notification.saveToDatabase(selectedUser.getDeviceId());

                        OrganizerDatabase.moveUserToSelectedList(eventId, selectedUser.getDeviceId(),
                                success -> {
                                    if (success) {
                                        completedMoves[0]++;
                                        if (completedMoves[0] == drawCount) {
                                            refreshAllFragments();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Failed to move some users", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to fetch selected list count", Toast.LENGTH_SHORT).show();
                Log.e("FirestoreError", "Error fetching selected list count", e);
            }
        });
        /**db.collection("Events").document(eventId)
                .collection("selectedList").get()
                .addOnSuccessListener(selectedSnapshot -> {
                    int currentSelectedCount = selectedSnapshot.size();
                    int remainingSlots = eventCapacity - currentSelectedCount;

                    if (remainingSlots <= 0) {
                        Toast.makeText(getContext(), "Event capacity is full", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int drawCount = Math.min(remainingSlots, usersWaitingList.size());
                    final int[] completedMoves = {0};

                    for (int i = 0; i < drawCount; i++) {
                        if (!usersWaitingList.isEmpty()) {
                            Random random = new Random();
                            int index = random.nextInt(usersWaitingList.size());
                            User selectedUser = usersWaitingList.get(index);

                            // Create notification
                            Notification notification = new Notification(
                                    new Date(),
                                    "Selected for Event",
                                    "You have been selected from the waiting list",
                                    "Selection"
                            );
                            notification.saveToDatabase(selectedUser.getDeviceId());

                            moveUserToSelectedList(selectedUser, () -> {
                                completedMoves[0]++;
                                if (completedMoves[0] == drawCount) {
                                    refreshAllFragments();
                                }
                            });
                        }
                    }
                });**/
    }


    /**
     * Moves a user from the waiting list to the selected list in Firestore.
     * Displays a confirmation message and updates the UI.
     *
     * @param user The user to be moved.
     * @param onComplete Runnable executed once the move is complete.
     */
    private void moveUserToSelectedList(User user, Runnable onComplete) {
        OrganizerDatabase.moveUserToWaitingList(eventId, user.getDeviceId(), new OrganizerDatabase.OnOperationCompleteListener() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    refreshAllFragments();
                    drawMultipleApplicants();
                } else {
                    Toast.makeText(getContext(), "Failed to move user to waiting list", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /**Map<String, Object> timestamp = new HashMap<>();
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
            Toast.makeText(getContext(), "User " + user.getName() + " moved to selected list", Toast.LENGTH_SHORT).show();
            onComplete.run();
        }).addOnFailureListener(e -> {
            Log.e("FirestoreError", "Error moving user to selected list", e);
            onComplete.run();
        });**/
    }

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