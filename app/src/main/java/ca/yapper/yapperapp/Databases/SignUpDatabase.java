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

/**
 * Class holding all the signup page related functions that interact with the database.
 */
public class SignUpDatabase {

    private static final FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    /**
     * This function uses the device Id to check if we have the given user in the database (used by SignupActivity...)
     *
     * @param deviceId The Id for users device.
     * @return a Task with a result that indicates if a user exists(true) or not(false).
     */
    public static Task<Boolean> checkUserExists(String deviceId) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        db.collection("Users").document(deviceId)
                .get()
                .addOnSuccessListener(document -> tcs.setResult(document.exists()))
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    /**
     * This function uses the given details to make a new user object
     *
     * @param deviceId The Id for users device.
     * @param name The name the user input
     * @param phone The phone number the user input
     * @param email The email the user input
     * @return a task that creates a new user document and completes, otherwise it fails.
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
