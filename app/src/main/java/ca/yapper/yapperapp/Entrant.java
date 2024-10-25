package ca.yapper.yapperapp;

import java.util.ArrayList;

public class Entrant extends Role {
    // list of events attribute (to be used in Homepage fragment!...)
    private ArrayList<Event> eventsList;
    private Boolean notifOn;
    // other methods (specific to Entrant) beyond Gs & Ss: can be moved around in package but for now I am placing them here
    public void joinWaitingList(Event event) {
        // method logic
    }
    public void unJoinWaitingList(Event event) {
        // method logic
    }
    public void acceptInvite(Event event) {
        // method logic
    }
    public void declineInvite(Event event) {
        // method logic
    }
//    public Event scanQRCode(qrCode qrCode) {
//        // method logic
//    }
    //  // other methods (specific to Event) beyond Gs & Ss: can be moved around in package but for now I am placing them here
    public void addEntrantToEvent(Entrant entrant) {
        // adds Entrant to eventsList
    }
}
