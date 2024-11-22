package ca.yapper.yapperapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.UMLClasses.User;

public class AdminRemoveProfileFragment extends Fragment {
    private String userId;
    private TextView profileName, profileEmail, profileFacility;
    private Button removeProfileButton;

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
        profileFacility = view.findViewById(R.id.profile_facility);
        removeProfileButton = view.findViewById(R.id.btn_remove_profile);
    }

    private void loadUserDetails() {
        EntrantDatabase.loadUserFromDatabase(userId, new EntrantDatabase.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                profileName.setText(user.getName());
                profileEmail.setText(user.getEmail());
            }
            @Override
            public void onUserLoadError(String error) {}
        });
    }

    private void setupButtons() {
        removeProfileButton.setOnClickListener(v -> {
            AdminDatabase.removeUser(userId).addOnSuccessListener(aVoid -> {
                getParentFragmentManager().popBackStack();
            });
        });
    }
}