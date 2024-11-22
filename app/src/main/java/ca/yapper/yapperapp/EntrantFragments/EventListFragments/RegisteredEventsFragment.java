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
import java.util.ArrayList;
import java.util.List;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.EventsAdapter;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Event;

public class RegisteredEventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
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

        loadEvents();

        return view;
    }

    private void loadEvents() {
        EntrantDatabase.loadEventsList(userDeviceId, EntrantDatabase.EventListType.REGISTERED, new EntrantDatabase.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(List<Event> events) {
                if (getContext() == null) return;
                eventList.clear();
                eventList.addAll(events);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                if (getContext() == null) return;
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}