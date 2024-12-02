package ca.yapper.yapperapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
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

/**
 * This is the fragment that allows admins to delete user profiles
 */
public class AdminRemoveProfileFragment extends Fragment {

    private String userId;
    private TextView profileName, profileEmail, profilePhone, profileFacility, profileAddress;
    private TextView adminStatus, entrantStatus, organizerStatus, deviceIdText, notificationStatus;
    private Button removeProfileButton;
    private Button removeFacilityButton;
    private Button removeProfilePictureButton;
    private ImageView profileImage;
    private View facilitySection;
    private TextView facilityHeader;


    /**
     *
     * Inflates the fragment layout, sets up UI components,
     * and loads user details.
     *
     * @param inflater LayoutInflater used to inflate the fragment layout.
     * @param container The parent view that this fragment's UI is attached to.
     * @param savedInstanceState Previous state data, if any.
     * @return The root view of the fragment.
     */
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


    /**
     * This function initializes all the references to the UI elements for the profile and admin functionality
     *
     * @param view the parent view
     */
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
        facilitySection = view.findViewById(R.id.facility_section);
        facilityHeader = view.findViewById(R.id.facility_header);

        removeProfileButton = view.findViewById(R.id.btn_remove_profile);
        removeFacilityButton = view.findViewById(R.id.btn_remove_facility);
        removeProfilePictureButton = view.findViewById(R.id.btn_remove_profile_picture);

        facilitySection.setVisibility(View.GONE);
        facilityHeader.setVisibility(View.GONE);
        removeFacilityButton.setVisibility(View.GONE);
    }


    /**
     * This function obtains the user details from the database and updates the UI elements accordingly
     */
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

                AdminDatabase.getFacilityDetails(userId, new AdminDatabase.OnFacilityDetailsLoadedListener() {
                    /**
                     * This function changes the visibility of UI elements depending on a users role
                     *
                     * @param facilityName the facilities name
                     * @param facilityAddress the facilities address
                     */
                    @Override
                    public void onFacilityLoaded(String facilityName, String facilityAddress) {
                        boolean isOrganizer = !TextUtils.isEmpty(facilityName) && !TextUtils.isEmpty(facilityAddress);
                        organizerStatus.setText(isOrganizer ? "Yes" : "No");

                        if (isOrganizer) {
                            facilitySection.setVisibility(View.VISIBLE);
                            facilityHeader.setVisibility(View.VISIBLE);
                            removeFacilityButton.setVisibility(View.VISIBLE);
                            profileFacility.setText(facilityName);
                            profileAddress.setText(facilityAddress);
                        } else {
                            facilitySection.setVisibility(View.GONE);
                            facilityHeader.setVisibility(View.GONE);
                            removeFacilityButton.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onError(String error) {
                        organizerStatus.setText("No");
                        facilitySection.setVisibility(View.GONE);
                        facilityHeader.setVisibility(View.GONE);
                        removeFacilityButton.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onUserLoadError(String error) {}
        });
    }


    /**
     * This function sets up button functionality for the different elements an admin could delete from
     * a users profile page.
     */
    private void setupButtons() {
        removeProfileButton.setOnClickListener(v -> handleUserDeletion());
        removeFacilityButton.setOnClickListener(v -> handleFacilityRemoval());
        removeProfilePictureButton.setOnClickListener(v -> handleProfilePictureRemoval());
    }


    /**
     * This function deletes a user from the database, and switches fragments accordingly
     */
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


    /**
     * This function deletes a facility from the database, and switches fragments accordingly
     */
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


    /**
     * This function deletes a profile picture from the database and updates the UI accordingly
     */
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