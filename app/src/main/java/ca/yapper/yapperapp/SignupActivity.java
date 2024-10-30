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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
// is below import necessary?
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
        checkUserInDatabase();
    }

    private void createFirebaseConnection() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("entrants");
    }

    // Check if the device ID is already in the database
    private void checkUserInDatabase() {
        usersRef.document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // If the user exists, go to EntrantActivity
                            navigateToEntrantActivity();
                        } else {
                            // If the user does not exist, set up views for signing up
                            setUpSignUpViews();
                        }
                    } else {
                        Log.d("Firestore", "Failed to retrieve document: ", task.getException());
                        Toast.makeText(this, "Error connecting to database. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToEntrantActivity() {
        Intent intent = new Intent(SignupActivity.this, EntrantActivity.class);
        startActivity(intent);
        finish(); // Close SignupActivity
    }

    private void setUpSignUpViews() {
        // Set up views only if the user is not already in the database
        addEntrantNameEditText = findViewById(R.id.name_input);
        addEntrantPhoneEditText = findViewById(R.id.phone_input);
        addEntrantEmailEditText = findViewById(R.id.email_input);
        signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(v -> saveUserToFirestore());
    }

    private void saveUserToFirestore() {
        String name = addEntrantNameEditText.getText().toString();
        String phone = addEntrantPhoneEditText.getText().toString();
        String email = addEntrantEmailEditText.getText().toString();

        /** if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
         Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
         return;
         } **/
        /** if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in your name to sign-up", Toast.LENGTH_SHORT).show();
            return;
        } **/
        // Create a User object with the provided data
        User newUser = new User();
        newUser.setName(name);
        newUser.setDeviceId(deviceId); // Assuming deviceId is available as a class variable
        newUser.setPhoneNum(phone);
        newUser.setEmail(email);

        usersRef.document(deviceId).set(newUser);
                /** .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "User registered successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to home screen after registration
                    ((EntrantActivity) getActivity()).navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding user", e);
                    Toast.makeText(getActivity(), "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                }); **/
    }
}
