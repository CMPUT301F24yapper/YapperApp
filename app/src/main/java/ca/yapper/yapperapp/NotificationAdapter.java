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

            holder.acceptButton.setOnClickListener(v -> {
                // Mark as read in Firebase
                notification.markAsRead();

                // Optionally perform additional logic for accepting
                Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show();

                // Remove from list and notify adapter
                notificationList.remove(position);
                notifyItemRemoved(position);
            });

            holder.rejectButton.setOnClickListener(v -> {
                // Mark as read in Firebase
                notification.markAsRead();

                // Optionally perform additional logic for rejecting
                Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show();

                // Remove from list and notify adapter
                notificationList.remove(position);
                notifyItemRemoved(position);
            });
        } else {
            holder.selectionButtons.setVisibility(View.GONE);
        }

        // Dim if read
        holder.itemView.setAlpha(notification.isRead() ? 0.6f : 1.0f);
    }


    @Override
    public int getItemCount() {
        return notificationList.size();
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
