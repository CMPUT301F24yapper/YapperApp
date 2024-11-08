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

public class FinalListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter adapter;
    private List<User> finalList;
    private FirebaseFirestore db;
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

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadFinalList();
        }

        return view;
    }



    public void refreshList() {
        if (getContext() == null) return;
        loadFinalList();
    }



    private void loadFinalList() {
        if (getContext() == null) return;

        finalList.clear();
        adapter.notifyDataSetChanged();

        db.collection("Events").document(eventId)
                .collection("finalList")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String userId = document.getId();
                        User.loadUserFromDatabase(userId, new User.OnUserLoadedListener() {
                            @Override
                            public void onUserLoaded(User user) {
                                if (getContext() == null) return;
                                finalList.add(user);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onUserLoadError(String error) {
                                if (getContext() == null) return;
                                Log.e("FinalList", "Error loading user: " + error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading final list", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}