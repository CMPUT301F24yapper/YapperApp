package ca.yapper.yapperapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ca.yapper.yapperapp.UMLClasses.User;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context;

    public AdminUserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_profileitem, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        String displayName = user.getName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getEmail();
            if (displayName == null || displayName.isEmpty()) {
                displayName = "Unknown User";
            }
        }
        holder.profileName.setText(displayName);

        holder.itemView.setOnClickListener(v -> {
            AdminRemoveProfileFragment fragment = new AdminRemoveProfileFragment();
            Bundle args = new Bundle();
            args.putString("userId", user.getDeviceId());
            fragment.setArguments(args);

            ((FragmentActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView profileName;
        ImageView profileImage;

        UserViewHolder(View itemView) {
            super(itemView);
            profileName = itemView.findViewById(R.id.profile_name);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}