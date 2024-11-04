package ca.yapper.yapperapp.Activities;

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

import ca.yapper.yapperapp.OrganizerFragments.OrganizerCreateEventFragment;
import ca.yapper.yapperapp.OrganizerFragments.OrganizerHomeFragment;
import ca.yapper.yapperapp.ProfileFragment;
import ca.yapper.yapperapp.R;

public class OrganizerActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventsRef = db.collection("Events");

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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up normal click listener
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

    private void showProfileSwitchMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.profile_popup_menu, popup.getMenu());

        // Hide the current role option
        popup.getMenu().findItem(R.id.switch_to_organizer).setVisible(false);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.switch_to_entrant) {
                // Switch to EntrantActivity
                Intent intent = new Intent(OrganizerActivity.this, EntrantActivity.class);
                startActivity(intent);
                finish(); // Close current activity
                return true;
            }
            return false;
        });

        popup.show();
    }
}