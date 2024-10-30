package ca.yapper.yapperapp.UMLClasses;

public class Facility {
    private String facilityName;
    private String location;  // String?
    private FacilityPic facilityPic;
    // deserialize constructor for Firebase
    public Facility() {}
    // constructor version with parameters
    public Facility(String facilityName, String location, FacilityPic facilityPic) {
        this.facilityName = facilityName;
        this.location = location;
        this.facilityPic = facilityPic;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public FacilityPic getFacilityPic() {
        return facilityPic;
    }

    public void setFacilityPic(FacilityPic facilityPic) {
        this.facilityPic = facilityPic;
    }
}
