package com.supportiq.ticket.dto.response;

import java.util.List;

public class ImportResultDto {

    private int totalRecords;
    private int successful;
    private int failed;
    private List<ImportFailureDto> failures;
    private List<TicketDto> createdTickets;

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getSuccessful() { return successful; }
    public void setSuccessful(int successful) { this.successful = successful; }

    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }

    public List<ImportFailureDto> getFailures() { return failures; }
    public void setFailures(List<ImportFailureDto> failures) { this.failures = failures; }

    public List<TicketDto> getCreatedTickets() { return createdTickets; }
    public void setCreatedTickets(List<TicketDto> createdTickets) { this.createdTickets = createdTickets; }
}
