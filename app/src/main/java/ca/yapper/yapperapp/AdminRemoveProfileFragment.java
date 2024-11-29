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
    private TextView profileName, profileEmail, profilePhone, profileFacility, profileAddress;
    private TextView adminStatus, entrantStatus, organizerStatus, deviceIdText, notificationStatus;
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
        setupButtons();

        return view;
    }

    private void initializeViews(View view) {
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profilePhone = view.findViewById(R.id.profile_phone);
        profileFacility = view.findViewById(R.id.profile_facility);
        profileAddress = view.findViewById(R.id.profile_address);
        adminStatus = view.findViewById(R.id.admin_status);
        entrantStatus = view.findViewById(R.id.entrant_status);
        organizerStatus = view.findViewById(R.id.organizer_status);
        deviceIdText = view.findViewById(R.id.device_id);
        notificationStatus = view.findViewById(R.id.notification_status);
        profileImage = view.findViewById(R.id.profile_image);

        removeProfileButton = view.findViewById(R.id.btn_remove_profile);
        removeFacilityButton = view.findViewById(R.id.btn_remove_facility);
        removeProfilePictureButton = view.findViewById(R.id.btn_remove_profile_picture);
    }

    private void loadUserDetails() {
        UserDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                profileName.setText(user.getName());
                profileEmail.setText(user.getEmail());
                profilePhone.setText(user.getPhoneNum());
                deviceIdText.setText(user.getDeviceId());

                adminStatus.setText(user.isAdmin() ? "Yes" : "No");
                entrantStatus.setText(user.isEntrant() ? "Yes" : "No");
                organizerStatus.setText(user.isOrganizer() ? "Yes" : "No");
                notificationStatus.setText(user.isOptedOut() ? "Disabled" : "Enabled");

                EntrantDatabase.loadProfileImage(userId, new EntrantDatabase.OnProfileImageLoadedListener() {
                    @Override
                    public void onProfileImageLoaded(String base64Image) {
                        if (base64Image != null) {
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profileImage.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                });
            }

            @Override
            public void onUserLoadError(String error) {}
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