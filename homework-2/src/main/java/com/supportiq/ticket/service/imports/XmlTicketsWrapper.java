package com.supportiq.ticket.service.imports;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "tickets")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTicketsWrapper {

    @XmlElement(name = "ticket")
    private List<XmlTicketRecord> tickets;

    public List<XmlTicketRecord> getTickets() { return tickets; }
    public void setTickets(List<XmlTicketRecord> tickets) { this.tickets = tickets; }
}
