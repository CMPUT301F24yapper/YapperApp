package ca.yapper.yapperapp.Databases;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import ca.yapper.yapperapp.UMLClasses.User;

/**
 * Class holding all the Firestore utility related functions that interact with the database.
 */
public class FirestoreUtils {
    private static FirebaseFirestore dbInstance;
    private static boolean isMockMode = false;


    /**
     * Makes the instance of firestore a mock instance. For testing purposes.
     *
     * @param mockFirestore the mock instance of firestore we want to use
     */
    public static void useMockInstance(FirebaseFirestore mockFirestore) {
        dbInstance = mockFirestore;
        isMockMode = true;
        System.out.println("FirestoreUtils: Using mock Firestore instance");
    }


    /**
     * Changes current instance of firestore back to the actual one after testing has been done.
     */
    public static void useRealInstance() {
        dbInstance = FirebaseFirestore.getInstance();
        isMockMode = false;
        System.out.println("FirestoreUtils: Using real Firestore instance");
    }


    /**
     * Function to obtain an instance of the firestore database
     *
     * @return the instance of the database
     */
    public static FirebaseFirestore getFirestoreInstance() {
        if (dbInstance == null) {
            synchronized (FirestoreUtils.class) {
                if (dbInstance == null) {
                    dbInstance = FirebaseFirestore.getInstance();
                    System.out.println("FirestoreUtils: Initialized Firestore instance");
                }
            }
        }
        return dbInstance;
    }


    /**
     * Function to check if certain boolean fields in the database are true.
     *
     * @param collection the firestore collection we wish to check
     * @param documentId the document we wish to check
     * @param field the document we wish to check
     * @return a task that is true if the field we wish to check exists and is true.
     */
    public static Task<Boolean> checkDocumentField(String collection, String documentId, String field) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();
        getFirestoreInstance().collection(collection).document(documentId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Boolean value = document.getBoolean(field);
                        tcs.setResult(value != null && value);
                    } else {
                        tcs.setResult(false);
                    }
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }


    /**
     * Returns true if we are using mock instance of firestore
     * @return
     */
    public static boolean isMockMode() {
        return isMockMode;
    }


    /**
     *
     */
    public static void validateEnvironment() {
        if (isMockMode) {
            throw new IllegalStateException("Mock Firestore instance should not be used in production!");
        }
    }


    /**
     *
     * @param doc
     * @return
     */
    public static User parseUserFromSnapshot(DocumentSnapshot doc) {
        // Ensure all required fields are present
        if (!doc.contains("deviceId") || !doc.contains("entrantEmail") || !doc.contains("Admin")) {
            throw new IllegalArgumentException("Missing required fields in user document.");
        }

        // Parse and return a User object
        return new User(
                doc.getString("deviceId"),
                doc.getString("entrantEmail"),
                Boolean.TRUE.equals(doc.getBoolean("Admin")),
                Boolean.TRUE.equals(doc.getBoolean("Entrant")),
                Boolean.TRUE.equals(doc.getBoolean("Organizer")),
                doc.getString("entrantName"),
                doc.getString("entrantPhone"),
                Boolean.TRUE.equals(doc.getBoolean("notificationsEnabled")),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
    }

}
