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

public class FinalListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> finalList;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_finallist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        finalList = new ArrayList<>();
        adapter = new UsersAdapter(finalList, getContext());
        recyclerView.setAdapter(adapter);

        // Retrieve eventId from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            if (eventId == null || eventId.isEmpty()) {
                Log.e("FinalListFragment", "Event ID is missing. Cannot load final list.");
                Toast.makeText(getContext(), "Error: Event ID is missing.", Toast.LENGTH_SHORT).show();
            } else {
                loadFinalList();
            }
        } else {
            Log.e("FinalListFragment", "No arguments provided. Cannot load final list.");
            Toast.makeText(getContext(), "Error: No event information provided.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadFinalList() {
        if (eventId == null || eventId.isEmpty()) {
            Log.e("FinalListFragment", "Event ID is missing. Cannot load final list.");
            return;
        }

        finalList.clear();
        adapter.notifyDataSetChanged();

        OrganizerDatabase.loadUserIdsFromSubcollection(eventId, "finalList", new OrganizerDatabase.OnUserIdsLoadedListener() {
            @Override
            public void onUserIdsLoaded(ArrayList<String> userIdsList) {
                if (userIdsList.isEmpty()) {
                    Log.d("FinalListFragment", "No users found in the final list.");
                    return;
                }

                // Fetch User details for each user ID
                for (String userId : userIdsList) {
                    EntrantDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(User user) {
                            if (getContext() == null) return;

                            finalList.add(user);

                            // Notify adapter only once after all users are added
                            if (finalList.size() == userIdsList.size()) {
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onUserLoadError(String error) {
                            if (getContext() == null) return;

                            Log.e("FinalListFragment", "Error loading user: " + error);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Log.e("FinalListFragment", "Error loading user IDs: " + error);
                    Toast.makeText(getContext(), "Error loading final list: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    /**
     * Refreshes the selected list by reloading data from Firestore and updating the RecyclerView.
     */
    public void refreshList() {
        if (getContext() == null) return;
        loadFinalList();
    }


}
