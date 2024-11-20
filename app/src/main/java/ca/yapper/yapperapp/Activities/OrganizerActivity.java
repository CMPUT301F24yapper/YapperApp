package ca.yapper.yapperapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.OrganizerFragments.OrganizerCreateEventFragment;
import ca.yapper.yapperapp.OrganizerFragments.OrganizerHomeFragment;
import ca.yapper.yapperapp.ProfileFragment;
import ca.yapper.yapperapp.R;
/**
 * OrganizerActivity is the main activity for the Organizer user role.
 * This activity manages the Organizer's home, event creation, and profile views,
 * allowing navigation between these sections via a BottomNavigationView.
 * Additionally, it includes options for switching roles to Entrant.
 */
public class OrganizerActivity extends AppCompatActivity {

    // private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // private CollectionReference eventsRef = db.collection("Events");
    private String deviceId;

    /**
     * Initializes the activity, setting up the layout and enabling Edge-to-Edge for UI.
     * Sets up BottomNavigationView for navigating between sections: Home, Create Event, and Profile.
     *
     * @param savedInstanceState The state saved in a previous configuration, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.organizer_activity_layout);

        setupBottomNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new OrganizerHomeFragment())
                    .commit();
        }
    }

    /**
     * Sets up the BottomNavigationView for OrganizerActivity.
     * Loads the appropriate Fragment based on the selected item.
     * Sets up a long-click listener on the profile item for role-switch options.
     */
    private void setupBottomNavigation() {
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_organizer_home) {
                selectedFragment = new OrganizerHomeFragment();
            } else if (item.getItemId() == R.id.nav_organizer_createevent) {
                selectedFragment = new OrganizerCreateEventFragment();
            } else if (item.getItemId() == R.id.nav_organizer_profile) {
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
        View profileItem = bottomNavigationView.findViewById(R.id.nav_organizer_profile);
        profileItem.setOnLongClickListener(v -> {
            showProfileSwitchMenu(v);
            return true;
        });
    }


    /**
     * Displays a menu with options to switch to the Entrant role, allowing the user to open
     * EntrantActivity and exit OrganizerActivity.
     *
     * @param view The view from which this menu is triggered.
     */
    private void showProfileSwitchMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_popup_menu, popup.getMenu());

        popup.getMenu().findItem(R.id.switch_to_organizer).setVisible(false);

        OrganizerDatabase.checkIfUserIsAdmin(deviceId)
                .addOnSuccessListener(isAdmin -> {
                    popup.getMenu().findItem(R.id.switch_to_admin).setVisible(isAdmin);
                })
                .addOnFailureListener(e -> {
                    popup.getMenu().findItem(R.id.switch_to_admin).setVisible(false);
                });

        /**
        //String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore.getInstance().collection("Users").document(deviceId).get()
                .addOnSuccessListener(document -> {
                    Boolean isAdmin = document.getBoolean("Admin");
                    popup.getMenu().findItem(R.id.switch_to_admin).setVisible(isAdmin != null && isAdmin);
                }); **/

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.switch_to_entrant) {
                startActivity(new Intent(this, EntrantActivity.class));
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