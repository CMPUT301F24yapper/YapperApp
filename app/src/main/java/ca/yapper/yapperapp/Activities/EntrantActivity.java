package ca.yapper.yapperapp.Activities;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.EntrantFragments.EntrantHomeFragment;
import ca.yapper.yapperapp.EntrantFragments.EntrantNotificationsFragment;
import ca.yapper.yapperapp.EntrantFragments.EntrantQRCodeScannerFragment;
import ca.yapper.yapperapp.Adapters.NotificationListener;
import ca.yapper.yapperapp.EntrantFragments.ProfileFragment;
import ca.yapper.yapperapp.R;
/**
 * EntrantActivity is the main activity for the Entrant user role.
 * This activity manages the Entrant's home, QR scanner, notifications, and profile views,
 * allowing navigation between these sections via a BottomNavigationView.
 * Additionally, it includes options for switching roles to Organizer.
 */
public class EntrantActivity extends AppCompatActivity {

    //private FirebaseFirestore db;
    //private CollectionReference usersRef;
    private String deviceId;


    /**
     * Initializes the activity, setting up the layout and the BottomNavigationView for navigating
     * between different sections: Home, QR Scanner, Notifications, and Profile.
     * If the activity is created without a saved instance state, it loads the home fragment by default.
     *
     * @param savedInstanceState The state saved in a previous configuration, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationListener notificationListener = new NotificationListener(this);
        notificationListener.startListening();
        Log.d("MainActivity", "NotificationListener initialized and started");
        setContentView(R.layout.entrant_activity_layout);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        this.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        setupBottomNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EntrantHomeFragment())
                    .commit();

            // Setting the home item as selected in bottom nav
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.nav_entrant_home);
        }
    }


    /**
     * Sets up the BottomNavigationView for EntrantActivity.
     * Loads the appropriate Fragment based on the selected item.
     * Sets up a long-click listener on the profile item for role-switch options.
     */
    private void setupBottomNavigation() {

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_entrant_home) {
                selectedFragment = new EntrantHomeFragment();
            } else if (item.getItemId() == R.id.nav_entrant_qrscanner) {
                selectedFragment = new EntrantQRCodeScannerFragment();
            } else if (item.getItemId() == R.id.nav_entrant_notifications) {
                selectedFragment = new EntrantNotificationsFragment();
            } else if (item.getItemId() == R.id.nav_entrant_profile) {
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
        View profileItem = bottomNavigationView.findViewById(R.id.nav_entrant_profile);
        profileItem.setOnLongClickListener(v -> {
            showProfileSwitchMenu(v);
            return true;
        });
    }


    /**
     * Displays a menu with options to switch to the Organizer role, allowing the user to open
     * OrganizerActivity and exit EntrantActivity.
     *
     * @param view The view from which this menu is triggered.
     */
    private void showProfileSwitchMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_popup_menu, popup.getMenu());

        // Hide current role option
        popup.getMenu().findItem(R.id.switch_to_entrant).setVisible(false);

        UserDatabase.checkIfUserIsAdmin(deviceId)
                .addOnSuccessListener(isAdmin -> {
                    popup.getMenu().findItem(R.id.switch_to_admin).setVisible(isAdmin);
                })
                .addOnFailureListener(e -> {
                    popup.getMenu().findItem(R.id.switch_to_admin).setVisible(false);
                });

        /**
        // Check if user is admin before showing admin option
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore.getInstance().collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    Boolean isAdmin = document.getBoolean("Admin");
                    popup.getMenu().findItem(R.id.switch_to_admin).setVisible(isAdmin != null && isAdmin);
                }); **/

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.switch_to_organizer) {
                startActivity(new Intent(this, OrganizerActivity.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.switch_to_admin) {
                startActivity(new Intent(this, AdminActivity.class));
                finish();
                return true;
            }
            return false;
        });

        popup.show();
    }
}
