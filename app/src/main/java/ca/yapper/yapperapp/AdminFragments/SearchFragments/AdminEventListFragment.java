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

import ca.yapper.yapperapp.AdminEventAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.Databases.AdminDatabase;

public class AdminEventListFragment extends Fragment {
    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private List<Event> eventList;
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_eventlist, container, false);

        recyclerView = view.findViewById(R.id.events_recycler_view);
        searchBar = view.findViewById(R.id.search_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        adapter = new AdminEventAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        setupSearch();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        AdminDatabase.getAllEvents()
                .addOnSuccessListener(events -> {
                    eventList.clear();
                    eventList.addAll(events);
                    adapter.notifyDataSetChanged();
                });
    }

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

    private void filterEvents(String searchText) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : eventList) {
            if (event.getName().toLowerCase().contains(searchText)) {
                filteredList.add(event);
            }
        }
        adapter = new AdminEventAdapter(filteredList, getContext());
        recyclerView.setAdapter(adapter);
    }
}