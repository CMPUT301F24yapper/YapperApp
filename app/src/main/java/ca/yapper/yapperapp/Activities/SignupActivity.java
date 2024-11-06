package ca.yapper.yapperapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import ca.yapper.yapperapp.R;
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

        FirebaseApp.initializeApp(this);
        createFirebaseConnection();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        checkUserExists();
    }

    private void createFirebaseConnection() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("Users");
    }

    private void checkUserExists() {
        db.collection("Users").document(deviceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            launchEntrantActivity();
                        } else {
                            setUpSignUpViews();
                        }
                    } else {
                        Log.w("SignupActivity", "Error checking user existence", task.getException());
                        Toast.makeText(SignupActivity.this, "Error connecting to database.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpSignUpViews() {
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

        User.createUserInDatabase(
                deviceId,
                entrantEmail,
                false,  // isAdmin
                true,   // isEntrant
                false,  // isOrganizer
                entrantName,
                entrantPhone,
                false   // isOptedOut
        );

        launchEntrantActivity();
    }

    private void launchEntrantActivity() {
        Intent intent = new Intent(SignupActivity.this, EntrantActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchOrganizerActivity() {
        Intent intent = new Intent(SignupActivity.this, OrganizerActivity.class);
        startActivity(intent);
        finish();
    }
}