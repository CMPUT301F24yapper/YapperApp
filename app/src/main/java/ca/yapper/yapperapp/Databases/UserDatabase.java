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

/**
 * Class holding all the user class related functions that interact with the database.
 */
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
        validateUserInputs(deviceId, email, name);

        User user = createUserObject(deviceId, email, isAdmin, isEntrant, isOrganizer, name, phoneNum, isOptedOut);

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


    /**
     * This function adds user specific events that are missed out to the users corresponding subcollection
     *
     * @param userDeviceId The device ID of the user.
     * @return a task that adds all the missed out events then completes, otherwise it fails.
     */
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

    /**
     * This function provides user input validation, for the device id, email and name.
     *
     * @param deviceId The Id for users device.
     * @param email The user profile email
     * @param name The user profile name
     */
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
     * This function creates a user object using the given data.
     *
     * @param deviceId The Id for users device.
     * @param email The user profile email
     * @param isAdmin Boolean for checking if the user has the admin role
     * @param isEntrant Boolean for checking if the user has the entrant role
     * @param isOrganizer Boolean for checking if the user has the organizer role
     * @param name The user profile name
     * @param phoneNum The user profile phone number
     * @param isOptedOut The user status on recieving notifcations. (True means they don't want to
     *                   recieve any notifications)
     * @return a new user object created from the given data
     */
    public static User createUserObject(String deviceId, String email, boolean isAdmin, boolean isEntrant,
                                        boolean isOrganizer, String name, String phoneNum, boolean isOptedOut) {
        return new User(deviceId, email, isAdmin, isEntrant, isOrganizer, name, phoneNum,
                isOptedOut, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * This function obtains user data from a user object and converts it into a map of the data
     *
     * @param user a user object
     * @return a map of the users data
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
     * This function takes user data and saves it to the FireStore database
     *
     * @param deviceId The Id for users device.
     * @param user a user object
     * @param userData A map of the users data
     * @return a task that saves the user data to the database and completes, otherwise we recieve an error on failure
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

    /**
     * This function updates a given field in the users document in the database.
     *
     * @param deviceId The Id for users device.
     * @param field name of the field we want to update
     * @param fieldContents the value we want to change the chosen field to
     * @param listener handles the outcome of the operation
     */
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

    /**
     * This function is for validating a field value
     *
     * @param field name of the field we want to validate
     * @param value the value we are validating
     * @return returns true if the value in the field is valid
     */
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

    /**
     * Checks if the given email is valid
     *
     * @param email the users email
     * @return true if email is a valid format
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");
    }

    /**
     * Checks if the given phone is valid
     *
     * @param phone the users phone number
     * @return true if phone is a valid format
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\+?[0-9]{7,15}$");
    }

    /**
     * Checks if the users profile fields are valid
     *
     * @param field a given field
     * @return true if the given field is a user profile valid
     */
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

    /**
     * Interface for location saving success and errors associated with it
     */
    public interface OnLocationSavedListener {
        void onSuccess();
        void onError(String error);
    }

    /**
     * This function saves the users location to the database, for a given event.
     *
     * @param eventId The unique id for the event, created from the QR code.
     * @param userDeviceId The device ID of the user.
     * @param latitude The users latitude
     * @param longitude The users longitude
     * @param listener Listener to handle the outcome of saving the location
     */
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