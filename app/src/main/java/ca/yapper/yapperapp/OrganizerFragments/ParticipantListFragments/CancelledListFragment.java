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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.UsersAdapter;
/**
 * CancelledListFragment displays a list of users who have cancelled their participation
 * in a specific event. The list is retrieved from Firestore and shown in a RecyclerView.
 */
public class CancelledListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> cancelledList;
    private FirebaseFirestore db;
    private String eventId;


    /**
     * Inflates the fragment layout, initializes Firestore, RecyclerView, and adapter components,
     * and loads the cancelled list for the specified event.
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
        cancelledList = new ArrayList<>();
        adapter = new UsersAdapter(cancelledList, getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadCancelledList();
        }

        return view;
    }


    /**
     * Refreshes the cancelled list by reloading data from Firestore and updating the RecyclerView.
     */
    public void refreshList() {
        if (getContext() == null) return;
        loadCancelledList();
    }


    /**
     * Loads the cancelled list from the "cancelledList" subcollection of the event document in Firestore.
     * Updates the RecyclerView adapter with the retrieved users.
     */
    private void loadCancelledList() {
        if (getContext() == null) return;

        cancelledList.clear();
        adapter.notifyDataSetChanged();

        db.collection("Events").document(eventId)
                .collection("cancelledList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String userId = document.getId();
                        User.loadUserFromDatabase(userId, new User.OnUserLoadedListener() {
                            @Override
                            public void onUserLoaded(User user) {
                                if (getContext() == null) return;
                                cancelledList.add(user);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onUserLoadError(String error) {
                                if (getContext() == null) return;
                                Log.e("CancelledList", "Error loading user: " + error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading cancelled list", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}