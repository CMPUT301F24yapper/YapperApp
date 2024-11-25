package ca.yapper.yapperapp.Databases;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.FirebaseFirestore;

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


}
