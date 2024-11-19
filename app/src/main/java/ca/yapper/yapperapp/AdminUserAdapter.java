package ca.yapper.yapperapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        holder.profileName.setText(user.getName());
        // Profile image loading would go here

        holder.itemView.setOnClickListener(v -> {
            // Implement removal confirmation dialog here
            showRemoveConfirmationDialog(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void showRemoveConfirmationDialog(User user) {
        // Show dialog using admin_deleteconfirmation.xml
        // On confirm, call AdminDatabase.removeUser(user.getDeviceId())
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
