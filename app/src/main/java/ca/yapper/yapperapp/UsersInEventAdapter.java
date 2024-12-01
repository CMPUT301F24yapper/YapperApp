package ca.yapper.yapperapp;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.UMLClasses.User;
/**
 * An adapter class for displaying a list of users in a {@link RecyclerView}.
 * This adapter binds the {@link User} data to each item view in the list, displaying user names.
 */
public class UsersInEventAdapter extends RecyclerView.Adapter<UsersInEventAdapter.UsersViewHolder> {
    private final List<User> userList;
    // private final Context context;
    private String eventId;  // Event context for which statuses are being loaded
    private TextView nameTextView;


    /**
     * Constructs a new {@code UsersAdapter} with the specified list of users and context.
     *
     * @param userList The list of {@link User} objects to be displayed in the {@code RecyclerView}.
     * @param eventId   Event context for which statuses are being loaded
     */
    public UsersInEventAdapter(List<User> userList, String eventId) {
        this.userList = userList;
        this.eventId = eventId;
    }
    /**
     * Creates a new {@link UsersViewHolder} for the specified {@code ViewGroup}.
     *
     * @param parent   The {@link ViewGroup} into which the new item view will be added.
     * @param viewType The type of view to create (not used in this implementation).
     * @return A new instance of {@link UsersViewHolder} bound to a new view.
     */
    @NonNull
    @Override
    public UsersInEventAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_participants_item, parent, false);
        return new UsersInEventAdapter.UsersViewHolder(view);
    }

    /**
     * Binds the user data to the provided {@link UsersViewHolder}.
     *
     * @param holder   The {@link UsersViewHolder} to bind data to.
     * @param position The position of the item in the {@code userList}.
     */
    @Override
    public void onBindViewHolder(@NonNull UsersInEventAdapter.UsersViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userNameTextView.setText(user.getName());

        // Fetch invitation status from Firestore dynamically
        EntrantDatabase.getInvitationStatus(user.getDeviceId(), eventId, new EntrantDatabase.OnStatusCheckListener() {
            @Override
            public void onStatusLoaded(String status) {
                updateStatusIcon(holder.statusIcon, status);
            }

            @Override
            public void onStatusNotFound() {
                updateStatusIcon(holder.statusIcon, "Not found");  // Default to pending
            }

            @Override
            public void onUserNotInList() {
                updateStatusIcon(holder.statusIcon, "Pending");  // Treat as pending
            }

            @Override
            public void onError(String error) {
                Log.e("UserAdapter", "Error fetching status: " + error);
                updateStatusIcon(holder.statusIcon, "Pending");  // Show pending on error
            }
        });
    }

    private void updateStatusIcon(ImageView statusIcon, String status) {
        if ("Accepted".equalsIgnoreCase(status)) {
            statusIcon.setImageResource(R.drawable.ic_check_circle);  // Green checkmark
            statusIcon.setVisibility(View.VISIBLE);
        } else if ("Rejected".equalsIgnoreCase(status)) {
            statusIcon.setImageResource(R.drawable.ic_cancel);  // Red X
            statusIcon.setVisibility(View.VISIBLE);
        } else if ("Pending".equalsIgnoreCase(status)) {
            // Question mark for pending
            statusIcon.setImageResource(R.drawable.ic_pending);
            statusIcon.setVisibility(View.VISIBLE);
        }
        else { // status not found
            // nothing (visibility gone)
        }
    }

    /**
     * Returns the total number of users in the list.
     *
     * @return The size of the {@code userList}.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }
    /**
     * A {@link RecyclerView.ViewHolder} that holds the views for each user item.
     */
    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        ImageView statusIcon;
        /**
         * Constructs a new {@code UsersViewHolder} with the given item view.
         *
         * @param itemView The item view that will be used for each user.
         */
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.entrant_name);
            statusIcon = itemView.findViewById(R.id.status_icon);
        }
    }
}
