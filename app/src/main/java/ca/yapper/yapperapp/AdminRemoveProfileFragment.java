package ca.yapper.yapperapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ca.yapper.yapperapp.AdminFragments.AdminSearchFragment;
import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.UMLClasses.User;

public class AdminRemoveProfileFragment extends Fragment {
    private String userId;
    private TextView profileName, profileEmail, profileFacility;
    private Button removeProfileButton;
    private Button removeFacilityButton;
    private Button removeProfilePictureButton;
    private ImageView profileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_removeprofile, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        initializeViews(view);
        loadUserDetails();
        loadProfileImage();
        setupButtons();

        return view;
    }

    private void initializeViews(View view) {
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profileFacility = view.findViewById(R.id.profile_facility);
        removeProfileButton = view.findViewById(R.id.btn_remove_profile);
        removeFacilityButton = view.findViewById(R.id.btn_remove_facility);
        removeProfilePictureButton = view.findViewById(R.id.btn_remove_profile_picture);
        profileImage = view.findViewById(R.id.profile_image);
    }

    private void loadUserDetails() {
        UserDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                profileName.setText(user.getName());
                profileEmail.setText(user.getEmail());
            }
            @Override
            public void onUserLoadError(String error) {}
        });
    }

    private void loadProfileImage() {
        EntrantDatabase.loadProfileImage(userId, new EntrantDatabase.OnProfileImageLoadedListener() {
            @Override
            public void onProfileImageLoaded(String base64Image) {
                if (base64Image != null) {
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profileImage.setImageBitmap(bitmap);
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }
            @Override
            public void onError(String error) {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }
        });
    }

    private void setupButtons() {
        removeProfileButton.setOnClickListener(v -> handleUserDeletion());
        removeFacilityButton.setOnClickListener(v -> handleFacilityRemoval());
        removeProfilePictureButton.setOnClickListener(v -> handleProfilePictureRemoval());
    }

    private void handleUserDeletion() {
        AdminDatabase.removeUser(userId).addOnSuccessListener(aVoid -> {
            FragmentManager fm = getParentFragmentManager();
            fm.popBackStack();

            Fragment searchFragment = new AdminSearchFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .commit();
        });
    }

    private void handleFacilityRemoval() {
        AdminDatabase.removeFacility(userId).addOnSuccessListener(aVoid -> {
            FragmentManager fm = getParentFragmentManager();
            fm.popBackStack();

            Fragment searchFragment = new AdminSearchFragment();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .commit();
        });
    }

    private void handleProfilePictureRemoval() {
        UserDatabase.updateUserField(userId, "profileImage", null, new EntrantDatabase.OnFieldUpdateListener() {
            @Override
            public void onFieldUpdated(Object value) {
                profileImage.setImageResource(R.drawable.ic_profile_placeholder);
            }
            @Override
            public void onError(String error) {}
        });
    }
}