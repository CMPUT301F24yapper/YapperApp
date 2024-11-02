package ca.yapper.yapperapp.Activities;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

import android.os.Bundle;
import android.view.MenuItem;

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.entrant_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                // Default fragment
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            }
        });
} }