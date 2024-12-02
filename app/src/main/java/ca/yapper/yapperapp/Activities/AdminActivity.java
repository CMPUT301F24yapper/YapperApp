package ca.yapper.yapperapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import ca.yapper.yapperapp.AdminFragments.AdminHomeFragment;
import ca.yapper.yapperapp.AdminFragments.AdminSearchFragment;
import ca.yapper.yapperapp.Adapters.NotificationListener;
import ca.yapper.yapperapp.EntrantFragments.ProfileFragment;
import ca.yapper.yapperapp.R;

/**
 * This class represents the parent activity page used for admins, which holds all the
 * functionality for switching between admin fragments. It holds functions to show the admins nav
 * bar and also the ability to switch between admins and other roles.
 * */
public class AdminActivity extends AppCompatActivity {


    /**
     * Initial setup for admin activity
     *
     * @param savedInstanceState bundle, containing data from another fragment/activity
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationListener notificationListener = new NotificationListener(this);
        notificationListener.startListening();
        Log.d("MainActivity", "NotificationListener initialized and started");
        setContentView(R.layout.admin_activity_layout);
        setupBottomNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AdminHomeFragment())
                    .commit();
        }
    }


    /**
     * This function sets up the fragment switching functionality of the bottom navigation bar.
     * */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_admin_home) {
                selectedFragment = new AdminHomeFragment();
            } else if (item.getItemId() == R.id.nav_admin_search) {
                selectedFragment = new AdminSearchFragment();
            } else if (item.getItemId() == R.id.nav_admin_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set up long click listener for profile item
        View profileItem = bottomNavigationView.findViewById(R.id.nav_admin_profile);
        profileItem.setOnLongClickListener(v -> {
            showProfileSwitchMenu(v);
            return true;
        });
    }


    /**
     * Function for displaying a popup menu that displays different roles and sets the
     * functionality for each button press so users can switch activities using intents.
     *
     * @param view The UI element that will be used for the popup menu
     */
    public void showProfileSwitchMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_popup_menu, popup.getMenu());

        popup.getMenu().findItem(R.id.switch_to_admin).setVisible(false);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.switch_to_entrant) {
                startActivity(new Intent(this, EntrantActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.switch_to_organizer) {
                startActivity(new Intent(this, OrganizerActivity.class));
                finish();
                return true;
            }
            return false;
        });

        popup.show();
    }


}