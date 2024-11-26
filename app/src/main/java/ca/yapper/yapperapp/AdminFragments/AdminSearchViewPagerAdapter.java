package ca.yapper.yapperapp.AdminFragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminEventListFragment;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminImageListFragment;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminProfileListFragment;

public class AdminSearchViewPagerAdapter extends FragmentStateAdapter {
    private final String[] tabTitles = new String[]{"Events", "Profiles", "Images"};
    private int currentPosition = 0;

    public AdminSearchViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        currentPosition = position;
        switch (position) {
            case 0:
                return new AdminEventListFragment();
            case 1:
                return new AdminProfileListFragment();
            case 2:
                return new AdminImageListFragment();
            default:
                return new AdminEventListFragment();
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId < tabTitles.length;
    }

    public String[] getTabTitles() {
        return tabTitles;
    }
}