package ca.yapper.yapperapp.OrganizerFragments;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.Adapters.EventsAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

/**
 * OrganizerHomeFragment displays a list of events created by the organizer.
 * The fragment retrieves events from Firestore and displays them in a RecyclerView,
 * allowing organizers to view their events and manage participants.
 */
public class OrganizerHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
    //private FirebaseFirestore db;
    private String userDeviceId;

    private TextView emptyTextView;
    private ImageView emptyImageView;


    /**
     * Inflates the fragment layout, initializes Firestore, RecyclerView, and adapter components,
     * and loads the list of events created by the organizer.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_homepage, container, false);

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        recyclerView = view.findViewById(R.id.my_events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        adapter = new EventsAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        emptyTextView = view.findViewById(R.id.emptyTextView);
        emptyImageView = view.findViewById(R.id.emptyImageView);

        //db = FirebaseFirestore.getInstance();
        loadEventsFromFirebase();

        return view;
    }


    /**
     * Loads the organizer's created events from the "createdEvents" subcollection in Firestore.
     * Updates the RecyclerView adapter with the retrieved events.
     */
    private void loadEventsFromFirebase() {
        OrganizerDatabase.loadCreatedEvents(userDeviceId, new OrganizerDatabase.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<String> eventIds) {
                eventList.clear();

                if (eventIds.isEmpty()) {
                    // Show empty state
                    recyclerView.setVisibility(View.GONE);
                    getView().findViewById(R.id.empty_state_layout).setVisibility(View.VISIBLE);
                } else {
                    // Show events list
                    getView().findViewById(R.id.empty_state_layout).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                for (String eventId : eventIds) {
                    OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
                        @Override
                        public void onEventLoaded(Event event) {
                            eventList.add(event);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onEventLoadError(String error) {
                            Log.e("OrganizerHome", "Error loading event: " + error);
                        }
                    });
                }
            }

            @Override
            public void onEventsLoadError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        /**if (userDeviceId == null) {
            Toast.makeText(getContext(), "Error: Unable to get user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .document(userDeviceId)
                .collection("createdEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();
                        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
                            @Override
                            public void onEventLoaded(Event event) {
                                eventList.add(event);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onEventLoadError(String error) {
                                Log.e("OrganizerHome", "Error loading event: " + error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });**/
    }
}