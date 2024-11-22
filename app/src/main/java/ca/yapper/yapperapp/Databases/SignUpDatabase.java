package ca.yapper.yapperapp.Databases;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Activities.SignupActivity;

public class SignUpDatabase {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Checks if a user exists in the database based on the device ID (used by SignupActivity...)
     */
    public static Task<Boolean> checkUserExists(String deviceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        db.collection("Users").document(deviceId)
                .get()
                .addOnSuccessListener(document -> tcs.setResult(document.exists()))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * Creates a new user in the database with the provided details.
     */
    public static Task<Void> createUser(String deviceId, String name, String phone, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("deviceId", deviceId);
        userMap.put("entrantName", name);
        userMap.put("entrantPhone", phone);
        userMap.put("entrantEmail", email);
        userMap.put("Admin", false);
        userMap.put("Entrant", true);
        userMap.put("Organizer", false);
        userMap.put("notificationsEnabled", true);

        return db.collection("Users").document(deviceId).set(userMap);
    }

}
