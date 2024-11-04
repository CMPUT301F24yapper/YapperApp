package ca.yapper.yapperapp.OrganizerFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrganizerSelectRandomPoolFragment extends Fragment {
    private FirebaseFirestore db;
    private String eventId; // Pass this value when initializing the Fragment
    private List<String> userDeviceIds;
    private int eventAttendees;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_select_random_pool, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        userDeviceIds = new ArrayList<>();

        loadEventData();

        return view;
    }

    private void loadEventData() {
        // Retrieve eventAttendees count and waitingList from Firestore
        db.collection("Events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                eventAttendees = documentSnapshot.getLong("eventAttendees").intValue();

                // Access the "waitingList" subcollection
                db.collection("Events").document(eventId).collection("waitingList").get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                userDeviceIds.add(doc.getId()); // Assuming doc ID is 'entrantDeviceId'
                            }
                            createRandomPool();
                        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching waitingList", e));
            }
        }).addOnFailureListener(e -> Log.e("Firestore", "Error fetching event document", e));
    }

    private void createRandomPool() {
        if (userDeviceIds.size() >= eventAttendees) {
            Collections.shuffle(userDeviceIds);
            List<String> randomPool = userDeviceIds.subList(0, eventAttendees);
            // Use randomPool for your UI or further processing
            Log.d("RandomPool", "Randomized Pool: " + randomPool);
        } else {
            Log.w("RandomPool", "Not enough users in waitingList to meet eventAttendees requirement");
        }
    }
}

}
