package ca.yapper.yapperapp.AdminFragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminEventListFragment;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminImageListFragment;
import ca.yapper.yapperapp.AdminFragments.SearchFragments.AdminProfileListFragment;

/**
 * Fragment for admin search features/functionality
 */
public class AdminSearchViewPagerAdapter extends FragmentStateAdapter {
    private final String[] tabTitles = new String[]{"Events", "Profiles", "Images"};
    private int currentPosition = 0;

    /**
     *
     * @param fragment
     */
    public AdminSearchViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    /**
     *
     * @param position
     * @return
     */
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

    /**
     * Obtains number of entries in the tab
     *
     * @return size of tab
     */
    @Override
    public int getItemCount() {return tabTitles.length;}

    /**
     * Returns ID for item at the given position
     *
     * @param position Adapter position
     * @return integer position of the item in the adapter, which is also the id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Checks if an item is in the adapter
     *
     * @param itemId id of the entry in the list
     * @return
     */
    @Override
    public boolean containsItem(long itemId) {
        return itemId < tabTitles.length;
    }

    /**
     * Returns the names of each admin search tab
     *
     * @return string array of tab titles
     */
    public String[] getTabTitles() {return tabTitles;}
}