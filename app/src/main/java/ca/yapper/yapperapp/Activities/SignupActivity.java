package ca.yapper.yapperapp.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
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

import java.util.Date;

import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;
import ca.yapper.yapperapp.UMLClasses.User;
import ca.yapper.yapperapp.Databases.SignUpDatabase;
/**
 * SignupActivity is responsible for user registration and Firebase Firestore setup.
 * It initializes Firebase, checks for existing users, and facilitates new user sign-up
 * with notifications and redirection based on user role.
 */
public class SignupActivity extends AppCompatActivity {

    // private FirebaseFirestore db;
    // private CollectionReference usersRef;
    private String deviceId;
    private EditText addEntrantNameEditText;
    private EditText addEntrantPhoneEditText;
    private EditText addEntrantEmailEditText;
    private Button signupButton;
    private static final String channel_Id = "event_notifications";
    private static final String channel_Name = "event_notifications";
    private static final String channel_desc = "Notifications related to event participation";


    /**
     * Initializes the activity, sets up notifications for user roles, and checks if the user
     * already exists in Firestore.
     *
     * @param savedInstanceState The state saved in a previous configuration, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_page_fragment);
        // to do: could move this notification logic into its own method (e.g. "setUpNotificationChannel")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channel_Id, channel_Name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(channel_desc);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("SignupActivity", "Notification channel created");
            } else {
                Log.e("SignupActivity", "NotificationManager is null, channel creation failed");
            }
        }

        // FirebaseApp.initializeApp(this);
        // createFirebaseConnection();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        checkUserExists();
    }
    /**
     * Establishes a Firebase connection and initializes the reference to the "Users" collection.
     */
    /** private void createFirebaseConnection() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("Users");
    } **/

    /**
     * Checks if a user with the current deviceId exists in Firestore.
     * If the user exists, it displays a welcome notification and launches EntrantActivity.
     * Otherwise, it sets up the sign-up form.
     */
    private void checkUserExists() {
        SignUpDatabase.checkUserExists(deviceId)
            .addOnSuccessListener(exists -> {
                if (exists) {
                    launchEntrantActivity();
                } else {
                    setUpSignUpViews();
                }
            })
            .addOnFailureListener(e -> {
                Log.w("SignupActivity", "Error checking user existence", e);
                Toast.makeText(this, "Error connecting to database.", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Initializes sign-up form views for new users and sets up the listener for the sign-up button.
     */
    private void setUpSignUpViews() {
        addEntrantNameEditText = findViewById(R.id.name_input);
        addEntrantPhoneEditText = findViewById(R.id.phone_input);
        addEntrantEmailEditText = findViewById(R.id.email_input);
        signupButton = findViewById(R.id.signup_button);

        signupButton.setOnClickListener(v -> createUserAndNotify());
    }


    /**
     * Registers a new user in Firestore, then displays a sign-up success notification and
     * launches EntrantActivity.
     */
    private void createUserAndNotify() {
        String entrantName = addEntrantNameEditText.getText().toString();
        String entrantPhone = addEntrantPhoneEditText.getText().toString();
        String entrantEmail = addEntrantEmailEditText.getText().toString();

        if (entrantName.isEmpty() || entrantPhone.isEmpty() || entrantEmail.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        SignUpDatabase.createUser(deviceId, entrantName, entrantPhone, entrantEmail)
                .addOnSuccessListener(aVoid -> {
                    showSignupSuccessNotification(entrantName);
                    launchEntrantActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e("SignupActivity", "Error creating user", e);
                    Toast.makeText(this, "Error signing up. Please try again.", Toast.LENGTH_SHORT).show();
                });
        /**
        User.createUserInDatabase(
                deviceId,
                entrantEmail,
                false,   // isAdmin
                true,           // isEntrant
                false,         // isOrganizer
                entrantName,
                entrantPhone,
                false        // isOptedOut
        );

        Notification signupNotification = new Notification(
                new Date(),
                "Welcome to Event Lottery!",
                "Hello " + entrantName + ", you have successfully signed up.",
                "Signup Success"
        );
        Log.d("SignupActivity", "Attempting to display signup notification");

        launchEntrantActivity(); **/
    }

    /**
     * Displays a signup success notification.
     */
    private void showSignupSuccessNotification(String entrantName) {
        Notification signupNotification = new Notification(
                new Date(),
                "Welcome to Event Lottery!",
                "Hello " + entrantName + ", you have successfully signed up.",
                "Signup Success"
        );
        Log.d("SignupActivity", "Signup success notification displayed.");
    }

    /**
     * Launches EntrantActivity, completing the sign-up process for Entrants.
     */
    private void launchEntrantActivity() {
        Intent intent = new Intent(SignupActivity.this, EntrantActivity.class);
        startActivity(intent);
        finish();
    }
    /**
     * Launches OrganizerActivity if the user role is changed or set as Organizer.
     */
    private void launchOrganizerActivity() {
        Intent intent = new Intent(SignupActivity.this, OrganizerActivity.class);
        startActivity(intent);
        finish();
    }
}
