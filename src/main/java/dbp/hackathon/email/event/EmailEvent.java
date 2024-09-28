package dbp.hackathon.email.event;

import dbp.hackathon.Ticket.Ticket;

public class EmailEvent {

    private final Ticket ticket;

    public EmailEvent(Ticket ticket) {
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return ticket;
    }
}
