package ca.yapper.yapperapp.Databases;

import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreUtils {
    private static FirebaseFirestore dbInstance;
    private static boolean isMockMode = false;

    // Set mock instance for testing
    public static void setMockInstance(FirebaseFirestore mockFirestore) {
        dbInstance = mockFirestore;
        isMockMode = true;
    }

    // Get Firestore instance (real or mock)
    public static FirebaseFirestore getFirestoreInstance() {
        if (dbInstance == null) {
            dbInstance = FirebaseFirestore.getInstance();
        }
        return dbInstance;
    }

    // Check if mock mode is enabled
    public static boolean isMockMode() {
        return isMockMode;
    }
}
