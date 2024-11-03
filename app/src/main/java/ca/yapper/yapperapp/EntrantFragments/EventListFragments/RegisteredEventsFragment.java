package ca.yapper.yapperapp.EntrantFragments.EventListFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.EventsAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

public class RegisteredEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_registeredevents, container, false);

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
        CollectionReference eventsRef = db.collection("Events");

        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String eventName = document.getString("eventName");
                    String eventDateTime = document.getString("eventDate");
                    String eventRegDeadline = document.getString("registrationDeadline");
                    String eventFacilityName = ""; // Placeholder if not stored in Firestore
                    String eventFacilityLocation = ""; // Placeholder if not stored in Firestore
                    int eventAttendees = 0; // Placeholder if not stored in Firestore
                    int eventWlCapacity = document.contains("wlCapacity") ? document.getLong("wlCapacity").intValue() : 0;
                    int eventWlSeatsLeft = document.contains("waitingListSeats") ? document.getLong("waitingListSeats").intValue() : 0;
                    boolean eventGeolocEnabled = false; // Placeholder if not stored in Firestore

                    Event event = null;
                    try {
                        event = new Event(eventName, eventDateTime, eventRegDeadline, eventFacilityName, eventFacilityLocation, eventAttendees, eventWlCapacity, eventWlSeatsLeft, eventGeolocEnabled);
                    } catch (WriterException e) {
                        throw new RuntimeException(e);
                    }
                    eventList.add(event);
                }
                adapter.notifyDataSetChanged();
            } else {
                // Handle errors
            }
        });
    }
}

