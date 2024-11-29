package ca.yapper.yapperapp.AdminFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.R;
import java.util.List;
import java.util.Map;

/**
 * Fragment for the admins home page which displays statistics regarding events, users, facilities
 * and the biggest events on the app.
 */
public class AdminHomeFragment extends Fragment {

    private TextView totalEventsText;
    private TextView totalOrganizersText;
    private TextView totalUsersText;
    private LinearLayout eventsListContainer;


    /**
     * Function which inflates the admin home page view group, initializes view UI elements,
     * obtains statistics, then obtains the stats for the largest events.
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
        View view = inflater.inflate(R.layout.admin_homepage, container, false);

        initializeViews(view);
        loadStats();
        loadBiggestEvents();

        return view;
    }

    /**
     * Function that attaches UI elements with variables for ease of access and modification
     *
     * @param view The UI element that will be used for the various text views
     */
    private void initializeViews(View view) {
        totalEventsText = view.findViewById(R.id.total_events);
        totalOrganizersText = view.findViewById(R.id.organizers);
        totalUsersText = view.findViewById(R.id.users);
        eventsListContainer = view.findViewById(R.id.events_list_container);
    }

    /**
     * Function that sets the specified UI elements to statistics obtained from the database
     */
    private void loadStats() {
        AdminDatabase.getAdminStats().addOnSuccessListener(stats -> {
            if (getContext() == null) return;

            totalEventsText.setText(String.valueOf(stats.get("totalEvents")));
            totalOrganizersText.setText(String.valueOf(stats.get("totalOrganizers")));
            totalUsersText.setText(String.valueOf(stats.get("totalUsers")));
        });
    }

    /**
     * Function that organizes all the events by total users and then displays a list with the
     * five largest events.
     */
    private void loadBiggestEvents() {
        AdminDatabase.getBiggestEvents().addOnSuccessListener(events -> {
            if (getContext() == null) return;

            eventsListContainer.removeAllViews();

            for (Map<String, Object> event : events) {
                LinearLayout eventRow = new LinearLayout(getContext());
                eventRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                eventRow.setOrientation(LinearLayout.HORIZONTAL);

                TextView nameView = new TextView(getContext());
                nameView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                nameView.setText((String) event.get("name"));

                TextView capacityView = new TextView(getContext());
                capacityView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                capacityView.setText(String.valueOf(event.get("capacity")));

                eventRow.addView(nameView);
                eventRow.addView(capacityView);
                eventsListContainer.addView(eventRow);
            }
        });
    }
}