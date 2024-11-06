package ca.yapper.yapperapp.OrganizerFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import ca.yapper.yapperapp.EventParticipantsViewPagerAdapter;
import ca.yapper.yapperapp.R;

public class ViewParticipantsFragment extends Fragment {
    private String eventId;
    private ViewPager2 viewPager;
    private EventParticipantsViewPagerAdapter viewPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_participants_viewtab, container, false);

        // Retrieve eventId from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        // Initialize ViewPager and Adapter
        viewPager = view.findViewById(R.id.viewPager);
        viewPagerAdapter = new EventParticipantsViewPagerAdapter(this, eventId);
        viewPager.setAdapter(viewPagerAdapter);

        return view;
    }
}
