package ca.yapper.yapperapp.EntrantFragments.EventListFragments;

import android.os.Bundle;
import android.provider.Settings;
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

import ca.yapper.yapperapp.EventsAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;
/**
 * RegisteredEventsFragment displays a list of events that the user has registered for.
 * This fragment retrieves the registered events from Firestore based on the user's device ID
 * and displays them in a RecyclerView.
 */
public class RegisteredEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private String userDeviceId;


    /**
     * Inflates the fragment layout and initializes Firestore, RecyclerView, and adapter components.
     * Loads the user's registered events from Firestore.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_registeredevents, container, false);

        // Retrieve the device ID to use as the user identifier
        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        recyclerView = view.findViewById(R.id.recyclerView);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list and adapter for displaying events
        eventList = new ArrayList<>();
        adapter = new EventsAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        loadEventsFromFirebase();

        return view;
    }


    /**
     * Loads the user's registered events from the "registeredEvents" subcollection in Firestore.
     * Updates the RecyclerView adapter with the retrieved events.
     */
    private void loadEventsFromFirebase() {
        if (userDeviceId == null) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Unable to get user ID", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        db.collection("Users")
                .document(userDeviceId)
                .collection("registeredEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "No registered events found", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();

                        Event.loadEventFromDatabase(eventId, new Event.OnEventLoadedListener() {
                            @Override
                            public void onEventLoaded(Event event) {
                                if (getContext() == null) return;
                                eventList.add(event);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onEventLoadError(String error) {
                                if (getContext() == null) return;
                                Log.e("RegisteredEvents", "Error loading event " + eventId + ": " + error);
                                Toast.makeText(getContext(),
                                        "Error loading event: " + error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Error loading registered events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
