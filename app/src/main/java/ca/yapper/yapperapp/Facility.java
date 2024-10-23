package ca.yapper.yapperapp;

public class Facility {
    private String facilityName;
    private String location;  // String?
    private FacilityPic facilityPic;

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
