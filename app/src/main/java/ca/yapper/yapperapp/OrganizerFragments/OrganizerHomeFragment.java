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

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.ViewPagerAdapter;

public class OrganizerHomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entrant_homepage, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        // Set up the adapter for ViewPager
        viewPager.setAdapter(new ViewPagerAdapter(this));

        // Attach TabLayout and ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Waiting List");
                            break;
                        case 1:
                            tab.setText("Selected List");
                            break;
                        case 2:
                            tab.setText("Final List");
                            break;
                        case 3:
                            tab.setText("Cancelled List");
                            break;
                    }
                }).attach();

        return view;
    }
}
