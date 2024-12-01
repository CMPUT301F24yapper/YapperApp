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
import ca.yapper.yapperapp.UsersAdapter;

public class CancelledListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> cancelledList;
    private String eventId;
    private View emptyStateContainer;

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

    public void refreshList() {
        if (getContext() == null) return;
        loadCancelledList();
    }

    private void loadCancelledList() {
        if (getContext() == null || eventId == null || eventId.isEmpty()) {
            Log.e("CancelledListFragment", "Context or Event ID is null. Cannot load cancelled list.");
            return;
        }

        cancelledList.clear();
        adapter.notifyDataSetChanged();

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "cancelledList", new OrganizerDatabase.OnUserIdsLoadedListener() {
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
