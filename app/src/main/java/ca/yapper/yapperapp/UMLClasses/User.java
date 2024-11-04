package ca.yapper.yapperapp.UMLClasses;

import java.util.ArrayList;

public class User {
    private String deviceId;
    private String name;
    private String email;
    private String phoneNum;
    // private ArrayList<Role> roles;
    // private ProfilePic profilePic;
    // private ProfilePic generatedProfilePic;
    // implement Booleans for role (not an array anymore!)
    private Boolean isEntrant;
    private Boolean isOrganizer;
    private Boolean isAdmin;
    private Boolean isOptedOut; // Added attribute for notification opt-out status

    // Default constructor (required for Firestore deserialization)
    public User() {}
    // constructor version with parameters
    public User(String name, String deviceId, String email, String phoneNum, Boolean isEntrant, Boolean isOrganizer, Boolean isAdmin, Boolean isOptedOut) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phoneNum = phoneNum;
        // this.profilePic = profilePic;
        // this.generatedProfilePic = generatedProfilePic;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
        this.isOptedOut = isOptedOut; // Initialize in the constructor
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    /** public ProfilePic getProfilePic() {
     return profilePic;
     }

     public void setProfilePic(ProfilePic profilePic) {
     this.profilePic = profilePic;
     }

     public ProfilePic getGeneratedProfilePic() {
     return generatedProfilePic;
     }

     public void setGeneratedProfilePic(ProfilePic generatedProfilePic) {
     this.generatedProfilePic = generatedProfilePic;
     } **/

    public Boolean getIsEntrant() {
        return isEntrant;
    }

    public void setIsEntrant(Boolean entrant) {
        isEntrant = entrant;
    }

    public Boolean getIsOrganizer() {
        return isOrganizer;
    }

    public void setIsOrganizer(Boolean organizer) {
        isOrganizer = organizer;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public Boolean getIsOptedOut() { // Getter for isOptedOut
        return isOptedOut;
    }

    public void setIsOptedOut(Boolean isOptedOut) { // Setter for isOptedOut
        this.isOptedOut = isOptedOut;
    }

    // other methods (specific to User) beyond Gs & Ss: can be moved around in package but for now I am placing them here

    public void uploadProfilePic(ProfilePic profilePic) {
        // method logic
    }

    public void removeProfilePic() {
        // method logic
    }

//    public ProfilePic generateProfilePic(String name) {
//        // method logic
//    }
}
