package ca.yapper.yapperapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import ca.yapper.yapperapp.UMLClasses.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private List<User> userList;
    private Context context;

    public UsersAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UsersAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_participants_item, parent, false);
        return new UsersAdapter.UsersViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userNameTextView.setText(user.getName());
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.entrant_name);
        }
    }
}