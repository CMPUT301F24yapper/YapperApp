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
import ca.yapper.yapperapp.UMLClasses.Notification;

/**
 * This is the adapter for notifications entrants can view on their notification page
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;

    private Context context;


    /**
     * Constructor for the adapter
     *
     * @param notificationList a list of notifications
     */
    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }


    /**
     * This function inflates a layout and creates a new view for a notifications
     *
     * @param parent The parent view group for the new view
     * @param viewType The view type
     *
     * @return the new view
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.entrant_notifications_item, parent, false);
        return new NotificationViewHolder(itemView);
    }


    /**
     * This function displays the event information for the notification and deals with accepting/rejecting notifications
     *
     * @param holder The ViewHolder which will represents the data at this position
     * @param position index of item in list
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        // Set notification details
        holder.eventName.setText(notification.getEventName());
        holder.message.setText(notification.getMessage());

        // Format and set the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        holder.date.setText(dateFormat.format(notification.getDateTimeStamp()));

        // Set visibility of buttons based on notification type
        holder.selectionButtons.setVisibility(
                "Selection".equals(notification.getNotificationType()) ? View.VISIBLE : View.GONE
        );

        // Navigate to EventDetailsFragment on item click
        holder.itemView.setOnClickListener(v -> navigateToEventDetails(notification));

        // Accept button logic
        holder.acceptButton.setOnClickListener(v -> handleAcceptNotification(holder, notification, position));

        // Reject button logic
        holder.rejectButton.setOnClickListener(v -> handleRejectNotification(holder, notification, position));

        // Dim the item if the notification is read
        holder.itemView.setAlpha(notification.isRead() ? 0.6f : 1.0f);
    }


    /**
     * This function changes the fragment to a specific event fragment based on what event the notification was for.
     *
     * @param notification a given notification
     */
    private void navigateToEventDetails(Notification notification) {
        String eventId = notification.getEventId();
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(context, "Event ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("0", eventId); // Pass eventId as argument
        eventDetailsFragment.setArguments(args);

        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, eventDetailsFragment) // Correct container ID
                    .addToBackStack(null)
                    .commit();
        }
    }


    /**
     * This function deals with notification acceptance and moves users to final lists
     *
     * @param holder The ViewHolder which will represents the data at this position
     * @param notification a given notification
     * @param position the index of an item in the list
     */
    private void handleAcceptNotification(NotificationViewHolder holder, Notification notification, int position) {
        String eventId = notification.getEventId();
        String userId = notification.getUserToId();

        if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
            Toast.makeText(context, "Missing Event or User ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        OrganizerDatabase.addUserToFinalList(eventId, userId, success -> {
            if (success) {
                markNotificationAsRead(holder, notification, position);
                Toast.makeText(context, "Accepted and added to Final List", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error adding to Final List", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * This function deals with notification rejection and moves users to cancelled lists
     *
     * @param holder The ViewHolder which will represents the data at this position
     * @param notification a given notification
     * @param position the index of an item in the list
     */
    private void handleRejectNotification(NotificationViewHolder holder, Notification notification, int position) {
        String eventId = notification.getEventId();
        String userId = notification.getUserToId();

        if (eventId == null || eventId.isEmpty() || userId == null || userId.isEmpty()) {
            Toast.makeText(context, "Missing Event or User ID.", Toast.LENGTH_SHORT).show();
            return;
        }
        //confirm still works
        OrganizerDatabase.moveUserBetweenEventSubcollections(eventId, userId, "selectedList", "cancelledList", success -> {
            if (success) {
                markNotificationAsRead(holder, notification, position);
                Toast.makeText(context, "Rejected and added to Cancelled List", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error removing from Selected List to Cancelled List", Toast.LENGTH_SHORT).show();
            }
            });
    }


    /**
     * This function changes the notifications status to read
     *
     * @param holder The ViewHolder which will represents the data at this position
     * @param notification a given notification
     * @param position the index of an item in the list
     */
    private void markNotificationAsRead(NotificationViewHolder holder, Notification notification, int position) {
        notification.markAsRead();
        notificationList.remove(position);
        notifyItemRemoved(position);
    }


    /**
     * Returns the size of the list that displays the events
     *
     * @return size of the notification list
     */
    @Override
    public int getItemCount() {
        return notificationList == null ? 0 : notificationList.size();
    }


    /**
     * ViewHolder used for the notification
     */
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
