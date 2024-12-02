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
import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.Adapters.UsersAdapter;

/**
 * The fragment for organizers to view the users in the cancelled list
 */
public class CancelledListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> cancelledList;
    private String eventId;
    private View emptyStateContainer;


    /**
     * Inflates the fragments layout, sets up views, initializes arrays and starts updating the UI for users.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_cancellist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        cancelledList = new ArrayList<>();
        adapter = new UsersAdapter(cancelledList, getContext());
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            if (eventId == null || eventId.isEmpty()) {
                Log.e("CancelledListFragment", "Event ID is missing. Cannot load cancelled list.");
                Toast.makeText(getContext(), "Error: Event ID is missing.", Toast.LENGTH_SHORT).show();
            } else {
                loadCancelledList();
            }
        } else {
            Log.e("CancelledListFragment", "No arguments provided. Cannot load cancelled list.");
            Toast.makeText(getContext(), "Error: No event information provided.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }


    /**
     * This function updates the UI
     */
    public void refreshList() {
        if (getContext() == null) return;
        loadCancelledList();
    }


    /**
     * This function is for validating the cancelled list and updating the UI
     */
    private void loadCancelledList() {
        if (getContext() == null || eventId == null || eventId.isEmpty()) {
            Log.e("CancelledListFragment", "Context or Event ID is null. Cannot load cancelled list.");
            return;
        }

        cancelledList.clear();
        adapter.notifyDataSetChanged();

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "cancelledList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            /**
             * This function is for toggling the empty state once users are added to the list
             *
             * @param userIdsList List of user Id in the subcollection
             */
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                if (userIdsList.isEmpty()) {
                    Log.d("CancelledListFragment", "No users found in the cancelled list.");
                    toggleEmptyState(true);
                    return;
                }

                toggleEmptyState(false);

                // Fetch User details for each user ID
                for (String userId : userIdsList) {
                    EntrantDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
                        /**
                         * This function updates the cancelled list when users are obtained
                         * @param user a given user
                         */
                        @Override
                        public void onUserLoaded(User user) {
                            if (getContext() == null) return;

                            cancelledList.add(user);

                            // Notify adapter only once after all users are added
                            if (cancelledList.size() == userIdsList.size()) {
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onUserLoadError(String error) {
                            if (getContext() == null) return;

                            Log.e("CancelledListFragment", "Error loading user: " + error);
                        }
                    });
                }
            }
            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Log.e("CancelledListFragment", "Error loading user IDs: " + error);
                    Toast.makeText(getContext(), "Error loading cancelled list: " + error, Toast.LENGTH_SHORT).show();
                }
                toggleEmptyState(true);
            }
        });
    }


    /**
     * This function checks if the list is empty and updates the UI to display different views
     *
     * @param isEmpty a value indicating if the list is empty
     */
    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
