package ca.yapper.yapperapp.EntrantFragments.EventListFragments;

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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.EventsAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
/**
 * MissedOutFragment displays a list of events that the user missed out on.
 * This fragment retrieves the missed events from Firestore based on the user's device ID
 * and displays them in a RecyclerView.
 */
public class MissedOutFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private String userDeviceId;

    private TextView emptyTextView;
    private ImageView emptyImageView;


    /**
     * Inflates the fragment layout and initializes Firestore, RecyclerView, and adapter components.
     * Loads the user's missed events from Firestore.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_missedevents, container, false);

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        adapter = new EventsAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);
        // db = FirebaseFirestore.getInstance();

        emptyTextView = view.findViewById(R.id.emptyTextView);
        emptyImageView = view.findViewById(R.id.emptyImageView);

        loadEventsFromFirebaseDebug();

        return view;
    }


    /**
     * Loads the user's missed events from the "missedOutEvents" subcollection in Firestore.
     * Updates the RecyclerView adapter with the retrieved events.
     */
    /** private void loadEventsFromFirebase() {
        if (userDeviceId == null) {
            Toast.makeText(getContext(), "Error: Unable to get user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .document(userDeviceId)
                .collection("missedOutEvents")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No missed events found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();

                        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
                            @Override
                            public void onEventLoaded(Event event) {
                                if (getContext() == null) return;
                                eventList.add(event);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onEventLoadError(String error) {
                                if (getContext() == null) return;
                                Log.e("MissedEvents", "Error loading event " + eventId + ": " + error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Error loading missed events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }**/


    private void loadEventsFromFirebaseDebug() {
        EntrantDatabase.loadEventsList(userDeviceId, EntrantDatabase.EventListType.MISSED, new EntrantDatabase.OnEventsLoadedListener() {
                    @Override
                    public void onEventsLoaded(List<Event> events) {
                        if (getContext() == null) return;
                        eventList.clear();
                        eventList.addAll(events);
                        adapter.notifyDataSetChanged();
                        // Hide empty state views and show the RecyclerView
                        if (eventList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyTextView.setVisibility(View.VISIBLE);
                            emptyImageView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyTextView.setVisibility(View.GONE);
                            emptyImageView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (getContext() == null) return;
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            /**db.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        // Show empty state views
                        recyclerView.setVisibility(View.GONE);
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyImageView.setVisibility(View.VISIBLE);
                        return;
                    }

                    // Hide empty state views and show the RecyclerView
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                    emptyImageView.setVisibility(View.GONE);


                    // PREVIOUS CODE
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(getContext(), "No events found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //*********************************
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String eventId = document.getId();

                        OrganizerDatabase.loadEventFromDatabase(eventId, new OrganizerDatabase.OnEventLoadedListener() {
                            @Override
                            public void onEventLoaded(Event event) {
                                if (getContext() == null) return;
                                eventList.add(event);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onEventLoadError(String error) {
                                if (getContext() == null) return;
                                Log.e("AllEvents", "Error loading event " + eventId + ": " + error);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Error loading events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }**/
    }
}