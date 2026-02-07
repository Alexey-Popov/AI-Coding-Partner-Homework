package com.supportiq.ticket.service;

import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.dto.request.UpdateTicketRequest;
import com.supportiq.ticket.dto.response.ClassificationResultDto;
import com.supportiq.ticket.dto.response.TicketDto;
import com.supportiq.ticket.entity.TicketEntity;
import com.supportiq.ticket.entity.TicketMetadata;
import com.supportiq.ticket.enums.TicketCategory;
import com.supportiq.ticket.enums.TicketPriority;
import com.supportiq.ticket.enums.TicketStatus;
import com.supportiq.ticket.exception.TicketNotFoundException;
import com.supportiq.ticket.repository.TicketRepository;
import com.supportiq.ticket.repository.TicketSpecifications;
import com.supportiq.ticket.service.classification.ClassificationResult;
import com.supportiq.ticket.service.classification.ClassificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final ClassificationService classificationService;

    public TicketService(TicketRepository ticketRepository,
                         TicketMapper ticketMapper,
                         ClassificationService classificationService) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.classificationService = classificationService;
    }

    public TicketDto createTicket(CreateTicketRequest request) {
        TicketEntity entity = ticketMapper.toEntity(request);
        entity = ticketRepository.save(entity);
        return ticketMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public TicketDto getTicket(UUID id) {
        TicketEntity entity = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        return ticketMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<TicketDto> listTickets(TicketCategory category, TicketPriority priority,
                                        TicketStatus status, String email,
                                        Instant createdAfter, Instant createdBefore,
                                        String tag, String assignedTo, Pageable pageable) {
        Specification<TicketEntity> spec = Specification
                .where(TicketSpecifications.hasCategory(category))
                .and(TicketSpecifications.hasPriority(priority))
                .and(TicketSpecifications.hasStatus(status))
                .and(TicketSpecifications.hasCustomerEmail(email))
                .and(TicketSpecifications.createdAfter(createdAfter))
                .and(TicketSpecifications.createdBefore(createdBefore))
                .and(TicketSpecifications.hasTag(tag))
                .and(TicketSpecifications.hasAssignedTo(assignedTo));

        return ticketRepository.findAll(spec, pageable).map(ticketMapper::toDto);
    }

    public TicketDto updateTicket(UUID id, UpdateTicketRequest request) {
        TicketEntity entity = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (request.getCustomerId() != null) entity.setCustomerId(request.getCustomerId());
        if (request.getCustomerName() != null) entity.setCustomerName(request.getCustomerName());
        if (request.getCustomerEmail() != null) entity.setCustomerEmail(request.getCustomerEmail());
        if (request.getSubject() != null) entity.setSubject(request.getSubject());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getCategory() != null) entity.setCategory(request.getCategory());
        if (request.getPriority() != null) entity.setPriority(request.getPriority());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getAssignedTo() != null) entity.setAssignedTo(request.getAssignedTo());
        if (request.getResolvedAt() != null) entity.setResolvedAt(request.getResolvedAt());
        if (request.getTags() != null) entity.setTags(new HashSet<>(request.getTags()));

        if (request.getSource() != null || request.getBrowser() != null || request.getDeviceType() != null) {
            TicketMetadata metadata = entity.getMetadata();
            if (metadata == null) metadata = new TicketMetadata();
            if (request.getSource() != null) metadata.setSource(request.getSource());
            if (request.getBrowser() != null) metadata.setBrowser(request.getBrowser());
            if (request.getDeviceType() != null) metadata.setDeviceType(request.getDeviceType());
            entity.setMetadata(metadata);
        }

        entity = ticketRepository.save(entity);
        return ticketMapper.toDto(entity);
    }

    public void deleteTicket(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException(id);
        }
        ticketRepository.deleteById(id);
    }

    public ClassificationResultDto classifyTicket(UUID id) {
        TicketEntity entity = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        ClassificationResult result = classificationService.classify(entity.getSubject(), entity.getDescription());

        entity.setCategory(result.category());
        entity.setPriority(result.priority());
        entity.setClassificationConfidence(result.confidence());
        entity.setClassificationReasoning(result.reasoning());
        entity.setClassificationKeywords(new ArrayList<>(result.matchedKeywords()));
        entity.setLastClassifiedAt(Instant.now());
        ticketRepository.save(entity);

        ClassificationResultDto dto = new ClassificationResultDto();
        dto.setCategory(result.category());
        dto.setPriority(result.priority());
        dto.setConfidence(result.confidence());
        dto.setReasoning(result.reasoning());
        dto.setKeywords(result.matchedKeywords());
        return dto;
    }
}
