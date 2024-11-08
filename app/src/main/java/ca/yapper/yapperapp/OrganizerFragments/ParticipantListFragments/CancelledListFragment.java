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

public class CancelledListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> cancelledList;
    private FirebaseFirestore db;
    private String eventId;



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



    public void refreshList() {
        if (getContext() == null) return;
        loadCancelledList();
    }



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