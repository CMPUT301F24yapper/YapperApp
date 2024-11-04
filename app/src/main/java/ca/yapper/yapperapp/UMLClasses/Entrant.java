package ca.yapper.yapperapp.UMLClasses;

import java.util.ArrayList;

public class Entrant {
    // list of events attribute (to be used in Homepage fragment!...)
    private String status; // options being 'waiting', 'selected', 'cancelled', 'final'!!
    private ArrayList<Event> eventsSignedUpList;
    // need to instantiate the other types of lists of Events that Entrant can see on homepage
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
