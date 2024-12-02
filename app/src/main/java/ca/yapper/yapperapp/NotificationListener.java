package ca.yapper.yapperapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import ca.yapper.yapperapp.Databases.NotificationsDatabase;

public class NotificationListener {
    private static final String TAG = "NotificationListener";
    private static final String CHANNEL_ID = "yapper_notifications";
    private static final String CHANNEL_NAME = "YapperApp Notifications";
    private final Context context;
    private final String deviceId;

    public NotificationListener(Context context) {
        this.context = context;
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "NotificationListener initialized with deviceId: " + deviceId);
        createNotificationChannel();
    }

    public void startListening() {
        Log.d(TAG, "Starting to listen for notifications");
        NotificationsDatabase.startNotificationListener(deviceId, (title, message) -> {
            Log.d(TAG, "New notification received - Title: " + title + ", Message: " + message);
            sendNotification(title != null ? title : "YapperApp", message);
        });
    }

    public void stopListening() {
        Log.d(TAG, "Stopping notification listener");
        NotificationsDatabase.stopNotificationListener();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            channel.setShowBadge(true);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    private void sendNotification(String title, String message) {
        Log.d(TAG, "Attempting to send notification - Title: " + title + ", Message: " + message);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Using system icon temporarily
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (int) System.currentTimeMillis();
        Log.d(TAG, "Sending notification with ID: " + notificationId);
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "Notification sent");
    }

    public interface NotificationCallback {
        void onNotification(String title, String message);
    }
}