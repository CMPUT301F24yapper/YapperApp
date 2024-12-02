package ca.yapper.yapperapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;
import org.mockito.Mockito;

import ca.yapper.yapperapp.Databases.FirestoreUtils;
import ca.yapper.yapperapp.UMLClasses.User;

public class FirestoreUtilsTest {

    @Test
    public void testParseUserFromSnapshot_Success() {
        // Arrange
        DocumentSnapshot mockDocument = Mockito.mock(DocumentSnapshot.class);

        when(mockDocument.contains("deviceId")).thenReturn(true);
        when(mockDocument.contains("entrantEmail")).thenReturn(true);
        when(mockDocument.contains("Admin")).thenReturn(true);

        when(mockDocument.getString("deviceId")).thenReturn("device123");
        when(mockDocument.getString("entrantEmail")).thenReturn("user@example.com");
        when(mockDocument.getBoolean("Admin")).thenReturn(true);
        when(mockDocument.getBoolean("Entrant")).thenReturn(false);
        when(mockDocument.getBoolean("Organizer")).thenReturn(true);
        when(mockDocument.getString("entrantName")).thenReturn("John Doe");
        when(mockDocument.getString("entrantPhone")).thenReturn("555-1234");
        when(mockDocument.getBoolean("notificationsEnabled")).thenReturn(true);

        // Act
        User user = FirestoreUtils.parseUserFromSnapshot(mockDocument);

        // Assert
        assertEquals("device123", user.getDeviceId());
        assertEquals("user@example.com", user.getEmail());
        assertTrue(user.isAdmin());
        assertFalse(user.isEntrant());
        assertTrue(user.isOrganizer());
        assertEquals("John Doe", user.getName());
        assertEquals("555-1234", user.getPhoneNum());
        assertTrue(user.isOptedOut());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUserFromSnapshot_MissingFields() {
        // Arrange
        DocumentSnapshot mockDocument = Mockito.mock(DocumentSnapshot.class);

        when(mockDocument.contains("deviceId")).thenReturn(false);

        // Act
        FirestoreUtils.parseUserFromSnapshot(mockDocument);

        // Assert
        // Expect IllegalArgumentException
    }
}