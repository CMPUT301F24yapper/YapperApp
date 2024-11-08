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

public class SignupActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private String deviceId;
    private EditText addEntrantNameEditText;
    private EditText addEntrantPhoneEditText;
    private EditText addEntrantEmailEditText;
    private Button signupButton;
    private static final String channel_Id = "event_notifications";
    private static final String channel_Name = "event_notifications";
    private static final String channel_desc = "Notifications related to event participation";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_page_fragment);

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
                            displayWelcomeNotification();
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



    private void displayWelcomeNotification() {
        // Display a local notification for a welcome message
        Notification welcomeNotification = new Notification(new Date(), "Welcome back!",
                "Hello, welcome to the Event Lottery app.", "Welcome");
        Log.d("SignupActivity", "Attempting to display welcome notification");
        welcomeNotification.displayNotification(this);
    }

    private void setUpSignUpViews() {
        addEntrantNameEditText = findViewById(R.id.name_input);
        addEntrantPhoneEditText = findViewById(R.id.phone_input);
        addEntrantEmailEditText = findViewById(R.id.email_input);
        signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(v -> createUserAndNotify());
    }



    private void createUserAndNotify() {
        String entrantName = addEntrantNameEditText.getText().toString();
        String entrantPhone = addEntrantPhoneEditText.getText().toString();
        String entrantEmail = addEntrantEmailEditText.getText().toString();

        if (entrantName.isEmpty() || entrantPhone.isEmpty() || entrantEmail.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

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
        signupNotification.displayNotification(this);

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
