package ca.yapper.yapperapp;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SignupFragment extends Fragment {
    private EditText addEntrantNameEditText;
    private EditText addEntrantPhoneEditText;
    private EditText addEntrantEmailEditText;
    private Button signupButton;
    private FirebaseFirestore db;
    private CollectionReference entrantsRef;
    private String deviceId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_page_fragment, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        entrantsRef = db.collection("entrants");

        // Get device ID
        deviceId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Set up views
        addEntrantNameEditText = view.findViewById(R.id.name_input);
        addEntrantPhoneEditText = view.findViewById(R.id.phone_input);
        addEntrantEmailEditText = view.findViewById(R.id.email_input);
        signupButton = view.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(v -> saveUserToFirestore());

        return view;
    }
    private void saveUserToFirestore() {
        String name = addEntrantNameEditText.getText().toString();
        String phone = addEntrantPhoneEditText.getText().toString();
        String email = addEntrantEmailEditText.getText().toString();

        /** if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        } **/
        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill in your name to sign-up", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a User object with the provided data
        User newUser = new User();
        newUser.setName(name);
        newUser.setDeviceId(deviceId); // Assuming deviceId is available as a class variable
        newUser.setPhoneNum(phone);
        newUser.setEmail(email);

        entrantsRef.document(deviceId).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "User registered successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to home screen after registration
                    ((EntrantActivity) getActivity()).navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding user", e);
                    Toast.makeText(getActivity(), "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

}
