package ca.yapper.yapperapp.OrganizerFragments;

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
import ca.yapper.yapperapp.UMLClasses.User;

public class OrganizerHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_homepage, container, false);
        // implement listView of Events pulled from Firestore, onClickListeners logic & navigability

        recyclerView = view.findViewById(R.id.my_events_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();
        adapter = new EventsAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        //loadEventsFromFirebase();

        return view;
    }

//    private void loadEventsFromFirebase() {
//        User currentUser = User.loadUserFromDatabase(userDeviceId);
//        if (currentUser != null && currentUser.isOrganizer()) {
//            // Assuming there's a field for organizer's events
//            for (String eventId : currentUser.getOrganizerEvents()) {
//                Event event = Event.loadEventFromDatabase(eventId);
//                if (event != null) {
//                    eventList.add(event);
//                }
//            }
//            adapter.notifyDataSetChanged();
//        }
//    }
}
