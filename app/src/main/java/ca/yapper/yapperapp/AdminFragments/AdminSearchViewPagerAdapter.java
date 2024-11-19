package ca.yapper.yapperapp.AdminFragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminEventListFragment;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminImageListFragment;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminProfileListFragment;

public class AdminSearchViewPagerAdapter extends FragmentStateAdapter {
    private final String[] tabTitles = new String[]{"Events", "Profiles", "Images"};

    public AdminSearchViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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

    public String[] getTabTitles() {
        return tabTitles;
    }
}