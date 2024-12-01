package ca.yapper.yapperapp;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import ca.yapper.yapperapp.Databases.AdminDatabase;
import ca.yapper.yapperapp.Databases.FirestoreUtils;
import ca.yapper.yapperapp.UMLClasses.User;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

public class AdminDatabaseTest {

    private FirebaseFirestore mockFirestore;
    private CollectionReference mockCollection;
    private CollectionReference mockEventsCollection;
    private DocumentReference mockDocument;
    private Task<DocumentSnapshot> mockTask;

    @Before
    public void setUp() {
        // Mock Firestore and its components
        mockFirestore = Mockito.mock(FirebaseFirestore.class);
        mockCollection = Mockito.mock(CollectionReference.class);
        mockDocument = Mockito.mock(DocumentReference.class);
        mockTask = Mockito.mock(Task.class);
        mockEventsCollection = Mockito.mock(CollectionReference.class);

        // Set FirestoreUtils to use the mocked Firestore instance
        FirestoreUtils.useMockInstance(mockFirestore);

        // Reset mocks before each test
        Mockito.reset(mockFirestore, mockCollection, mockDocument);

        // Mock Firestore behavior
        when(mockFirestore.collection("Users")).thenReturn(mockCollection);
        when(mockFirestore.collection("Events")).thenReturn(mockEventsCollection);
        when(mockCollection.document("user123")).thenReturn(mockDocument);
        when(mockDocument.get()).thenReturn(mockTask);
    }

    @Test
    public void testGetProfileImage_Success() {
        // Arrange: Mock a successful Firestore document retrieval
        DocumentSnapshot mockSnapshot = Mockito.mock(DocumentSnapshot.class);
        when(mockSnapshot.exists()).thenReturn(true);
        when(mockSnapshot.getString("profileImage")).thenReturn("profile_image_base64");

        // Simulate Firestore's success behavior
        doAnswer(invocation -> {
            OnSuccessListener<DocumentSnapshot> successListener = invocation.getArgument(0);
            successListener.onSuccess(mockSnapshot); // Trigger success
            return mockTask; // Return the task for chaining
        }).when(mockTask).addOnSuccessListener(any());

        // Act
        String result = AdminDatabase.getProfileImage("user123").getResult();

        // Assert
        assertNotNull(result);
        assertEquals("profile_image_base64", result);
    }

    @Test
    public void testDeleteImage_Success() {
        // Arrange: Mock Firestore behavior
        Task<Void> mockDeleteTask = Mockito.mock(Task.class);
        when(mockDeleteTask.isSuccessful()).thenReturn(true);

        // Mock correct collection and document selection
        when(mockFirestore.collection("Users")).thenReturn(mockCollection);
        when(mockCollection.document("user123")).thenReturn(mockDocument);
        when(mockDocument.update(anyMap())).thenReturn(mockDeleteTask);

        // Act
        Task<Void> result = AdminDatabase.deleteImage("user123", "Users", "profileImage");

        // Assert
        assertNotNull("Task should not be null", result);
        verify(mockDocument).update(argThat(updates ->
                updates.containsKey("profileImage") && updates.get("profileImage") == null
        ));
    }



}