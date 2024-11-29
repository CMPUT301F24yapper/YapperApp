package ca.yapper.yapperapp;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
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
    private final List<Notification> notificationList;
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

        // Set notification details
        holder.eventName.setText(notification.getTitle());
        holder.message.setText(notification.getMessage());

        // Format and set the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.date.setText(dateFormat.format(notification.getDateTimeStamp()));

        // Set OnClickListener for itemView to navigate to EventDetailsFragment
        holder.itemView.setOnClickListener(v -> {
            String eventId = notification.getEventId();
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(context, "Event ID is missing!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create the EventDetailsFragment and pass the eventId
            EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
            Bundle args = new Bundle();
            args.putString("0", eventId); // Pass eventId as argument
            eventDetailsFragment.setArguments(args);

            // Transition to the EventDetailsFragment
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, eventDetailsFragment) // Ensure correct container ID
                        .addToBackStack(null) // Add to backstack for navigation
                        .commit();
            }
        });

        // Handle Accept button logic
        holder.acceptButton.setOnClickListener(v -> {
            String eventId = notification.getEventId();
            String userId = notification.getUserToId();

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
                    // Remove the notification after successful addition
                    notification.markAsRead();
                    notificationList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Accepted and added to Final List", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error adding to Final List", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Handle Reject button logic
        holder.rejectButton.setOnClickListener(v -> {
            String eventId = notification.getEventId();
            String userId = notification.getUserToId();

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
                    OrganizerDatabase.removeUserFromSelectedList(eventId, userId, removed -> {
                        if (removed) {
                            // Remove the notification after successful rejection
                            notification.markAsRead();
                            notificationList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Rejected and added to Cancelled List", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error removing from Selected List", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(context, "Error adding to Cancelled List", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Dim the item if the notification is read
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