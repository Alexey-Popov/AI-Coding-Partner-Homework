package com.supportiq.ticket.service.imports;

import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.dto.response.TicketDto;
import com.supportiq.ticket.service.TicketService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ImportTransactionHelper {

    private final TicketService ticketService;

    public ImportTransactionHelper(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TicketDto saveTicket(CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }
}
