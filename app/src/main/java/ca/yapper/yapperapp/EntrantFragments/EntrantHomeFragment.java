package ca.yapper.yapperapp.EntrantFragments;

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

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.ViewPagerAdapter;
/**
 * EntrantHomeFragment serves as the main home view for Entrants, displaying different
 * categories of events in tabs: Registered Events, Joined Events, and Missed Out events.
 * This fragment uses a ViewPager2 with a TabLayout for navigation between these categories.
 */
public class EntrantHomeFragment extends Fragment {

    /**
     * Inflates the fragment layout, sets up the ViewPager2 with a TabLayout, and
     * configures each tab to display a specific event category.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_homepage, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new ViewPagerAdapter(this));

        // Attach TabLayout and ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Registered Events");
                            break;
                        case 1:
                            tab.setText("Joined Events");
                            break;
                        case 2:
                            tab.setText("Missed Out");
                            break;
                    }
                }).attach();

        return view;
    }
}
