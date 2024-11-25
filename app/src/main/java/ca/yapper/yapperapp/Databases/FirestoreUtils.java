package ca.yapper.yapperapp.Databases;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import ca.yapper.yapperapp.UMLClasses.User;

public class FirestoreUtils {
    private static FirebaseFirestore dbInstance;
    private static boolean isMockMode = false;

    public static void useMockInstance(FirebaseFirestore mockFirestore) {
        dbInstance = mockFirestore;
        isMockMode = true;
        System.out.println("FirestoreUtils: Using mock Firestore instance");
    }

    public static void useRealInstance() {
        dbInstance = FirebaseFirestore.getInstance();
        isMockMode = false;
        System.out.println("FirestoreUtils: Using real Firestore instance");
    }

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

    public static boolean isMockMode() {
        return isMockMode;
    }

    public static void validateEnvironment() {
        if (isMockMode) {
            throw new IllegalStateException("Mock Firestore instance should not be used in production!");
        }
    }

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
