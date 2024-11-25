package ca.yapper.yapperapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;
import ca.yapper.yapperapp.Databases.EntrantDatabase;
import ca.yapper.yapperapp.Databases.OrganizerDatabase;
import ca.yapper.yapperapp.UMLClasses.User;

public class ProfileFragment extends Fragment {

    private DocumentReference userRef;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText facilityEditText;
    private EditText addressEditText;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch notificationsSwitch;
    private LinearLayout notificationsSection;
    private LinearLayout facilitySection;
    private ImageView profileImage;
    private TextView changePicture;
    private TextView removePicture;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String userDeviceId;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Map<String, Runnable> pendingUpdates = new HashMap<>();
    private boolean isUpdatingField = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(requireActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        userRef = db.collection("Users").document(deviceId);

        userDeviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        initializeViews(view);
        setVisibilityBasedOnActivity();
        loadUserData();
        setupTextChangeListeners();
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            String base64Image = encodeImageToBase64(imageUri);
                            updateField("profileImage", base64Image); // Save to Firestore
                            Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                            profileImage.setImageBitmap(bitmap); // Update ImageView
                            Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Failed to encode image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateField("notificationsEnabled", isChecked);
        });

        changePicture.setOnClickListener(v -> openImageChooser());
        removePicture.setOnClickListener(v -> removeProfilePicture());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private String encodeImageToBase64(Uri imageUri) throws IOException {
        // Load the image as a bitmap
        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);


        // Compress the bitmap
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream); // Adjust quality as needed
        byte[] compressedBytes = outputStream.toByteArray();

        // Encode to Base64
        return Base64.encodeToString(compressedBytes, Base64.DEFAULT);
    }

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void removeProfilePicture() {
        updateField("profileImage", null); // Reset the Firestore field
        profileImage.setImageResource(R.drawable.ic_profile_placeholder); // Reset to placeholder
        Toast.makeText(getContext(), "Profile picture removed", Toast.LENGTH_SHORT).show();
    }

    private void initializeViews(View view) {
        nameEditText = view.findViewById(R.id.edit_name);
        emailEditText = view.findViewById(R.id.edit_email);
        phoneEditText = view.findViewById(R.id.edit_phone);
        facilityEditText = view.findViewById(R.id.edit_facility);
        addressEditText = view.findViewById(R.id.edit_Address);
        notificationsSwitch = view.findViewById(R.id.switch_notifications);
        notificationsSection = view.findViewById(R.id.notifications_section);
        facilitySection = view.findViewById(R.id.facility_section);

        profileImage = view.findViewById(R.id.profile_image);
        changePicture = view.findViewById(R.id.change_picture);
        removePicture = view.findViewById(R.id.remove_picture);
    }

    private void setVisibilityBasedOnActivity() {
        if (getActivity() instanceof EntrantActivity) {
            notificationsSection.setVisibility(View.VISIBLE);
            facilitySection.setVisibility(View.GONE);
        } else if (getActivity() instanceof OrganizerActivity) {
            notificationsSection.setVisibility(View.GONE);
            facilitySection.setVisibility(View.VISIBLE);
        } else {
            notificationsSection.setVisibility(View.GONE);
            facilitySection.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        EntrantDatabase.loadUserFromDatabase(userDeviceId, new EntrantDatabase.OnUserLoadedListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null) {
                    isUpdatingField = true;
                    nameEditText.setText(user.getName());
                    emailEditText.setText(user.getEmail());
                    Log.e("ProfileFragment", "On User loaded: " + user.getEmail());
                    phoneEditText.setText(user.getPhoneNum());

                    // If the facility section is visible, load facility-related data
                    if (facilitySection.getVisibility() == View.VISIBLE) {
                        loadFacilityData(user);
                    }

                    // Load notifications switch state
                    notificationsSwitch.setChecked(user.isOptedOut());

                    // Load the profile image
                    loadProfileImage(user);

                    isUpdatingField = false;
                }
            }

            @Override
            public void onUserLoadError(String error) {
                Toast.makeText(getContext(), "Error loading profile data: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        /**userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                nameEditText.setText(documentSnapshot.getString("entrantName"));
                emailEditText.setText(documentSnapshot.getString("entrantEmail"));
                phoneEditText.setText(documentSnapshot.getString("entrantPhone"));

                String base64Image = documentSnapshot.getString("profileImage");
                if (base64Image != null) {
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    if (bitmap != null) {
                        profileImage.setImageBitmap(bitmap);
                    } else {
                        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                }

                if (facilitySection.getVisibility() == View.VISIBLE) {
                    facilityEditText.setText(documentSnapshot.getString("facilityName"));
                    addressEditText.setText(documentSnapshot.getString("facilityAddress"));
                }

                if (notificationsSection.getVisibility() == View.VISIBLE) {
                    Boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled");
                    notificationsSwitch.setChecked(notificationsEnabled != null ? notificationsEnabled : true);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
        }); **/
    }

    private void loadFacilityData(User user) {
        OrganizerDatabase.loadFacilityData(userDeviceId, new OrganizerDatabase.OnFacilityDataLoadedListener() {
            @Override
            public void onFacilityDataLoaded(String facilityName, String facilityAddress) {
                facilityEditText.setText(facilityName != null ? facilityName : "Optional");
                addressEditText.setText(facilityAddress != null ? facilityAddress : "Optional");
                Log.d("ProfileFragment", "Facility data loaded: " + facilityName + ", " + facilityAddress);
            }

            @Override
            public void onError(String error) {
                // Handle error if needed
                Log.e("ProfileFragment", "Error loading facility data: " + error);
            }
        });
    }

    private void loadProfileImage(User user) {
        EntrantDatabase.loadProfileImage(userDeviceId, new EntrantDatabase.OnProfileImageLoadedListener() {
            @Override
            public void onProfileImageLoaded(String base64Image) {
                if (base64Image != null) {
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    if (bitmap != null) {
                        profileImage.setImageBitmap(bitmap);
                    } else {
                        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                    }
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                }
            }

            @Override
            public void onError(String error) {
                // Handle error if needed
            }
        });
    }

    private void setupTextChangeListeners() {
        nameEditText.addTextChangedListener(createTextWatcher("entrantName"));
        emailEditText.addTextChangedListener(createTextWatcher("entrantEmail"));
        phoneEditText.addTextChangedListener(createTextWatcher("entrantPhone"));

        if (facilitySection.getVisibility() == View.VISIBLE) {
            facilityEditText.addTextChangedListener(createTextWatcher("facilityName"));
            addressEditText.addTextChangedListener(createTextWatcher("facilityAddress"));
        }
    }

    private TextWatcher createTextWatcher(final String field) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // updateField(field, s.toString());
                if (isUpdatingField) return;  // Prevent feedback loops during updates

                String newValue = s.toString();

                // Remove any pending update for this field
                if (pendingUpdates.containsKey(field)) {
                    handler.removeCallbacks(pendingUpdates.get(field));
                }

                // Add a new update with a delay (e.g., 500ms)
                Runnable updateTask = () -> updateField(field, newValue);
                pendingUpdates.put(field, updateTask);
                handler.postDelayed(updateTask, 500); // Delay of 500ms
            }
        };
    }

    /**private void updateField(String field, Object value) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(field, value);

        userRef.update(updates).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error updating " + field, Toast.LENGTH_SHORT).show();
        });
    }**/

    private void updateField(String field, Object value) {
        EntrantDatabase.updateUserField(userDeviceId, field, value, new EntrantDatabase.OnFieldUpdateListener() {
            @Override
            public void onFieldUpdated(Object value) {
                // Toast.makeText(getContext(), field + " updated successfully", Toast.LENGTH_SHORT).show();
                Log.d("ProfileFragment", "Field updated successfully.");
            }

            @Override
            public void onError(String error) {
                Log.d("ProfileFragment", "Field updated successfully.");
            }
        });
    }
}