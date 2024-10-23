package ca.yapper.yapperapp;

import java.util.Date;

public class Event {
    private Facility facility;
    private Date date;
    private Date registrationDeadline;
    private Poster poster;
    private int wlSeatsAvailable;

    public Event(Facility facility, Date date, Date registrationDeadline, Poster poster, int wlSeatsAvailable) {
        this.facility = facility;
        this.date = date;
        this.registrationDeadline = registrationDeadline;
        this.poster = poster;
        this.wlSeatsAvailable = wlSeatsAvailable;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(Date registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public int getWlSeatsAvailable() {
        return wlSeatsAvailable;
    }

    public void setWlSeatsAvailable(int wlSeatsAvailable) {
        this.wlSeatsAvailable = wlSeatsAvailable;
    }

    public Poster getPoster() {
        return poster;
    }

    public void setPoster(Poster poster) {
        this.poster = poster;
    }
    //

}
