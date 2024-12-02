package ca.yapper.yapperapp;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.Databases.FirestoreUtils;

public class AdminDatabaseTest {

    private FirebaseFirestore mockFirestore;
    private CollectionReference mockCollectionReference;
    private DocumentReference mockDocumentReference;
    private Task<Void> mockTask;

    @Before
    public void setUp() {
        // Create new mock instances for each test
        mockFirestore = Mockito.mock(FirebaseFirestore.class);
        mockCollectionReference = Mockito.mock(CollectionReference.class);
        mockDocumentReference = Mockito.mock(DocumentReference.class);
        mockTask = Mockito.mock(Task.class);

        // Use the mock Firestore instance in FirestoreUtils
        FirestoreUtils.useMockInstance(mockFirestore);

        // Set the mock Firestore instance in AdminDatabase
        AdminDatabase.setFirestoreInstance(mockFirestore);

        // Set up mock behaviors
        when(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference);
        when(mockDocumentReference.update(anyMap())).thenReturn(mockTask);
    }

    @After
    public void tearDown() {
        // Clear the static references to avoid shared state
        FirestoreUtils.useMockInstance(null);
        AdminDatabase.setFirestoreInstance(null);
    }

    @Test
    public void testDeleteImageForEvent() {
        // Arrange
        String documentId = "event123";
        String documentType = "event";
        String fieldName = "posterBase64";

        // Act
        Task<Void> resultTask = AdminDatabase.deleteImage(documentId, documentType, fieldName);

        // Assert
        verify(mockFirestore).collection("Events");
        verify(mockCollectionReference).document(documentId);

        Map<String, Object> expectedUpdates = new HashMap<>();
        expectedUpdates.put(fieldName, null);
        verify(mockDocumentReference).update(expectedUpdates);

        assertEquals(mockTask, resultTask);
    }

    @Test
    public void testDeleteImageForUser() {
        // Arrange
        String documentId = "user123";
        String documentType = "user";
        String fieldName = "profileImage";

        // Act
        Task<Void> resultTask = AdminDatabase.deleteImage(documentId, documentType, fieldName);

        // Assert
        verify(mockFirestore).collection("Users");
        verify(mockCollectionReference).document(documentId);

        Map<String, Object> expectedUpdates = new HashMap<>();
        expectedUpdates.put(fieldName, null);
        verify(mockDocumentReference).update(expectedUpdates);

        assertEquals(mockTask, resultTask);
    }
}