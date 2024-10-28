package ca.yapper.yapperapp;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class EntrantActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference entrantsRef;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.entrant_activity_layout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        createFirebaseConnection();

        // Get device ID (assuming this method retrieves the device's unique ID)
        // CHECK
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Check if user is already in the database
        checkUserInDatabase();
        // Set up bottom nav.
        setupBottomNavigation();
    }

    private void createFirebaseConnection() {
        db = FirebaseFirestore.getInstance();
        entrantsRef = db.collection("entrants");
    }
    // Getter for Firebase reference
    public void checkUserInDatabase() {
        entrantsRef.document(deviceId).get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                // If the user exists, go to EntrantHomeFragment
                navigateToHome();
            } else {
                // If the user does not exist, show SignUpFragment
                navigateToSignUp();
            }
        } else {
            Log.d("Firestore", "Failed to retrieve document: ", task.getException());
        }
        });
    }

    public void navigateToHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new EntrantHomeFragment())
                .commit();
    }

    public void navigateToSignUp() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SignupFragment())
                .commit();
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

    /** private void addNewEntrant(User entrant) {
        // Add the city to the Firestore collection with the city name as the document Id
        HashMap<String, Object> data = new HashMap<>();
        // add required fields
        data.put("Name", entrant.getName());
        data.put("Device Id", entrant.getDeviceId());
        // add optional fields (if not null)
        if (entrant.getEmail() != null) data.put("email", entrant.getEmail());
        if (entrant.getPhoneNum() != null) data.put("phoneNum", entrant.getPhoneNum());
        if (entrant.getProfilePic() != null) {
            // Assuming ProfilePic has a method to convert it to a storable format (e.g., as a URL or encoded string)
            data.put("profilePic", entrant.getProfilePic().toString());
        }
        if (entrant.getGeneratedProfilePic() != null) {
            data.put("generatedProfilePic", entrant.getGeneratedProfilePic().toString());
        }
        // Add the data to the Firestore collection with the entrant's name as the document ID
        entrantsRef.document(entrant.getName()).set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Entrant added successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding entrant", e));
    } **/
} }