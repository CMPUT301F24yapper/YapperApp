package ca.yapper.yapperapp.OrganizerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ca.yapper.yapperapp.Adapters.EventParticipantsViewPagerAdapter;
import ca.yapper.yapperapp.R;
/**
 * ViewParticipantsFragment displays tabs for viewing different participant lists
 * (e.g., Waiting, Selected, Final, Cancelled) for a specific event.
 * The fragment sets up a TabLayout with a ViewPager2 for navigation between lists.
 */
public class ViewParticipantsFragment extends Fragment {

    private String eventId;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private EventParticipantsViewPagerAdapter viewPagerAdapter;


    /**
     * Inflates the fragment layout, retrieves the event ID from arguments, and
     * initializes the ViewPager2 and TabLayout for navigating between participant lists.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_viewtab, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPagerAdapter = new EventParticipantsViewPagerAdapter(this, eventId);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(viewPagerAdapter.getTabTitles()[position])
        ).attach();

        return view;
    }
}