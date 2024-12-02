package ca.yapper.yapperapp;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
        createNotificationChannel();
    }

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

    public void startListening() {
        NotificationsDatabase.startNotificationListener(deviceId, (title, message) -> {
            showNotification(title != null ? title : "YapperApp", message);
        });
    }

    public void stopListening() {
        NotificationsDatabase.stopNotificationListener();
    }

    @SuppressLint("MissingPermission")
    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    public interface NotificationCallback {
        void onNotification(String title, String message);
    }
}