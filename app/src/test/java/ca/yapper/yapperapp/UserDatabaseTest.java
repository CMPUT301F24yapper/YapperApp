package ca.yapper.yapperapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import ca.yapper.yapperapp.Databases.UserDatabase;
import ca.yapper.yapperapp.UMLClasses.User;
import org.junit.Test;

import java.util.Map;

public class UserDatabaseTest {

    @Test
    public void testValidateUserInputs_ValidInputs() {
        // Arrange
        String deviceId = "device123";
        String email = "user@example.com";
        String name = "John Doe";

        // Act & Assert
        // Should not throw any exception
        UserDatabase.validateUserInputs(deviceId, email, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateUserInputs_InvalidDeviceId() {
        // Arrange
        String deviceId = "";
        String email = "user@example.com";
        String name = "John Doe";

        // Act
        UserDatabase.validateUserInputs(deviceId, email, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateUserInputs_InvalidEmail() {
        // Arrange
        String deviceId = "device123";
        String email = "userexample.com";  // Invalid email format
        String name = "John Doe";

        // Act
        UserDatabase.validateUserInputs(deviceId, email, name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateUserInputs_InvalidName() {
        // Arrange
        String deviceId = "device123";
        String email = "user@example.com";
        String name = "";

        // Act
        UserDatabase.validateUserInputs(deviceId, email, name);
    }

    @Test
    public void testCreateUserObject() {
        // Arrange
        String deviceId = "device123";
        String email = "user@example.com";
        boolean isAdmin = false;
        boolean isEntrant = true;
        boolean isOrganizer = false;
        String name = "John Doe";
        String phoneNum = "555-1234";
        boolean isOptedOut = false;

        // Act
        User user = UserDatabase.createUserObject(
                deviceId, email, isAdmin, isEntrant, isOrganizer, name, phoneNum, isOptedOut);

        // Assert
        assertEquals(deviceId, user.getDeviceId());
        assertEquals(email, user.getEmail());
        assertEquals(isAdmin, user.isAdmin());
        assertEquals(isEntrant, user.isEntrant());
        assertEquals(isOrganizer, user.isOrganizer());
        assertEquals(name, user.getName());
        assertEquals(phoneNum, user.getPhoneNum());
        assertEquals(isOptedOut, user.isOptedOut());
    }

    @Test
    public void testPrepareUserData() {
        // Arrange
        User user = new User(
                "device123",
                "user@example.com",
                false,
                true,
                false,
                "John Doe",
                "555-1234",
                false,
                null, null, null, null
        );

        // Act
        Map<String, Object> userData = UserDatabase.prepareUserData(user);

        // Assert
        assertEquals("device123", userData.get("deviceId"));
        assertEquals("user@example.com", userData.get("entrantEmail"));
        assertEquals(false, userData.get("Admin"));
        assertEquals(true, userData.get("Entrant"));
        assertEquals(false, userData.get("Organizer"));
        assertEquals("John Doe", userData.get("entrantName"));
        assertEquals("555-1234", userData.get("entrantPhone"));
        assertEquals(true, userData.get("notificationsEnabled"));  // Note inversion
        assertEquals("", userData.get("facilityName"));
        assertEquals("", userData.get("facilityAddress"));
    }

    @Test
    public void testValidateFieldValue_ValidEmail() {
        assertTrue(UserDatabase.validateFieldValue("entrantEmail", "user@example.com"));
    }

    @Test
    public void testValidateFieldValue_InvalidEmail() {
        assertFalse(UserDatabase.validateFieldValue("entrantEmail", "userexample.com"));
    }

    @Test
    public void testValidateFieldValue_ValidName() {
        assertTrue(UserDatabase.validateFieldValue("entrantName", "John Doe"));
    }

    @Test
    public void testValidateFieldValue_InvalidName() {
        assertFalse(UserDatabase.validateFieldValue("entrantName", ""));
    }

    @Test
    public void testValidateFieldValue_ValidPhone() {
        assertTrue(UserDatabase.validateFieldValue("entrantPhone", "+1234567890"));
    }

    @Test
    public void testValidateFieldValue_InvalidPhone() {
        assertFalse(UserDatabase.validateFieldValue("entrantPhone", "abc123"));
    }

    @Test
    public void testValidateFieldValue_ValidNotificationsEnabled() {
        assertTrue(UserDatabase.validateFieldValue("notificationsEnabled", true));
    }

    @Test
    public void testValidateFieldValue_InvalidNotificationsEnabled() {
        assertFalse(UserDatabase.validateFieldValue("notificationsEnabled", "yes"));
    }

    @Test
    public void testIsValidEmail_Valid() {
        assertTrue(UserDatabase.isValidEmail("user@example.com"));
    }

    @Test
    public void testIsValidEmail_Invalid() {
        assertFalse(UserDatabase.isValidEmail("userexample.com"));
        assertFalse(UserDatabase.isValidEmail("user@com"));
        assertFalse(UserDatabase.isValidEmail("user@.com"));
    }

    @Test
    public void testIsValidPhone_Valid() {
        assertTrue(UserDatabase.isValidPhone("+1234567890"));
        assertTrue(UserDatabase.isValidPhone("1234567890"));
    }

    @Test
    public void testIsValidPhone_Invalid() {
        assertFalse(UserDatabase.isValidPhone("abc123"));
        assertFalse(UserDatabase.isValidPhone("12345"));
        assertFalse(UserDatabase.isValidPhone(null));
    }
}