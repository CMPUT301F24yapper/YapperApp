package ca.yapper.yapperapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.CancelledListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.FinalListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.SelectedListFragment;
import ca.yapper.yapperapp.OrganizerFragments.ParticipantListFragments.WaitingListFragment;

public class EventParticipantsViewPagerAdapter extends FragmentStateAdapter {

    public EventParticipantsViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WaitingListFragment();
            case 1:
                return new SelectedListFragment();
            case 2:
                return new FinalListFragment();
            default:
                return new CancelledListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
