package ca.yapper.yapperapp.EntrantFragments.EventListFragments;

import android.os.Bundle;
import android.provider.Settings;
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
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Adapters.EventsAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

/**
 * Fragment for the entrants page that shows users joined events
 */
public class JoinedEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
    private String userDeviceId;
    private TextView emptyTextView;
    private ImageView emptyImageView;

    /**
     * Inflates the fragments layout, sets up views, and starts obtaining/displaying user events
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     *
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_joinededevents, container, false);

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        adapter = new EventsAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        emptyTextView = view.findViewById(R.id.emptyTextView);
        emptyImageView = view.findViewById(R.id.emptyImageView);

        loadEvents();

        return view;
    }


    /**
     * This function updates the UI for the joined events page. Along with change the
     * page depending on if the page is empty or not.
     */
    private void loadEvents() {
        EntrantDatabase.loadEventsList(userDeviceId, EntrantDatabase.EventListType.JOINED, new EntrantDatabase.OnEventsLoadedListener() {

            /**
             * This function updates the UI to display/remove messages for empty pages
             * @param events a list of events
             */
            @Override
            public void onEventsLoaded(List<Event> events) {
                if (getContext() == null) return;
                eventList.clear();
                eventList.addAll(events);
                adapter.notifyDataSetChanged();

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

            /**
             * This function takes a given error and prints a message for it
             *
             * @param error a given error
             */
            @Override
            public void onError(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}