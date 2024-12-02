package ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.NotificationsDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.EventParticipantsViewPagerAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
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
    private UsersInEventAdapter adapter;
    private List<User> selectedList;
    private String eventId;
    private String eventName;
    private Button redrawButton;
    private Button dumpPendingButton;
    private int eventCapacity;
    private TextView selectedCountTextView;
    private LinearLayout emptyStateLayout;
    private String organizerId;

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

        organizerId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

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

        loadEventDetails();

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

        loadSelectedList();

        // Redraw Applicant Button
        // redrawButton.setOnClickListener(v -> redrawApplicant());
        // Dump Pending Applicant Button
        // dumpPendingButton.setOnClickListener(v -> dumpPendingApplicant());
        return view;
    }

    /**
     * Loads the capacity of the event from Firestore, setting the maximum number of selected participants.
     */
    private void loadEventDetails() {
        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                eventName = event.getName();
                eventCapacity = event.getCapacity();
            }
            @Override
            public void onEventLoadError(String message) {
                // implement error logic
            }
        });
    }

    /**
     * Refreshes the selected list by reloading data from Firestore and updating the RecyclerView.
     */
    public void refreshList() {
        if (getContext() == null) {return;}
        selectedList.clear();
        adapter.notifyDataSetChanged();

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "selectedList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            /**
             * This function is for toggling the empty state once users are added to the list
             *
             * @param userIdsList List of user Id in the subcollection
             */
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                if (userIdsList.isEmpty()) {
                    Log.i("SelectedListFragment", "refreshList(): userIdsList is EMPTY");
                    showEmptyState(true);
                    return;
                }
                showEmptyState(false);
                AtomicInteger loadedUsersCount = new AtomicInteger(0);

                for (String userId : userIdsList) {
                    UserDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
                        /**
                         * This function updates the cancelled list when users are obtained
                         * @param user a given user
                         */
                        @Override
                        public void onUserLoaded(User user) {
                            selectedList.add(user);
                            adapter.notifyDataSetChanged();
                            if (loadedUsersCount.incrementAndGet() == userIdsList.size()) {
                                finalizeRefreshList();
                            }
                        }
                        @Override
                        public void onUserLoadError(String error) {
                            if (getContext() == null) return;
                            Log.e("SelectedList", "Error loading user: " + error);
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


    /**
     * This function updates the UI of the list, and check various aspects of the selected list.
     */
    private void finalizeRefreshList() {
        if (selectedList.isEmpty()) {
            Toast.makeText(getContext(), "No users in selected list", Toast.LENGTH_SHORT).show();
            Log.i("SelectedListFragment refreshList()", "no users in selected list!");
        }
        selectedCountTextView.setText("Selected Count: " + selectedList.size());
        Log.i("SelectedListFragment", "refreshList(), selectedListSize: " + selectedList.size());

        if (selectedList.size() < eventCapacity) {
            redrawButton.setVisibility(View.VISIBLE);
        } else {
            redrawButton.setVisibility(View.GONE);
        }
        // Redraw Applicant Button
        redrawButton.setOnClickListener(v -> redrawApplicant());

        // Dump Pending Applicant Button
        dumpPendingButton.setOnClickListener(v -> dumpPendingApplicant());

    }

    private void dumpPendingApplicant() {
        Log.i("SelectedListFragment", "dumpPendingApplicant being called");
        dumpPendingButton.setEnabled(false);

        if (selectedList.isEmpty()) {
            Toast.makeText(getContext(), "No users in selected list", Toast.LENGTH_SHORT).show();
            dumpPendingButton.setEnabled(true);
            return;
        }

        List<User> usersToRemove = new ArrayList<>();
        AtomicInteger checksCompleted = new AtomicInteger(0);
        int totalChecks = selectedList.size();

        for (User user : selectedList) {
            OrganizerDatabase.isPendingStatusForUser(user.getDeviceId(), eventId, new OrganizerDatabase.OnIsPendingStatusCheckedListener() {
                @Override
                public void onStatusLoaded(boolean isPending) {
                    if (isPending) {
                        usersToRemove.add(user);
                        moveUserToCancelledList(user.getDeviceId());
                        sendCancelledNotif(user.getDeviceId());
                        updateInvitationStatus(user.getDeviceId());
                    }
                    checkIfAllProcessed();
                }

                @Override
                public void onError(String error) {
                    Log.e("dumpPendingApplicant", "Error checking pending status for user " + user.getDeviceId() + ": " + error);
                    checkIfAllProcessed();
                }

                private void checkIfAllProcessed() {
                    if (checksCompleted.incrementAndGet() == totalChecks) {
                        onAllPendingChecksCompleted(usersToRemove);
                    }
                }
            });
        }
    }

    private void onAllPendingChecksCompleted(List<User> usersToRemove) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (!usersToRemove.isEmpty()) {
                selectedList.removeAll(usersToRemove);
                adapter.notifyDataSetChanged();
                selectedCountTextView.setText("Selected Count: " + selectedList.size());
                refreshAllFragments();
                Toast.makeText(getContext(), "Pending users have been removed.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No pending users to remove.", Toast.LENGTH_SHORT).show();
            }
            dumpPendingButton.setEnabled(true);
        });
    }

    private void movePendingUsersToCancelledList(List<User> pendingUsers) {
        Log.i("SelectedListFragment", "All user statuses checked. Moving pending users.");
        for (User user : pendingUsers) {
            moveUserToCancelledList(user.getDeviceId());
           // selectedList.notifyDataSetChanged();
            moveUserBetweenUserSubcollections(user.getDeviceId(), eventId, "registeredEvents", "missedOutEvents");
        }
        // refreshList();
        if (pendingUsers.isEmpty()) {
            dumpPendingButton.setVisibility(View.GONE);
        } else {
            dumpPendingButton.setEnabled(true);
        }
        Log.i("refreshing all fragments", "refreshAllFragments()");
        refreshAllFragments();
    }

    private void moveUserBetweenUserSubcollections(String userId, String eventId, String subcollectionFrom, String subcollectionTo) {
        EntrantDatabase.moveUserBetweenUserSubcollections(userId, eventId, subcollectionFrom, subcollectionTo, new EntrantDatabase.OnOperationCompleteListener() {
            @Override
            public void onComplete(boolean success) {
                // implement complete logic / toasts
            }
        });
    }


    /**'
     * This function moves a user to the final list
     *
     * @param user a give user
     */
    private void moveUserToFinalList(User user) {
        OrganizerDatabase.moveUserBetweenEventSubcollections(eventId, user.getDeviceId(), "selectedList", "finalList", new OrganizerDatabase.OnOperationCompleteListener() {
            @Override
            public void onComplete(boolean success) {
                EntrantDatabase.addEventToRegisteredEvents(user.getDeviceId(), eventId);
            }
        });
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
    }

    private void drawFromWaitingList() {
        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "waitingList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                if (userIdsList.isEmpty()) {
                    Toast.makeText(getContext(), "No users in waiting list", Toast.LENGTH_SHORT).show();
                    return;
                }

                int remainingSlots = eventCapacity - selectedList.size();
                if (remainingSlots <= 0) {
                    Toast.makeText(getContext(), "Event is already at capacity.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Shuffle the waiting list to ensure random selection
                Collections.shuffle(userIdsList);

                // Determine the number of users to draw
                int drawCount = Math.min(remainingSlots, userIdsList.size());

                // Separate selected and remaining users
                List<String> selectedUserIds = new ArrayList<>(userIdsList.subList(0, drawCount));
                List<String> remainingUserIds = new ArrayList<>(userIdsList.subList(drawCount, userIdsList.size()));

                // Move selected users to selected list
                for (String userId : selectedUserIds) {
                    UserDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(User user) {
                            moveUserToSelectedList(user); // Move user to selected list
                        }

                        @Override
                        public void onUserLoadError(String error) {
                            Log.e("DrawFromWaitingList", "Failed to load user: " + userId);
                        }
                    });
                }

                // Notify users not selected
                notifyNotSelectedUsers(remainingUserIds);
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
     * @param user       The user to be moved.
     * // @param onComplete Runnable executed once the move is complete.
     */
    private void moveUserToSelectedList(User user) {
        Notification inviteNotification = new Notification(
                new Date(),
                user.getDeviceId(),
                organizerId,
                eventName,
                "You have been selected for event: " + eventName,
                "Event Invitation",
                eventId,
                eventName
        );
        NotificationsDatabase.sendNotificationToUser(user.getDeviceId(), inviteNotification);

        OrganizerDatabase.moveUserToSelectedList(eventId, user.getDeviceId(), new OrganizerDatabase.OnOperationCompleteListener() {
                    @Override
                    public void onComplete(boolean success) {
                        // implement toast or something
                    }
        });
    }

    private void sendCancelledNotif(String userDeviceId) {
        Notification cancelNotification = new Notification(
                new Date(),
                // userDeviceId.getDeviceId(),
                userDeviceId,
                organizerId,
                eventName,
                "You have been removed from the event: " + eventId,
                "Cancellation",
                eventId,
                eventName
        );
        NotificationsDatabase.sendNotificationToUser(userDeviceId, cancelNotification);
    }

    private void moveUserToCancelledList(String userDeviceId) {

        // Now, move the user from selectedList to cancelledList
        OrganizerDatabase.moveUserBetweenEventSubcollections(eventId, userDeviceId, "selectedList", "cancelledList", moveSuccess -> {
            if (moveSuccess) {
                // Successfully moved to cancelledList
                // Removed user from selectedList and notify adapter
               // for (int i = 0; i < selectedList.size(); i++) {
                    //User user = selectedList.get(i);
                   // if (user.getDeviceId().equals(userDeviceId)) {
                    //    selectedList.remove(i);
                    //    adapter.notifyItemRemoved(i);
                    //    break;
                 //   }
              //  }
             //   selectedCountTextView.setText("Selected Count: " + selectedList.size());  // Update selected count
                loadSelectedList();
            } else {
                // Handle any failure during the move operation
                Log.e("moveUserToCancelledList", "Failed to move user to cancelled list");
            }
        });
    }

    private void updateInvitationStatus(String userDeviceId) {
    // Update the invitationStatus for the user in the selectedList subcollection to "Rejected"
        OrganizerDatabase.updateInvitationStatus(userDeviceId, eventId, "Rejected", success -> {
            //implement success logic
        });
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

    private void notifyNotSelectedUsers(List<String> remainingUserIds) {
        for (String userId : remainingUserIds) {
            Notification notification = new Notification(
                    new Date(),
                    userId,
                    organizerId,
                    eventName,
                    "You were not selected for event: " + eventId,
                    "Event Update",
                    eventId,
                    eventName
            );
            NotificationsDatabase.sendNotificationToUser(userId, notification);
        }
    }
}