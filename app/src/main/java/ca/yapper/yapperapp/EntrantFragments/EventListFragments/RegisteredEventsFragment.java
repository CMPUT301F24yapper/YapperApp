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
                    String eventFacility = document.getString("eventFacility");

                    // Directly convert Timestamp to String
                    String eventDate = document.getTimestamp("eventDate").toDate().toString();
                    String registrationDeadline = document.getTimestamp("registrationDeadline").toDate().toString();

                    // Retrieve waitingListSeats as a number
                    int waitingListSeats = document.getLong("waitingListSeats").intValue();

                    Event event = new Event(eventName, eventFacility, eventDate, registrationDeadline, 0, 0, waitingListSeats, false);
                    eventList.add(event);
                }
                adapter.notifyDataSetChanged();
            } else {
                // Handle errors if needed
            }
        });
    }

}

