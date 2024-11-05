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
                    String eventName = document.getString("name");
                    String eventDateTime = document.getString("date_Time");
                    String eventRegDeadline = document.getString("registrationDeadline");
                    String eventFacilityName = document.getString("facilityName");
                    String eventFacilityLocation = document.getString("facilityLocation");
                    int eventCapacity = document.contains("capacity") ?
                            document.getLong("capacity").intValue() : 0;
                    int eventWlCapacity = document.contains("waitListCapacity") ?
                            document.getLong("waitListCapacity").intValue() : 0;
                    boolean eventGeolocEnabled = document.getBoolean("isGeolocationEnabled");

                    Event event = null;
                    try {
                        event = new Event(eventName, eventDateTime, eventRegDeadline,
                                eventFacilityName, eventFacilityLocation, eventCapacity,
                                eventWlCapacity, eventWlCapacity, eventGeolocEnabled);
                        // Store the document ID in the QR code
                        if (event.getQRCode() != null) {
                            event.getQRCode().setQRCodeValue(document.getId());
                        }
                    } catch (WriterException e) {
                        throw new RuntimeException(e);
                    }
                    eventList.add(event);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}