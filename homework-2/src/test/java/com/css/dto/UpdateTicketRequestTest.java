package com.css.dto;

import com.css.model.DeviceType;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketSource;
import com.css.model.TicketStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateTicketRequestTest {

    // -------------------------------------------------------------------------
    // Default state
    // -------------------------------------------------------------------------

    @Test
    void defaultConstructor_allFieldsAreNull() {
        UpdateTicketRequest request = new UpdateTicketRequest();

        assertThat(request.getCustomerId()).isNull();
        assertThat(request.getCustomerEmail()).isNull();
        assertThat(request.getCustomerName()).isNull();
        assertThat(request.getSubject()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getCategory()).isNull();
        assertThat(request.getPriority()).isNull();
        assertThat(request.getStatus()).isNull();
        assertThat(request.getAssignedTo()).isNull();
        assertThat(request.getTags()).isNull();
        assertThat(request.getMetadata()).isNull();
    }

    // -------------------------------------------------------------------------
    // String fields
    // -------------------------------------------------------------------------

    @Test
    void setCustomerId_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setCustomerId("cust-456");
        assertThat(request.getCustomerId()).isEqualTo("cust-456");
    }

    @Test
    void setCustomerEmail_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setCustomerEmail("updated@example.com");
        assertThat(request.getCustomerEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void setCustomerName_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setCustomerName("Charlie");
        assertThat(request.getCustomerName()).isEqualTo("Charlie");
    }

    @Test
    void setSubject_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setSubject("Updated subject");
        assertThat(request.getSubject()).isEqualTo("Updated subject");
    }

    @Test
    void setDescription_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setDescription("Updated description");
        assertThat(request.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void setAssignedTo_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setAssignedTo("agent-007");
        assertThat(request.getAssignedTo()).isEqualTo("agent-007");
    }

    // -------------------------------------------------------------------------
    // Enum fields
    // -------------------------------------------------------------------------

    @Test
    void setCategory_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setCategory(TicketCategory.TECHNICAL_ISSUE);
        assertThat(request.getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
    }

    @Test
    void setPriority_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setPriority(TicketPriority.HIGH);
        assertThat(request.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void setStatus_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.IN_PROGRESS);
        assertThat(request.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void setStatus_resolved() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setStatus(TicketStatus.RESOLVED);
        assertThat(request.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    // -------------------------------------------------------------------------
    // Collection field
    // -------------------------------------------------------------------------

    @Test
    void setTags_roundTrip() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTags(List.of("tag1", "tag2"));
        assertThat(request.getTags()).containsExactly("tag1", "tag2");
    }

    @Test
    void setTags_null_allowed() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setTags(null);
        assertThat(request.getTags()).isNull();
    }

    // -------------------------------------------------------------------------
    // Metadata field (reuses CreateTicketRequest.MetadataRequest)
    // -------------------------------------------------------------------------

    @Test
    void setMetadata_roundTrip() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        metadata.setSource(TicketSource.CHAT);
        metadata.setBrowser("Safari");
        metadata.setDeviceType(DeviceType.TABLET);

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setMetadata(metadata);

        assertThat(request.getMetadata()).isSameAs(metadata);
        assertThat(request.getMetadata().getSource()).isEqualTo(TicketSource.CHAT);
        assertThat(request.getMetadata().getBrowser()).isEqualTo("Safari");
        assertThat(request.getMetadata().getDeviceType()).isEqualTo(DeviceType.TABLET);
    }

    @Test
    void setMetadata_null_allowed() {
        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setMetadata(null);
        assertThat(request.getMetadata()).isNull();
    }

    // -------------------------------------------------------------------------
    // Full update scenario
    // -------------------------------------------------------------------------

    @Test
    void fullRequest_allFieldsPopulated() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        metadata.setSource(TicketSource.EMAIL);
        metadata.setBrowser("Edge");
        metadata.setDeviceType(DeviceType.DESKTOP);

        UpdateTicketRequest request = new UpdateTicketRequest();
        request.setCustomerId("cust-789");
        request.setCustomerEmail("update@test.com");
        request.setCustomerName("Diana");
        request.setSubject("Follow-up on billing");
        request.setDescription("The issue persists");
        request.setCategory(TicketCategory.BILLING_QUESTION);
        request.setPriority(TicketPriority.HIGH);
        request.setStatus(TicketStatus.IN_PROGRESS);
        request.setAssignedTo("agent-42");
        request.setTags(List.of("billing", "follow-up"));
        request.setMetadata(metadata);

        assertThat(request.getCustomerId()).isEqualTo("cust-789");
        assertThat(request.getCustomerEmail()).isEqualTo("update@test.com");
        assertThat(request.getCustomerName()).isEqualTo("Diana");
        assertThat(request.getSubject()).isEqualTo("Follow-up on billing");
        assertThat(request.getDescription()).isEqualTo("The issue persists");
        assertThat(request.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
        assertThat(request.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(request.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(request.getAssignedTo()).isEqualTo("agent-42");
        assertThat(request.getTags()).containsExactly("billing", "follow-up");
        assertThat(request.getMetadata()).isSameAs(metadata);
    }
}
