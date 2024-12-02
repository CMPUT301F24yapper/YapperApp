package ca.yapper.yapperapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ca.yapper.yapperapp.EntrantFragments.EventListFragments.JoinedEventsFragment;
import ca.yapper.yapperapp.EntrantFragments.EventListFragments.MissedOutFragment;
import ca.yapper.yapperapp.EntrantFragments.EventListFragments.RegisteredEventsFragment;

/**
 * A {@link FragmentStateAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 * */
public class ViewPagerAdapter extends FragmentStateAdapter {


    /**
     * Constructor for ViewPagerAdapter
     *
     * @param fragment the parent fragment
     */
    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }


    /**
     * Creates a fragment for the given position.
     *
     * @param position the position of the fragment to be created
     * @return the created fragment
     */
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


    /**
     * Returns the number of fragments.
     *
     * @return the number of fragments
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}
