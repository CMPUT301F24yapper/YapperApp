package ca.yapper.yapperapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import ca.yapper.yapperapp.Databases.UserDatabase;
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
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, Runnable> pendingUpdates = new HashMap<>();
    private boolean isUpdatingField = false;
    private Bitmap generatedProfilePicture;

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
                    getActivity();
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
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
        removePicture.setOnClickListener(v -> {
            try {
                removeProfilePicture();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

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

    private String encodeGeneratedImageToBase64(Bitmap bitmap) throws IOException {
        // Compress the bitmap
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream); // Adjust quality as needed
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

    private void removeProfilePicture() throws IOException {
        Bitmap generatedIMG = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        generateProfileImage(generatedIMG);
        String base64GeneratedImage = encodeGeneratedImageToBase64(generatedIMG);
        updateField("profileImage", base64GeneratedImage); // Once an img is deleted, we reset to generated again

        profileImage.setImageBitmap(generatedIMG); // Reset to generated pic
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
        UserDatabase.loadUserFromDatabase(userDeviceId, new EntrantDatabase.OnUserLoadedListener() {
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
            public void onProfileImageLoaded(String base64Image) throws IOException {
                Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
                generatedProfilePicture = bitmap;
                generateProfileImage(generatedProfilePicture);

                if (base64Image != null) {
                    bitmap = decodeBase64ToBitmap(base64Image);
                    Log.d("Profile IMG", "Bitmap obtained from database");
                }

                if(bitmap != null){ // if base64img has an image
                    if (bitmap.sameAs(generatedProfilePicture)) {
                        // here the image we got from base64 was the generated profile pic or base64 was == null
                        Log.d("Generated IMG","Generated Image Retrieved");
                        //profileImage.setImageResource(R.drawable.ic_profile_placeholder);
                        profileImage.setImageBitmap(bitmap);
                        String base64GeneratedImage = encodeGeneratedImageToBase64(bitmap);
                        updateField("profileImage", base64GeneratedImage); // Here we store generated img in database
                    }
                    else {
                        Log.d("Generated IMG","Custom Image Retrieved");
                        profileImage.setImageBitmap(bitmap);
                    }
                } else {
                    profileImage.setImageBitmap(generatedProfilePicture);
                    String base64GeneratedImage = encodeGeneratedImageToBase64(generatedProfilePicture);
                    updateField("profileImage", base64GeneratedImage); // Here we store generated img in database

                }
            }

            @Override
            public void onError(String error) {
                // Handle error if needed
            }
        });
    }

    private void generateProfileImage(Bitmap bitmap){
        Canvas imageName = new Canvas(bitmap);
        Paint textStyle = new Paint();
        Paint circleStyle = new Paint();
        circleStyle.setColor(Color.parseColor("#80712C95"));

        textStyle.setTextSize(500);
        //String[] profileName = nameEditText.getText().toString().split("[ .;:]");
        String[] profileName = nameEditText.getText().toString().trim().split("\\s+");
        String initials = "";
        for (String word : profileName){
            initials += word.charAt(0);
        }

        float textWidthOnScreen = textStyle.measureText(initials);
        Paint.FontMetrics fontMetrics = textStyle.getFontMetrics();
        float textHeightOnScreen = fontMetrics.descent - fontMetrics.ascent;

        textStyle.setColor(Color.BLACK);
        textStyle.setTextAlign(Paint.Align.CENTER);

        while ((textHeightOnScreen >= imageName.getHeight() * 0.5) || (textWidthOnScreen >= imageName.getWidth() * 0.5)){
            textStyle.setTextSize(textStyle.getTextSize() - 1);

            textWidthOnScreen = textStyle.measureText(initials);
            fontMetrics = textStyle.getFontMetrics();
            textHeightOnScreen = fontMetrics.descent - fontMetrics.ascent;
            // NOTE: descent is distance from _ bar beneath char to bottom of lowest char,
            //       ascent is distance from _ to the top of highest char(-'ve)
        }

        imageName.drawCircle(imageName.getWidth() / 2, imageName.getHeight() / 2, imageName.getWidth() / 2, circleStyle);
        imageName.drawText(initials,imageName.getWidth() / 2, (imageName.getHeight() / 2) - ((fontMetrics.ascent + fontMetrics.descent) / 2), textStyle);
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
                if (field == "facilityName") {
                    OrganizerDatabase.updateFacilityNameForEvents(userDeviceId, field);
                }
                else if (field == "facilityLocation") {
                    OrganizerDatabase.updateFacilityNameForEvents(userDeviceId, field);
                }
                // EntrantDatabase.updateFacilityAddressForEvents(userDeviceId, facilityAddress);
                pendingUpdates.put(field, updateTask);
                handler.postDelayed(updateTask, 500); // Delay of 500ms
            }
        };
    }

    private void updateField(String field, Object value) {
        UserDatabase.updateUserField(userDeviceId, field, value, new EntrantDatabase.OnFieldUpdateListener() {
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