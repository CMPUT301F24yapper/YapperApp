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
import com.google.firebase.messaging.FirebaseMessaging;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
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
    private String fcmToken; // Variable to store the FCM token
    private static final String CHANNEL_ID = "your_channel_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_page_fragment);

        FirebaseApp.initializeApp(this);
        createFirebaseConnection();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        // Step 1: Create Notification Channel (for Android 8.0+)
        createNotificationChannel();


        // Step 2: Retrieve the FCM token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fcmToken = task.getResult();
                Log.d("FCM Token", "Token: " + fcmToken);
            } else {
                Log.w("SignupActivity", "Fetching FCM token failed", task.getException());
            }
        });

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

        // Pass the FCM token to the User creation method
        User.createUserInDatabase(
                deviceId,
                entrantEmail,
                false,  // isAdmin
                true,   // isEntrant
                false,  // isOrganizer
                entrantName,
                entrantPhone,
                false,  // isOptedOut
                fcmToken // Pass the retrieved FCM token here
        );

        launchEntrantActivity();
    }

    private void launchEntrantActivity() {
        Intent intent = new Intent(SignupActivity.this, EntrantActivity.class);
        startActivity(intent);
        finish();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void launchOrganizerActivity() {
        Intent intent = new Intent(SignupActivity.this, OrganizerActivity.class);
        startActivity(intent);
        finish();
    }
}
