package ca.yapper.yapperapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.R;
import ca.yapper.yapperapp.UMLClasses.Notification;
/**
 * An adapter class for displaying a list of notifications in a {@link RecyclerView}.
 * This adapter binds the {@link Notification} data to each item view in the list.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private Context context;
    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entrant_notifications_item, parent, false);
        return new NotificationViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.eventName.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());
        // Format and set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.date.setText(dateFormat.format(notification.getDateTimeStamp()));
        // Handle Selection notifications
        if (notification.getNotificationType() != null &&
                notification.getNotificationType().equals("Selection")) {
            holder.selectionButtons.setVisibility(View.VISIBLE);

            // Accept Button Logic
            holder.acceptButton.setOnClickListener(v -> {
                String eventId = notification.getEventId(); // Ensure Notification has eventId
                String userId = notification.getUserToId(); // Ensure Notification has userToId

                if (eventId == null || eventId.isEmpty()) {
                    Toast.makeText(context, "Error: Missing Event ID.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(context, "Error: Missing User ID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                OrganizerDatabase.addUserToFinalList(eventId, userId, success -> {
                    if (success) {
                        // Mark the notification as read and remove it from the list
                        notification.markAsRead();
                        notificationList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Accepted and added to Final List", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error adding to Final List. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            // Reject Button Logic
            holder.rejectButton.setOnClickListener(v -> {
                String eventId = notification.getEventId(); // Ensure Notification has eventId
                String userId = notification.getUserToId(); // Ensure Notification has userToId

                if (eventId == null || eventId.isEmpty()) {
                    Toast.makeText(context, "Error: Missing Event ID.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(context, "Error: Missing User ID.", Toast.LENGTH_SHORT).show();
                    return;
                }

                OrganizerDatabase.addUserToCancelledList(eventId, userId, success -> {
                    if (success) {
                        // Mark the notification as read and remove it from the list
                        notification.markAsRead();
                        notificationList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Rejected and added to Cancelled List", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error adding to Cancelled List. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            });

        } else {
            holder.selectionButtons.setVisibility(View.GONE);
        }
        // Dim if read
        holder.itemView.setAlpha(notification.isRead() ? 0.6f : 1.0f);
    }


    @Override
    public int getItemCount() {
        return notificationList == null ? 0 : notificationList.size();
    }
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, message, date;
        LinearLayout selectionButtons;
        ImageView acceptButton, rejectButton;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            message = itemView.findViewById(R.id.notification_message);
            date = itemView.findViewById(R.id.notification_date);
            selectionButtons = itemView.findViewById(R.id.selection_buttons);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}