package ca.yapper.yapperapp;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
// is below import necessary?
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.UMLClasses.User;

import ca.yapper.yapperapp.UMLClasses.User;

import ca.yapper.yapperapp.UMLClasses.User;

public class SignupActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private String deviceId;
    private EditText addEntrantNameEditText;
    private EditText addEntrantPhoneEditText;
    private EditText addEntrantEmailEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_page_fragment);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        createFirebaseConnection();

        // Get device ID (assuming this method retrieves the device's unique ID)
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Check if user is already in the database

        checkUserExists();
    }

    private void createFirebaseConnection() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("Users");
    }

    // Check if the device ID is already in the database
    private void checkUserExists() {
        db.collection("Users").document(deviceId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                launchEntrantActivity();
                            }
                            else {
                                setUpSignUpViews();
                            }
                        } else {
                            Log.w("SignupActivity", "Error checking user existence", task.getException());
                            Toast.makeText(SignupActivity.this, "Error connecting to database.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setUpSignUpViews() {
        // Set up views only if the user is not already in the database
        addEntrantNameEditText = findViewById(R.id.name_input);
        addEntrantPhoneEditText = findViewById(R.id.phone_input);
        addEntrantEmailEditText = findViewById(R.id.email_input);
        signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(v -> createUserInFirestore());
    }

    private void createUserInFirestore() {
        String entrantName = addEntrantNameEditText.getText().toString();
        String entrantPhone = addEntrantPhoneEditText.getText().toString();
        String entrantEmail = addEntrantEmailEditText.getText().toString();

        Map<String, Object> user = new HashMap<>();
        user.put("deviceId", deviceId);
        user.put("entrantName", entrantName);
        user.put("entrantPhone", entrantPhone);
        user.put("entrantEmail", entrantEmail); // Replace with actual user details

        db.collection("Users").document(deviceId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // User added successfully, now redirect to EntrantActivity
                            launchEntrantActivity();
                        } else {
                            Log.w("SignupActivity", "Error adding user", task.getException());
                            Toast.makeText(SignupActivity.this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void launchEntrantActivity() {
        Intent intent = new Intent(SignupActivity.this, EntrantActivity.class);
        startActivity(intent);
        finish(); // Close SignupActivity
    }

    private void launchOrganizerActivity() {
        Intent intent = new Intent(SignupActivity.this, OrganizerActivity.class);
        startActivity(intent);
        finish(); // Close SignupActivity
    }
}