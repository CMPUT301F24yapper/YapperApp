package ca.yapper.yapperapp.Adapters;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ca.yapper.yapperapp.Databases.NotificationsDatabase;
import ca.yapper.yapperapp.R;

/**
 * This is a class for notification listeners
 */
public class NotificationListener {
    private static final String TAG = "NotificationListener";
    private static final String CHANNEL_ID = "yapper_notifications";
    private static final String CHANNEL_NAME = "YapperApp Notifications";
    private final Context context;
    private final String deviceId;

    /**
     * The constructor for notification listeners
     *
     * @param context the environmental data from the phone
     */
    public NotificationListener(Context context) {
        this.context = context;
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        createNotificationChannel();
    }

    /**
     * This function creates a notification channel, for displaying notifications
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * This function starts a notification listener for a user
     */
    public void startListening() {
        NotificationsDatabase.startNotificationListener(deviceId, (title, message) -> {
            showNotification(title != null ? title : "YapperApp", message);
        });
    }

    /**
     * This function displays a notification
     *
     * @param title The title for a notification
     * @param message The message for a notification
     */
    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message) {
        NotificationsDatabase.areNotificationsEnabled(deviceId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && Boolean.TRUE.equals(task.getResult())) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.baseline_notifications_24)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, builder.build());
            }
        });
    }

    public interface NotificationCallback {
        void onNotification(String title, String message);
    }
}