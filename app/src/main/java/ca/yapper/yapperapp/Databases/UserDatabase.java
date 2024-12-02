package ca.yapper.yapperapp.Databases;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.yapper.yapperapp.UMLClasses.Event;
import ca.yapper.yapperapp.UMLClasses.User;

public class UserDatabase {

    private static FirebaseFirestore db = FirestoreUtils.getFirestoreInstance();

    public static void setFirestoreInstance(FirebaseFirestore firestore) {
        db = firestore;
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(User user);
        void onUserLoadError(String error);
    }

    /**
     * Loads a User from Firestore using the specified device ID and provides the result
     * through the provided listener.
     *
     * @param userDeviceId The unique device ID of the user to be loaded.
     * @param listener     The listener to handle success or error when loading the user.
     */
    public static void loadUserFromDatabase(String userDeviceId, EntrantDatabase.OnUserLoadedListener listener) {
        // Early exit for invalid input
        if (userDeviceId == null || userDeviceId.isEmpty()) {
            listener.onUserLoadError("User ID is invalid or missing.");
            return;
        }

        // Query Firestore
        db.collection("Users").document(userDeviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            // Attempt to parse user
                            User user = FirestoreUtils.parseUserFromSnapshot(documentSnapshot);
                            listener.onUserLoaded(user);
                        } catch (IllegalArgumentException e) {
                            listener.onUserLoadError("User data is invalid: " + e.getMessage());
                        }
                    } else {
                        listener.onUserLoadError("User not found in the database.");
                    }
                })
                .addOnFailureListener(e -> listener.onUserLoadError("Failed to load user: " + e.getMessage()));
    }

    /**
     * Creates a new user entry in Firestore with the provided details.
     *
     * @param deviceId    The unique device ID of the user.
     * @param email       The email address of the user.
     * @param isAdmin     True if the user has admin privileges.
     * @param isEntrant   True if the user has entrant privileges.
     * @param isOrganizer True if the user has organizer privileges.
     * @param name        The name of the user.
     * @param phoneNum    The phone number of the user.
     * @param isOptedOut  True if the user has opted out of notifications.
     * @return The created User instance.
     */
    public static Task<User> createUserInDatabase(String deviceId, String email, boolean isAdmin,
                                                  boolean isEntrant, boolean isOrganizer, String name,
                                                  String phoneNum, boolean isOptedOut) {
        validateUserInputs(deviceId, email, name); // Step 1: Validation

        User user = createUserObject(deviceId, email, isAdmin, isEntrant, isOrganizer, name, phoneNum, isOptedOut); // Step 2: Create User Object

        Map<String, Object> userData = prepareUserData(user); // Step 3: Prepare Firestore Data

        TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
        FirestoreUtils.getFirestoreInstance().collection("Users")
                .document(deviceId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    addMissedOutEventsSubcollection(deviceId)
                            .addOnSuccessListener(subVoid -> tcs.setResult(user))
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    private static Task<Void> addMissedOutEventsSubcollection(String userDeviceId) {
        CollectionReference eventsRef = db.collection("Events");
        CollectionReference missedOutEventsRef = db.collection("Users").document(userDeviceId).collection("missedOutEvents");

        return eventsRef.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                List<Task<Void>> tasks = new ArrayList<>();
                for (DocumentSnapshot eventDoc : task.getResult().getDocuments()) {
                    String eventId = eventDoc.getId();
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("eventId", eventId);
                    tasks.add(missedOutEventsRef.document(eventId).set(eventData));
                }
                return Tasks.whenAll(tasks);
            } else {
                throw task.getException();
            }
        });
    }


    public static void validateUserInputs(String deviceId, String email, String name) {
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalArgumentException("Device ID cannot be null or empty");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }

    /**
     * Creates a User object based on the provided details.
     */
    public static User createUserObject(String deviceId, String email, boolean isAdmin, boolean isEntrant,
                                        boolean isOrganizer, String name, String phoneNum, boolean isOptedOut) {
        return new User(deviceId, email, isAdmin, isEntrant, isOrganizer, name, phoneNum,
                isOptedOut, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Prepares a Map of user data for Firestore.
     */
    public static Map<String, Object> prepareUserData(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("deviceId", user.getDeviceId());
        userData.put("entrantEmail", user.getEmail());
        userData.put("Admin", user.isAdmin());
        userData.put("Entrant", user.isEntrant());
        userData.put("Organizer", user.isOrganizer());
        userData.put("entrantName", user.getName());
        userData.put("entrantPhone", user.getPhoneNum().isEmpty() ? "" : user.getPhoneNum());
        userData.put("notificationsEnabled", !user.isOptedOut());
        userData.put("facilityName", "");
        userData.put("facilityAddress", "");

        return userData;
    }

    /**
     * Saves the user data to Firestore.
     */
    private static Task<User> saveUserToFirestore(String deviceId, User user, Map<String, Object> userData) {
        TaskCompletionSource<User> tcs = new TaskCompletionSource<>();
        FirestoreUtils.getFirestoreInstance().collection("Users")
                .document(deviceId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserDatabase", "User successfully saved: " + user.getDeviceId());
                    tcs.setResult(user); // Resolve with the created user
                })
                .addOnFailureListener(e -> {
                    Log.e("UserDatabase", "Error saving user to Firestore", e);
                    tcs.setException(e); // Pass the error to the caller
                });
        return tcs.getTask();
    }

    public static void updateUserField(String deviceId, String field, Object fieldContents, EntrantDatabase.OnFieldUpdateListener listener) {
        // Validate deviceId
        if (deviceId == null || deviceId.isEmpty()) {
            listener.onError("Invalid deviceId");
            return;
        }

        // Validate field name
        if (field == null || field.isEmpty()) {
            listener.onError("Field name cannot be null or empty");
            return;
        }

        // Perform field-specific validation
        if (!validateFieldValue(field, fieldContents)) {
            listener.onError("Invalid value for field: " + field);
            return;
        }

        // Prepare data for update
        Map<String, Object> updateData = new HashMap<>();
        updateData.put(field, fieldContents);

        // Update Firestore
        FirestoreUtils.getFirestoreInstance().collection("Users").document(deviceId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> listener.onFieldUpdated(fieldContents))  // Update successful
                .addOnFailureListener(e -> listener.onError("Error updating field: " + e.getMessage()));  // Update failed
    }

    public static boolean validateFieldValue(String field, Object value) {
        switch (field) {
            case "entrantEmail":
                return value instanceof String && isValidEmail((String) value);
            case "entrantName":
                return value instanceof String && !((String) value).isEmpty();
            case "entrantPhone":
                return value instanceof String && isValidPhone((String) value);
            case "notificationsEnabled":
                return value instanceof Boolean;
            default:
                return true; // No validation needed for other fields
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+?[0-9]{7,15}$");
    }

    private static boolean isValidField(String field) {
        // Add all valid field names
        List<String> validFields = List.of("entrantEmail", "entrantName", "entrantPhone", "notificationsEnabled");
        return validFields.contains(field);
    }

    /**
     * Checks if a user is an admin based on their device ID.
     *
     * @param deviceId The device ID of the user.
     * @return A Task that resolves to true if the user is an admin, false otherwise.
     */
    public static Task<Boolean> checkIfUserIsAdmin(String deviceId) {
        return FirestoreUtils.checkDocumentField("Users", deviceId, "Admin");
    }

    public interface OnLocationSavedListener {
        void onSuccess();
        void onError(String error);
    }

    public static void saveLocationToFirestore(String eventId, String userDeviceId, double latitude, double longitude, OnLocationSavedListener listener) {
        if (eventId == null || eventId.isEmpty() || userDeviceId == null || userDeviceId.isEmpty()) {
            listener.onError("Invalid event ID or device ID.");
            return;
        }

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        Log.d("UserDB", "Saving latitude: " + latitude);

        locationData.put("longitude", longitude);
        Log.d("UserDB", "Saving longitude: " + longitude);

        locationData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Events").document(eventId)
                .collection("waitingList").document(userDeviceId)
                .set(locationData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError("Failed to save location: " + e.getMessage()));

    }
}