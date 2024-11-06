package ca.yapper.yapperapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.CancelledListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.FinalListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.SelectedListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.WaitingListFragment;

public class EventParticipantsViewPagerAdapter extends FragmentStateAdapter {

    private String eventId;

    public EventParticipantsViewPagerAdapter(@NonNull Fragment fragment, String eventId) {
        super(fragment);
        this.eventId = eventId;
    }

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
            default:
                fragment = new CancelledListFragment();
                break;
        }

        // Pass eventId to each fragment
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
