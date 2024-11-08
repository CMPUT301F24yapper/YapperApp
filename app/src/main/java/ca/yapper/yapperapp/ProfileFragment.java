package ca.yapper.yapperapp;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.Activities.OrganizerActivity;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private String deviceId;
    private DocumentReference userRef;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText facilityEditText;
    private EditText addressEditText;
    private Switch notificationsSwitch;
    private LinearLayout notificationsSection;
    private LinearLayout facilitySection;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_layout, container, false);

        db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        userRef = db.collection("Users").document(deviceId);

        initializeViews(view);
        setVisibilityBasedOnActivity();
        loadUserData();
        setupTextChangeListeners();

        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateField("notificationsEnabled", isChecked);
        });

        return view;
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
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                nameEditText.setText(documentSnapshot.getString("entrantName"));
                emailEditText.setText(documentSnapshot.getString("entrantEmail"));
                phoneEditText.setText(documentSnapshot.getString("entrantPhone"));

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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                updateField(field, s.toString());
            }
        };
    }



    private void updateField(String field, Object value) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(field, value);

        userRef.update(updates).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error updating " + field, Toast.LENGTH_SHORT).show();
        });
    }
}