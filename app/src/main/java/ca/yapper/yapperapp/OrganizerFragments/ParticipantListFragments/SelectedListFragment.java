package ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersAdapter;

public class SelectedListFragment extends Fragment {
    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> selectedList;
    private FirebaseFirestore db;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_waitlist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedList = new ArrayList<>();
        adapter = new UsersAdapter(selectedList, getContext());
        recyclerView.setAdapter(adapter);
        // TO-DO: SET 'EVENTID' TO BUNDLE PARAMETER #1 SENT FROM HOMEPAGE TO SPECIFIC EVENT CLICK NAVIGATION!
        // add in event parameters bundle... etc
        db = FirebaseFirestore.getInstance();
        //loadUsersFromFirebase(eventId);

        return view;
    }

    private void loadUsersFromFirebase(String eventId) {
        Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
            @Override
            public void onEventLoaded(Event event) {
                for (String userId : event.getSelectedList()) {
                    User.loadUserFromDatabase(userId, new User.OnUserLoadedListener() {
                        @Override
                        public void onUserLoaded(User user) {
                            selectedList.add(user);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onUserLoadError(String error) {
                            Log.e("SelectedListFragment", "Error loading user: " + error);
                        }
                    });
                }
            }

            @Override
            public void onEventLoadError(String error) {
                Log.e("SelectedListFragment", "Error loading event: " + error);
            }
        });
    }

}