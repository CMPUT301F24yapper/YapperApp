package ca.yapper.yapperapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import ca.yapper.yapperapp.UMLClasses.User;
/**
 * An adapter class for displaying a list of users in a {@link RecyclerView}.
 * This adapter binds the {@link User} data to each item view in the list, displaying user names.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private final List<User> userList;
    private final Context context;
    private TextView nameTextView;


    /**
     * Constructs a new {@code UsersAdapter} with the specified list of users and context.
     *
     * @param userList The list of {@link User} objects to be displayed in the {@code RecyclerView}.
     * @param context  The context in which this adapter operates.
     */
    public UsersAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
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
    public UsersAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_participants_item, parent, false);
        return new UsersAdapter.UsersViewHolder(view);
    }

    /**
     * Binds the user data to the provided {@link UsersViewHolder}.
     *
     * @param holder   The {@link UsersViewHolder} to bind data to.
     * @param position The position of the item in the {@code userList}.
     */
    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userNameTextView.setText(user.getName());

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


        /**
         * Constructs a new {@code UsersViewHolder} with the given item view.
         *
         * @param itemView The item view that will be used for each user.
         */
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.entrant_name);
        }
    }
}