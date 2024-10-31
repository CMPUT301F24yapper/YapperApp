package ca.yapper.yapperapp.UMLClasses;

import java.util.ArrayList;

public class User {
    private String name;
    private String deviceId;
    private String email;
    private String phoneNum;
    private ArrayList<Role> roles;
    private ProfilePic profilePic;
    private ProfilePic generatedProfilePic;

    // Default constructor (required for Firestore deserialization)
    public User() {}
    // constructor version with parameters
    public User(String name, String deviceId, String email, String phoneNum, ProfilePic profilePic, ProfilePic generatedProfilePic) {
        this.name = name;
        this.deviceId = deviceId;
        this.email = email;
        this.phoneNum = phoneNum;
        this.profilePic = profilePic;
        this.generatedProfilePic = generatedProfilePic;
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

    public ProfilePic getProfilePic() {
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
    }

    // other methods (specific to User) beyond Gs & Ss: can be moved around in package but for now I am placing them here
//    public Boolean checkIsEntrant() {
//        // roles ArrayList is 3 booleans / 0s & 1s that represent the 3 possible roles (?), with the first entry (index 0) representing Entrant, then Organiser, & so on
//        return roles.get(0);
//    }
//
//    public Boolean checkIsOrganizer() {
//        // roles ArrayList is 3 booleans / 0s & 1s that represent the 3 possible roles (?), with the first entry (index 0) representing Entrant, then Organiser, & so on
//        return roles[1];
//    }
//
//    public Boolean checkIsAdmin() {
//        // roles ArrayList is 3 booleans / 0s & 1s that represent the 3 possible roles (?), with the first entry (index 0) representing Entrant, then Organiser, & so on
//        return roles[2];
//    }

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