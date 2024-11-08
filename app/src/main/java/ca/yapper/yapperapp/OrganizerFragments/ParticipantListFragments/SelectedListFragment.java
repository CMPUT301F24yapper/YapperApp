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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    private Button redrawButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_selectedlist, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedList = new ArrayList<>();
        adapter = new UsersAdapter(selectedList, getContext());
        recyclerView.setAdapter(adapter);

        redrawButton = view.findViewById(R.id.button_redraw);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadSelectedList(eventId);
        }

        redrawButton.setOnClickListener(v -> redrawApplicant());

        return view;
    }

    private void loadSelectedList(String eventId) {  // Change method name for each fragment
        db.collection("Events").document(eventId)
                .collection("selectedList")  // Change collection name for each fragment
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    selectedList.clear();  // Change list name for each fragment
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String userId = document.getId();
                        User.loadUserFromDatabase(userId, new User.OnUserLoadedListener() {
                            @Override
                            public void onUserLoaded(User user) {
                                if (getContext() == null) return;
                                selectedList.add(user);  // Change list name for each fragment
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onUserLoadError(String error) {
                                if (getContext() == null) return;
                                Log.e("SelectedList", "Error loading user: " + error);  // Change tag for each fragment
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading selected list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redrawApplicant() {
        if (selectedList.isEmpty()) {
            Toast.makeText(getContext(), "No applicants in the selected list", Toast.LENGTH_SHORT).show();
            return;
        }

        Random random = new Random();
        int index = random.nextInt(selectedList.size());
        User selectedUser = selectedList.get(index);

        moveUserToWaitingList(selectedUser);
    }

    private void moveUserToWaitingList(User user) {
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Events").document(eventId)
                .collection("selectedList")
                .document(user.getDeviceId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("Events").document(eventId)
                            .collection("waitingList")
                            .document(user.getDeviceId())
                            .set(timestamp)
                            .addOnSuccessListener(aVoid2 -> {
                                selectedList.remove(user);
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "User moved back to waiting list", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Log.e("FirestoreError", "Error adding to waiting list", e));
                })
                .addOnFailureListener(e -> Log.e("FirestoreError", "Error removing from selected list", e));
    }
}