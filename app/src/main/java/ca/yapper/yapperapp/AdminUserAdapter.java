package ca.yapper.yapperapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.UMLClasses.User;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {
    private final List<User> userList;
    private final Context context;

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

        AdminDatabase.getProfileImage(user.getDeviceId())
                .addOnSuccessListener(base64Image -> {
                    if (base64Image != null && !base64Image.isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            holder.profileImage.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            holder.profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                        }
                    }
                })
                .addOnFailureListener(e -> holder.profileImage.setImageResource(R.drawable.ic_profile_placeholder));

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