package ca.yapper.yapperapp.UMLClasses;

import java.util.ArrayList;

// IMPORTANT:
// In your Activity or Fragment (for EventList stuff), set up the RecyclerView and link it with the adapter (see EventAdapter).
public class EventList {
    private ArrayList<Entrant> waitingList;
    private ArrayList<Entrant> selectedList;
    private ArrayList<Entrant> cancelledList;
    private ArrayList<Entrant> finalList;

    public EventList(ArrayList<Entrant> waitingList, ArrayList<Entrant> selectedList, ArrayList<Entrant> cancelledList, ArrayList<Entrant> finalList) {
        this.waitingList = waitingList;
        this.selectedList = selectedList;
        this.cancelledList = cancelledList;
        this.finalList = finalList;
    }

    public ArrayList<Entrant> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<Entrant> waitingList) {
        this.waitingList = waitingList;
    }

    public ArrayList<Entrant> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(ArrayList<Entrant> selectedList) {
        this.selectedList = selectedList;
    }

    public ArrayList<Entrant> getCancelledList() {
        return cancelledList;
    }

    public void setCancelledList(ArrayList<Entrant> cancelledList) {
        this.cancelledList = cancelledList;
    }

    public ArrayList<Entrant> getFinalList() {
        return finalList;
    }

    public void setFinalList(ArrayList<Entrant> finalList) {
        this.finalList = finalList;
    }
}
