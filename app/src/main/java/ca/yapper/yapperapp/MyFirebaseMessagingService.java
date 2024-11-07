package ca.yapper.yapperapp;

import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ca.yapper.yapperapp.NotificationHelper;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingSvc";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if the message contains notification data
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Display the notification using NotificationHelper
            NotificationHelper.displayNotification(getApplicationContext(), title, body);
        }

        // Handle data payload if necessary
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // Handle additional data here if needed
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // You may want to update the token in Firestore if it changes
        updateTokenInFirestore(token);
    }

    private void updateTokenInFirestore(String token) {
        // Assuming deviceId is globally accessible or passed here
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(deviceId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Token updated successfully"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating token", e));
    }
}
