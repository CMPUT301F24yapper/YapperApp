package ca.yapper.yapperapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;
/**
 * An adapter class for displaying a list of notifications in a {@link RecyclerView}.
 * This adapter binds the {@link Notification} data to each item view in the list.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    /**
     * Constructs a new {@code NotificationAdapter} with the specified list of notifications.
     *
     * @param notificationList The list of {@link Notification} objects to be displayed in the {@code RecyclerView}.
     */
    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }
    /**
     * Creates a new {@link NotificationViewHolder} for the specified {@code ViewGroup}.
     *
     * @param parent   The {@link ViewGroup} into which the new item view will be added.
     * @param viewType The type of view to create (not used in this implementation).
     * @return A new instance of {@link NotificationViewHolder} bound to a new view.
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_notifications_item, parent, false);
        return new NotificationViewHolder(itemView);
    }
    /**
     * Binds the notification data to the provided {@link NotificationViewHolder}.
     *
     * @param holder   The {@link NotificationViewHolder} to bind data to.
     * @param position The position of the item in the {@code notificationList}.
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.eventName.setText(notification.getTitle()); // Set the title of the notification
        holder.notificationStatus.setText(notification.getMessage()); // Set the message

        // Optionally, you can customize the display based on the read status
        if (notification.isRead()) {
            holder.itemView.setAlpha(0.6f); // Dim the item if read
        } else {
            holder.itemView.setAlpha(1.0f); // Full brightness if unread
        }
    }
    /**
     * Returns the total number of notifications in the list.
     *
     * @return The size of the {@code notificationList}.
     */
    @Override
    public int getItemCount() {
        return notificationList.size();
    }
    /**
     * A {@link RecyclerView.ViewHolder} that holds the views for each notification item.
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        public TextView eventName, notificationStatus;
        /**
         * Constructs a new {@code NotificationViewHolder} with the given item view.
         *
         * @param itemView The item view that will be used for each notification.
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            notificationStatus = itemView.findViewById(R.id.notification_status);
        }
    }
}
