package ca.yapper.yapperapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ca.yapper.yapperapp.EntrantFragments.EventListFragments.JoinedEventsFragment;
import ca.yapper.yapperapp.EntrantFragments.EventListFragments.MissedOutFragment;
import ca.yapper.yapperapp.EntrantFragments.EventListFragments.RegisteredEventsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RegisteredEventsFragment();
            case 1:
                return new JoinedEventsFragment();
            case 2:
                return new MissedOutFragment();
            default:
                return new RegisteredEventsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
