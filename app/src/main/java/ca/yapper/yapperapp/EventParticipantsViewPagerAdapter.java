package ca.yapper.yapperapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.CancelledListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.FinalListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.SelectedListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.WaitingListFragment;
/**
 * An adapter class for managing fragments in a ViewPager that displays different lists of event participants.
 * The ViewPager has four tabs: Waiting, Selected, Final, and Cancelled.
 * Each tab displays a list of participants with a specific status for the event.
 */
public class EventParticipantsViewPagerAdapter extends FragmentStateAdapter {

    private final String eventId;
    private final String[] tabTitles = new String[]{"Waiting", "Selected", "Final", "Cancelled"};
    private final Map<Integer, Fragment> fragmentMap;


    /**
     * Constructs a new {@code EventParticipantsViewPagerAdapter} with the specified fragment and event ID.
     *
     * @param fragment The fragment hosting this adapter.
     * @param eventId  The ID of the event to load participant data for.
     */
    public EventParticipantsViewPagerAdapter(@NonNull Fragment fragment, String eventId) {
        super(fragment);
        this.eventId = eventId;
        this.fragmentMap = new HashMap<>();
    }


    /**
     * Creates and returns a new fragment based on the position in the ViewPager.
     * Each fragment corresponds to a different list of participants in the event, based on their status.
     *
     * @param position The position of the tab in the ViewPager.
     *                 0 = Waiting list, 1 = Selected list, 2 = Final list, 3 = Cancelled list.
     * @return A {@link Fragment} instance representing the participants in the specified list.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new WaitingListFragment();
                break;
            case 1:
                fragment = new SelectedListFragment();
                break;
            case 2:
                fragment = new FinalListFragment();
                break;
            case 3:
                fragment = new CancelledListFragment();
                break;
            default:
                fragment = new WaitingListFragment();
                break;
        }

        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);

        fragmentMap.put(position, fragment);
        return fragment;
    }


    /**
     * Refreshes the participant lists in all fragments. This method calls {@code refreshList} on each fragment
     * to ensure the displayed data is up-to-date.
     */
    public void refreshAllLists() {
        for (Fragment fragment : fragmentMap.values()) {
            if (fragment instanceof WaitingListFragment) {
                ((WaitingListFragment) fragment).refreshList();
            }
            else if (fragment instanceof SelectedListFragment) {
                ((SelectedListFragment) fragment).refreshList();
            }
            else if (fragment instanceof FinalListFragment && fragment.isAdded()) {
                ((FinalListFragment) fragment).refreshList();
            }
            else if (fragment instanceof CancelledListFragment && fragment.isAdded()) {
                ((CancelledListFragment) fragment).refreshList();
            }
        }
    }


    /**
     * Returns the total number of tabs (participant lists) in the ViewPager.
     *
     * @return The number of tabs, which is 4 (Waiting, Selected, Final, Cancelled).
     */
    @Override
    public int getItemCount() {
        return tabTitles.length;
    }
    /**
     * Returns the titles for the tabs in the ViewPager.
     *
     * @return An array of strings representing the titles of the tabs: "Waiting", "Selected", "Final", "Cancelled".
     */
    public String[] getTabTitles() {
        return tabTitles;
    }
}