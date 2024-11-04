package ca.yapper.yapperapp.Activities;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.content.Intent;
import android.os.Bundle;
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

import ca.yapper.yapperapp.EntrantFragments.EntrantHomeFragment;
import ca.yapper.yapperapp.EntrantFragments.EntrantNotificationsFragment;
import ca.yapper.yapperapp.EntrantFragments.EntrantQRCodeScannerFragment;
import ca.yapper.yapperapp.ProfileFragment;
import ca.yapper.yapperapp.R;

public class EntrantActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.entrant_activity_layout);


        this.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        setupBottomNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EntrantHomeFragment())
                    .commit();

            // Also set the home item as selected in bottom nav
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setSelectedItemId(R.id.nav_entrant_home);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up normal click listener
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

    private void showProfileSwitchMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_popup_menu, popup.getMenu());

        // Hide the current role option
        popup.getMenu().findItem(R.id.switch_to_entrant).setVisible(false);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.switch_to_organizer) {
                // Switch to OrganizerActivity
                Intent intent = new Intent(EntrantActivity.this, OrganizerActivity.class);
                startActivity(intent);
                finish(); // Close current activity
                return true;
            }
            return false;
        });

        popup.show();
    }
}