package ca.yapper.yapperapp.AdminFragments.SearchFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;

import ca.yapper.yapperapp.AdminEventAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.Databases.AdminDatabase;

/**
 * Fragment to display the event stored in the database as lists that only admins can browse.
 */
public class AdminEventListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private List<Event> eventList;
    private EditText searchBar;
    private LinearLayout emptyStateLayout;

    /**
     * Inflates the fragments layout, sets up views, and starts search function
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
        View view = inflater.inflate(R.layout.admin_eventlist, container, false);

        recyclerView = view.findViewById(R.id.events_recycler_view);
        searchBar = view.findViewById(R.id.search_bar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        adapter = new AdminEventAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        setupSearch();

        return view;
    }

    /**
     * Displays stored events specific to the fragment and updates UI
     */
    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    /**
     * Obtains event information from all events and displays them while updating the UI.
     */
    private void loadEvents() {
        AdminDatabase.getAllEvents()
                .addOnSuccessListener(events -> {
                    eventList.clear();
                    eventList.addAll(events);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    /**
     * Function that sets up the search functionality for admin lists of events
     * Uses the listener to detect changes in text and update whats shown on the lists accordingly.
     */
    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase();
                filterEvents(searchText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     *  Function that filters event list based on searchText string and updates the UI with
     *  an adapter.
     * @param searchText Text from user search bar input, used to filter the event list.
     */
    private void filterEvents(String searchText) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : eventList) {
            if (event.getName().toLowerCase().contains(searchText)) {
                filteredList.add(event);
            }
        }
        adapter = new AdminEventAdapter(filteredList, getContext());
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    /**
     * Function to check if list is empty and change visibility accordingly. If list is empty,
     * it will display a custom message.
     */
    private void updateEmptyState() {
        if (eventList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}