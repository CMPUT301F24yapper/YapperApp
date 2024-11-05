package ca.yapper.yapperapp.EntrantFragments.EventListFragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.EventsAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

public class MissedOutFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private String userDeviceId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_registeredevents, container, false);

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        adapter = new EventsAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadEventsFromFirebase();

        return view;
    }

    private void loadEventsFromFirebase() {
        CollectionReference usersRef = db.collection("Users");

        // Access the document for the current user based on their device ID
        usersRef.document(userDeviceId).collection("EntrantDetails").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Document found, proceed to access subcollections
                        DocumentReference entrantDetailsDoc = task.getResult().getDocuments().get(0).getReference();
                        // Load events from the 'missedOutEvents'
                        loadEventSubCollection(entrantDetailsDoc.collection("missedOutEvents"));
                    } else {
                        // Handle case where user's EntrantDetails or subcollections don't exist
                        Log.d("Firestore", "EntrantDetails or subcollections not found for user.");
                    }
                });
    }

    private void loadEventSubCollection(CollectionReference eventSubCollection) {
        eventSubCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot eventRefDoc : task.getResult()) {
                    // Retrieve the event ID stored as a reference
                    String eventId = eventRefDoc.getId();

                    // Fetch event details from the "Events" collection using the event ID
                    db.collection("Events").document(eventId).get().addOnCompleteListener(eventTask -> {
                        if (eventTask.isSuccessful() && eventTask.getResult() != null) {
                            DocumentSnapshot eventDoc = eventTask.getResult();

                            String eventName = eventDoc.getString("name");
                            String eventDateTime = eventDoc.getString("date_Time");
                            String eventRegDeadline = eventDoc.getString("registrationDeadline");
                            String eventFacilityName = eventDoc.getString("facilityName");
                            String eventFacilityLocation = eventDoc.getString("facilityLocation");
                            int eventCapacity = eventDoc.contains("capacity") ?
                                    eventDoc.getLong("capacity").intValue() : 0;
                            int eventWlCapacity = eventDoc.contains("waitListCapacity") ?
                                    eventDoc.getLong("waitListCapacity").intValue() : 0;
                            boolean eventGeolocEnabled = eventDoc.getBoolean("isGeolocationEnabled");

                            // Construct the Event object
                            Event event = null;
                            try {
                                event = new Event(eventName, eventDateTime, eventRegDeadline,
                                        eventFacilityName, eventFacilityLocation, eventCapacity,
                                        eventWlCapacity, eventGeolocEnabled);
                                // Set the event ID as QR code value if applicable
                                if (event.getQRCode() != null) {
                                    event.getQRCode().setQRCodeValue(eventId);
                                }
                            } catch (WriterException e) {
                                Log.e("Firestore", "Error generating QR code for event: " + eventId, e);
                            }

                            if (event != null) {
                                eventList.add(event);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("Firestore", "Event details not found for ID: " + eventId);
                        }
                    });
                }
            } else {
                Log.d("Firestore", "Failed to load subcollection: " + eventSubCollection.getId());
            }
        });
    }
}
