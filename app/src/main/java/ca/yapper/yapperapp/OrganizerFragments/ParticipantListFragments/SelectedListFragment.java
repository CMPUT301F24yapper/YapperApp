package ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.concurrent.atomic.AtomicInteger;

import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.EventParticipantsViewPagerAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersInEventAdapter;

/**
 * SelectedListFragment displays the list of users selected to participate in a specific event.
 * This fragment allows for re-selection by drawing users from the waiting list as needed.
 * The list is retrieved from Firestore and displayed in a RecyclerView.
 */
public class SelectedListFragment extends Fragment {

    private RecyclerView recyclerView;
    //private UsersAdapter adapter;
    private UsersInEventAdapter adapter;
    private List<User> selectedList;
    private String eventId;
    private Button redrawButton;
    private Button dumpPendingButton;
    private int eventCapacity;
    private TextView selectedCountTextView;
    private LinearLayout emptyStateLayout;

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

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            if (eventId == null) {
                Log.e("SelectedListFragment", "eventId is null!");
                return view;
            }
        }
        else {
            Log.e("SelectedListFragment", "Arguments bundle is null!");
            return view; }

        // Initialize selectedList here
        selectedList = new ArrayList<>();  // Make sure selectedList is initialized
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsersInEventAdapter(selectedList, eventId);
        recyclerView.setAdapter(adapter);
        selectedCountTextView = view.findViewById(R.id.selected_count_textview);
        redrawButton = view.findViewById(R.id.button_redraw);
        dumpPendingButton = view.findViewById(R.id.button_dump_pending);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        loadEventCapacity();
        loadSelectedList();

        // Redraw Applicant Button
        redrawButton.setOnClickListener(v -> redrawApplicant());

        // Dump Pending Applicant Button
        dumpPendingButton.setOnClickListener(v -> dumpPendingApplicant());
        return view;
    }

    /**
     * Loads the capacity of the event from Firestore, setting the maximum number of selected participants.
     */
    private void loadEventCapacity() {
        OrganizerDatabase.loadEventCapacity(eventId, new OrganizerDatabase.OnEventCapLoadedListener() {
            @Override
            public void onCapacityLoaded(int capacity) {
                eventCapacity = capacity;
                Log.i("SelectedListFragment", "loadEventCapacity() from OrgDb yields Event Capacity: " + eventCapacity);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading event capacity: " + errorMessage, Toast.LENGTH_SHORT).show();    }
        });
    }

    /**
     * Refreshes the selected list by reloading data from Firestore and updating the RecyclerView.
     */
    public void refreshList() {
        if (getContext() == null) {return;}
        // get refreshed list
        selectedList.clear();
        adapter.notifyDataSetChanged();

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "selectedList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                if (userIdsList.isEmpty()) {
                    Log.i("SelectedListFragment", "refreshList(): userIdsList is EMPTY");
                    showEmptyState(true);
                    return;
                }
                showEmptyState(false);
                AtomicInteger loadedUsersCount = new AtomicInteger(0);  // Track loaded users

                for (String userId : userIdsList) {
                    // For each userId, fetch the corresponding User object
                    UserDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(User user) {
                            selectedList.add(user);
                            adapter.notifyDataSetChanged();  // Notify the adapter about new data
                            // Check if all users are loaded
                            if (loadedUsersCount.incrementAndGet() == userIdsList.size()) {
                                // Perform post-loading operations here
                                finalizeRefreshList();
                            }
                        }
                        @Override
                        public void onUserLoadError(String error) {
                            if (getContext() == null) return;
                            Log.e("SelectedList", "Error loading user: " + error);
                            // Check even on failure if all users are processed
                            if (loadedUsersCount.incrementAndGet() == userIdsList.size()) {
                                finalizeRefreshList();
                            }
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

    private void finalizeRefreshList() {
        // Perform UI updates and checks
        loadEventCapacity();
        if (selectedList.isEmpty()) {
            Toast.makeText(getContext(), "No users in selected list", Toast.LENGTH_SHORT).show();
            Log.i("SelectedListFragment refreshList()", "no users in selected list!");
        }
        selectedCountTextView.setText("Selected Count: " + selectedList.size());
        Log.i("SelectedListFragment", "refreshList(), selectedListSize: " + selectedList.size());

        if (selectedList.size() < eventCapacity) {
            redrawButton.setVisibility(View.VISIBLE);
            // redrawButton.setOnClickListener(v -> redrawApplicant());
        } else {
            redrawButton.setVisibility(View.GONE); }

        // Check if there are any "Pending" users in the selected list
        boolean hasPending = false;

        // Track how many users have been checked
        AtomicInteger checkedUsersCount = new AtomicInteger(0);

        String userStatus;
        for (User user : selectedList) {
            OrganizerDatabase.isPendingStatusForUser(user.getDeviceId(), eventId, new OrganizerDatabase.OnIsPendingStatusCheckedListener() {
                @Override
                public void onStatusLoaded(boolean isPending) {
                    Log.i("finalizeRefreshList", "OrgDb.isPendingStatusForUser(userId x, eventId y...): " + "x: " + user.getDeviceId() + ",y: " + eventId);
                    if (isPending) {
                        updatePendingUI(isPending);
                        Log.i("finalizeRefreshList", "In for loop, isPendingStatusForUser: User x is pending: " + user.getDeviceId());
                        return;
                    }
                    else {
                        Log.i("finalizeRefreshList", "In for loop, isPendingStatusForUser: User x is NOT pending: " + user.getDeviceId());
                    }
                    // Increment the count of checked users
                    if (checkedUsersCount.incrementAndGet() == selectedList.size()) {
                        // All users have been checked, update UI
                        updatePendingUI(isPending);
                    }
                }
                @Override
                public void onError(String error) {
                    Log.e("SelectedListFragment", "Error checking status for user: " + user.getDeviceId() + " - " + error);
                    }
                });
        }
    }

    private void updatePendingUI(boolean hasPending) {
        // Show the Dump Pending Applicant button if there are pending users
        if (hasPending) {
            dumpPendingButton.setVisibility(View.VISIBLE);
        } else {
            dumpPendingButton.setVisibility(View.GONE);
        }

        // Show the Redraw button if there are seats left in the selected list
        if (selectedList.size() < eventCapacity) {
            redrawButton.setVisibility(View.VISIBLE);
        } else {
            redrawButton.setVisibility(View.GONE);
        }
    }

    private void dumpPendingApplicant() {
        // Disable the button to prevent multiple clicks while processing
        dumpPendingButton.setEnabled(false);

        for (User user : selectedList) {
            OrganizerDatabase.isPendingStatusForUser(user.getDeviceId(), eventId, new OrganizerDatabase.OnIsPendingStatusCheckedListener() {
                @Override
                public void onStatusLoaded(boolean isPending) {
                    if (isPending) {
                        // pendingUsers.add(user); }// Add pending users to the list
                        moveUserToCancelledList(user);
                        selectedList.remove(user);
                    }
                }
                @Override
                public void onError(String error) {
                    Log.e("SelectedListFragment", "Error checking status for user: " + user.getDeviceId() + " - " + error);
                }
            });
        }
    }

    private void moveUserToFinalList(User user) {
        OrganizerDatabase.moveUserBetweenSubcollections(eventId, user.getDeviceId(), "selectedList", "finalList");
        EntrantDatabase.addEventToRegisteredEvents(user.getDeviceId(), eventId);
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
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
        loadEventCapacity();  // update eventCapacity
        if (selectedList.isEmpty()) {
            Toast.makeText(getContext(), "No users in selected list", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedList.size() < eventCapacity) {
            drawFromWaitingList();
            return;
        }
        else {
            Toast.makeText(getContext(), "You have already drawn the maximum number of attendees for your event.", Toast.LENGTH_SHORT).show();
        }
        /** Random random = new Random();
        int index = random.nextInt(selectedList.size());
        User selectedUser = selectedList.get(index);
        moveUserToWaitingList(selectedUser); **/
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

                        UserDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
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


    /**
     * Moves a user from the waiting list to the selected list in Firestore.
     *
     * @param user The user to be moved.
     * @param onComplete Runnable executed once the move is complete.
     */
    /**
     * Moves a user from the waiting list to the selected list in Firestore.
     *
     * @param user The user to be moved.
     * @param onComplete Runnable executed once the move is complete.
     */
    private void moveToSelectedList(User user, Runnable onComplete) {
        // Create notification with event details
        Notification notification = new Notification(
                new Date(),
                user.getDeviceId(), // Recipient's device ID
                null, // Assuming no specific sender is set
                eventName, // Use the event name as the title
                "You have been selected from the waiting list for the event: " + eventName,
                "Selection",
                eventId, // Include the event ID
                eventName // Include the event name
        );
        notification.saveToDatabase(); // Save the notification to Firestore

        OrganizerDatabase.moveUserToSelectedList(eventId, user.getDeviceId(), new OrganizerDatabase.OnOperationCompleteListener() {
          @Override
          public void onComplete(boolean success) {
            if (success) {
                onComplete.run(); // Notify that the operation was successful
                Toast.makeText(getContext(), user.getName() + " has been moved to the selected list.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to move user to selected list. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    });
}

                                                 
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

    private void moveUserToCancelledList(User user) {
        OrganizerDatabase.moveUserBetweenSubcollections(eventId, user.getDeviceId(), "selectedList", "cancelledList");
        // status change
        // OrganizerDatabase.changeUserStatusForEvent(user.getDeviceId(), eventId, "");
        selectedList.remove(user);  // Remove from selected list
        adapter.notifyDataSetChanged();
        selectedCountTextView.setText("Selected Count: " + selectedList.size());  // Update selected count
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