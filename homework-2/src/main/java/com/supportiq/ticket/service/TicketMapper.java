package com.supportiq.ticket.service;

import com.supportiq.ticket.dto.request.CreateTicketRequest;
import com.supportiq.ticket.dto.response.TicketDto;
import com.supportiq.ticket.entity.TicketEntity;
import com.supportiq.ticket.entity.TicketMetadata;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class TicketMapper {

    public TicketEntity toEntity(CreateTicketRequest request) {
        TicketEntity entity = new TicketEntity();
        entity.setCustomerId(request.getCustomerId());
        entity.setCustomerName(request.getCustomerName());
        entity.setCustomerEmail(request.getCustomerEmail());
        entity.setSubject(request.getSubject());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setPriority(request.getPriority());

        TicketMetadata metadata = new TicketMetadata();
        metadata.setSource(request.getSource());
        metadata.setBrowser(request.getBrowser());
        metadata.setDeviceType(request.getDeviceType());
        entity.setMetadata(metadata);

        if (request.getTags() != null) {
            entity.setTags(new HashSet<>(request.getTags()));
        }

        return entity;
    }

    public TicketDto toDto(TicketEntity entity) {
        TicketDto dto = new TicketDto();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setCustomerName(entity.getCustomerName());
        dto.setCustomerEmail(entity.getCustomerEmail());
        dto.setSubject(entity.getSubject());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setPriority(entity.getPriority());
        dto.setStatus(entity.getStatus());
        dto.setTags(entity.getTags());
        dto.setClassificationConfidence(entity.getClassificationConfidence());
        dto.setClassificationReasoning(entity.getClassificationReasoning());
        dto.setClassificationKeywords(entity.getClassificationKeywords());
        dto.setLastClassifiedAt(entity.getLastClassifiedAt());
        dto.setResolvedAt(entity.getResolvedAt());
        dto.setAssignedTo(entity.getAssignedTo());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getMetadata() != null) {
            dto.setSource(entity.getMetadata().getSource());
            dto.setBrowser(entity.getMetadata().getBrowser());
            dto.setDeviceType(entity.getMetadata().getDeviceType());
        }

        return dto;
    }
}
