package com.css.dto;

import com.css.model.DeviceType;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import com.css.model.TicketSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTicketRequestTest {

    // -------------------------------------------------------------------------
    // Default state
    // -------------------------------------------------------------------------

    @Test
    void defaultConstructor_allFieldsAreNull() {
        CreateTicketRequest request = new CreateTicketRequest();

        assertThat(request.getCustomerId()).isNull();
        assertThat(request.getCustomerEmail()).isNull();
        assertThat(request.getCustomerName()).isNull();
        assertThat(request.getSubject()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getCategory()).isNull();
        assertThat(request.getPriority()).isNull();
        assertThat(request.getTags()).isNull();
        assertThat(request.getMetadata()).isNull();
        assertThat(request.getAutoClassify()).isNull();
    }

    // -------------------------------------------------------------------------
    // String fields
    // -------------------------------------------------------------------------

    @Test
    void setCustomerId_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("cust-123");
        assertThat(request.getCustomerId()).isEqualTo("cust-123");
    }

    @Test
    void setCustomerEmail_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerEmail("user@example.com");
        assertThat(request.getCustomerEmail()).isEqualTo("user@example.com");
    }

    @Test
    void setCustomerName_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerName("Alice");
        assertThat(request.getCustomerName()).isEqualTo("Alice");
    }

    @Test
    void setSubject_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setSubject("Login issue");
        assertThat(request.getSubject()).isEqualTo("Login issue");
    }

    @Test
    void setDescription_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setDescription("Cannot log in after password reset");
        assertThat(request.getDescription()).isEqualTo("Cannot log in after password reset");
    }

    // -------------------------------------------------------------------------
    // Enum fields
    // -------------------------------------------------------------------------

    @Test
    void setCategory_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCategory(TicketCategory.BILLING_QUESTION);
        assertThat(request.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
    }

    @Test
    void setPriority_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setPriority(TicketPriority.HIGH);
        assertThat(request.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    // -------------------------------------------------------------------------
    // Collection field
    // -------------------------------------------------------------------------

    @Test
    void setTags_roundTrip() {
        CreateTicketRequest request = new CreateTicketRequest();
        List<String> tags = List.of("billing", "urgent");
        request.setTags(tags);
        assertThat(request.getTags()).containsExactly("billing", "urgent");
    }

    @Test
    void setTags_null_allowed() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTags(null);
        assertThat(request.getTags()).isNull();
    }

    // -------------------------------------------------------------------------
    // Boolean field
    // -------------------------------------------------------------------------

    @Test
    void setAutoClassify_true() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setAutoClassify(true);
        assertThat(request.getAutoClassify()).isTrue();
    }

    @Test
    void setAutoClassify_false() {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setAutoClassify(false);
        assertThat(request.getAutoClassify()).isFalse();
    }

    // -------------------------------------------------------------------------
    // MetadataRequest inner class
    // -------------------------------------------------------------------------

    @Test
    void setMetadata_roundTrip() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        CreateTicketRequest request = new CreateTicketRequest();
        request.setMetadata(metadata);
        assertThat(request.getMetadata()).isSameAs(metadata);
    }

    @Test
    void metadataRequest_defaultConstructor_allFieldsAreNull() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        assertThat(metadata.getSource()).isNull();
        assertThat(metadata.getBrowser()).isNull();
        assertThat(metadata.getDeviceType()).isNull();
    }

    @Test
    void metadataRequest_setSource_roundTrip() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        metadata.setSource(TicketSource.EMAIL);
        assertThat(metadata.getSource()).isEqualTo(TicketSource.EMAIL);
    }

    @Test
    void metadataRequest_setBrowser_roundTrip() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        metadata.setBrowser("Chrome");
        assertThat(metadata.getBrowser()).isEqualTo("Chrome");
    }

    @Test
    void metadataRequest_setDeviceType_roundTrip() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        metadata.setDeviceType(DeviceType.MOBILE);
        assertThat(metadata.getDeviceType()).isEqualTo(DeviceType.MOBILE);
    }

    @Test
    void fullRequest_allFieldsPopulated() {
        CreateTicketRequest.MetadataRequest metadata = new CreateTicketRequest.MetadataRequest();
        metadata.setSource(TicketSource.WEB_FORM);
        metadata.setBrowser("Firefox");
        metadata.setDeviceType(DeviceType.DESKTOP);

        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("cust-001");
        request.setCustomerEmail("test@test.com");
        request.setCustomerName("Bob");
        request.setSubject("Payment failed");
        request.setDescription("Tried to pay, got error 500");
        request.setCategory(TicketCategory.BILLING_QUESTION);
        request.setPriority(TicketPriority.HIGH);
        request.setTags(List.of("payment", "error"));
        request.setMetadata(metadata);
        request.setAutoClassify(true);

        assertThat(request.getCustomerId()).isEqualTo("cust-001");
        assertThat(request.getCustomerEmail()).isEqualTo("test@test.com");
        assertThat(request.getCustomerName()).isEqualTo("Bob");
        assertThat(request.getSubject()).isEqualTo("Payment failed");
        assertThat(request.getDescription()).isEqualTo("Tried to pay, got error 500");
        assertThat(request.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
        assertThat(request.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(request.getTags()).containsExactly("payment", "error");
        assertThat(request.getMetadata()).isSameAs(metadata);
        assertThat(request.getAutoClassify()).isTrue();
    }
}
