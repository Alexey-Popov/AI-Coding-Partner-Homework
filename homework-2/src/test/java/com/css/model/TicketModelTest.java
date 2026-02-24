package com.css.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TicketModelTest {

    // -------------------------------------------------------------------------
    // Ticket – constructor defaults
    // -------------------------------------------------------------------------

    @Test
    void ticket_defaultConstructor_setsId() {
        Ticket ticket = new Ticket();
        assertThat(ticket.getId()).isNotNull();
    }

    @Test
    void ticket_defaultConstructor_eachInstanceHasUniqueId() {
        Ticket t1 = new Ticket();
        Ticket t2 = new Ticket();
        assertThat(t1.getId()).isNotEqualTo(t2.getId());
    }

    @Test
    void ticket_defaultConstructor_setsStatusNew() {
        Ticket ticket = new Ticket();
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NEW);
    }

    @Test
    void ticket_defaultConstructor_setsCreatedAt() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Ticket ticket = new Ticket();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(ticket.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void ticket_defaultConstructor_setsUpdatedAt() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Ticket ticket = new Ticket();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(ticket.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void ticket_defaultConstructor_tagsIsEmptyList() {
        Ticket ticket = new Ticket();
        assertThat(ticket.getTags()).isNotNull().isEmpty();
    }

    @Test
    void ticket_defaultConstructor_classificationKeywordsIsEmptyList() {
        Ticket ticket = new Ticket();
        assertThat(ticket.getClassificationKeywords()).isNotNull().isEmpty();
    }

    @Test
    void ticket_defaultConstructor_optionalFieldsAreNull() {
        Ticket ticket = new Ticket();
        assertThat(ticket.getCustomerId()).isNull();
        assertThat(ticket.getCustomerEmail()).isNull();
        assertThat(ticket.getCustomerName()).isNull();
        assertThat(ticket.getSubject()).isNull();
        assertThat(ticket.getDescription()).isNull();
        assertThat(ticket.getCategory()).isNull();
        assertThat(ticket.getPriority()).isNull();
        assertThat(ticket.getResolvedAt()).isNull();
        assertThat(ticket.getAssignedTo()).isNull();
        assertThat(ticket.getMetadata()).isNull();
        assertThat(ticket.getClassificationConfidence()).isNull();
        assertThat(ticket.getClassificationReasoning()).isNull();
    }

    // -------------------------------------------------------------------------
    // Ticket – getters / setters
    // -------------------------------------------------------------------------

    @Test
    void ticket_setAndGetId() {
        Ticket ticket = new Ticket();
        UUID uuid = UUID.randomUUID();
        ticket.setId(uuid);
        assertThat(ticket.getId()).isEqualTo(uuid);
    }

    @Test
    void ticket_setAndGetCustomerId() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("cust-123");
        assertThat(ticket.getCustomerId()).isEqualTo("cust-123");
    }

    @Test
    void ticket_setAndGetCustomerEmail() {
        Ticket ticket = new Ticket();
        ticket.setCustomerEmail("user@example.com");
        assertThat(ticket.getCustomerEmail()).isEqualTo("user@example.com");
    }

    @Test
    void ticket_setAndGetCustomerName() {
        Ticket ticket = new Ticket();
        ticket.setCustomerName("Jane Doe");
        assertThat(ticket.getCustomerName()).isEqualTo("Jane Doe");
    }

    @Test
    void ticket_setAndGetSubject() {
        Ticket ticket = new Ticket();
        ticket.setSubject("Cannot login");
        assertThat(ticket.getSubject()).isEqualTo("Cannot login");
    }

    @Test
    void ticket_setAndGetDescription() {
        Ticket ticket = new Ticket();
        ticket.setDescription("Detailed description");
        assertThat(ticket.getDescription()).isEqualTo("Detailed description");
    }

    @Test
    void ticket_setAndGetCategory() {
        Ticket ticket = new Ticket();
        ticket.setCategory(TicketCategory.BUG_REPORT);
        assertThat(ticket.getCategory()).isEqualTo(TicketCategory.BUG_REPORT);
    }

    @Test
    void ticket_setAndGetPriority() {
        Ticket ticket = new Ticket();
        ticket.setPriority(TicketPriority.HIGH);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void ticket_setAndGetStatus() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.RESOLVED);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void ticket_setAndGetCreatedAt() {
        Ticket ticket = new Ticket();
        LocalDateTime dt = LocalDateTime.of(2025, 1, 15, 10, 30);
        ticket.setCreatedAt(dt);
        assertThat(ticket.getCreatedAt()).isEqualTo(dt);
    }

    @Test
    void ticket_setAndGetUpdatedAt() {
        Ticket ticket = new Ticket();
        LocalDateTime dt = LocalDateTime.of(2025, 2, 20, 12, 0);
        ticket.setUpdatedAt(dt);
        assertThat(ticket.getUpdatedAt()).isEqualTo(dt);
    }

    @Test
    void ticket_setAndGetResolvedAt() {
        Ticket ticket = new Ticket();
        LocalDateTime dt = LocalDateTime.of(2025, 3, 1, 9, 0);
        ticket.setResolvedAt(dt);
        assertThat(ticket.getResolvedAt()).isEqualTo(dt);
    }

    @Test
    void ticket_setAndGetAssignedTo() {
        Ticket ticket = new Ticket();
        ticket.setAssignedTo("agent-007");
        assertThat(ticket.getAssignedTo()).isEqualTo("agent-007");
    }

    @Test
    void ticket_setAndGetTags() {
        Ticket ticket = new Ticket();
        ticket.setTags(List.of("urgent", "billing"));
        assertThat(ticket.getTags()).containsExactly("urgent", "billing");
    }

    @Test
    void ticket_setAndGetMetadata() {
        Ticket ticket = new Ticket();
        TicketMetadata metadata = new TicketMetadata(TicketSource.WEB_FORM, "Chrome", DeviceType.DESKTOP);
        ticket.setMetadata(metadata);
        assertThat(ticket.getMetadata()).isSameAs(metadata);
    }

    @Test
    void ticket_setAndGetClassificationConfidence() {
        Ticket ticket = new Ticket();
        ticket.setClassificationConfidence(0.95);
        assertThat(ticket.getClassificationConfidence()).isEqualTo(0.95);
    }

    @Test
    void ticket_setAndGetClassificationReasoning() {
        Ticket ticket = new Ticket();
        ticket.setClassificationReasoning("Keywords matched billing");
        assertThat(ticket.getClassificationReasoning()).isEqualTo("Keywords matched billing");
    }

    @Test
    void ticket_setAndGetClassificationKeywords() {
        Ticket ticket = new Ticket();
        ticket.setClassificationKeywords(List.of("billing", "invoice"));
        assertThat(ticket.getClassificationKeywords()).containsExactly("billing", "invoice");
    }

    // -------------------------------------------------------------------------
    // TicketMetadata
    // -------------------------------------------------------------------------

    @Test
    void ticketMetadata_defaultConstructor_allFieldsNull() {
        TicketMetadata meta = new TicketMetadata();
        assertThat(meta.getSource()).isNull();
        assertThat(meta.getBrowser()).isNull();
        assertThat(meta.getDeviceType()).isNull();
    }

    @Test
    void ticketMetadata_parameterizedConstructor_setsAllFields() {
        TicketMetadata meta = new TicketMetadata(TicketSource.EMAIL, "Firefox", DeviceType.MOBILE);
        assertThat(meta.getSource()).isEqualTo(TicketSource.EMAIL);
        assertThat(meta.getBrowser()).isEqualTo("Firefox");
        assertThat(meta.getDeviceType()).isEqualTo(DeviceType.MOBILE);
    }

    @Test
    void ticketMetadata_setAndGetSource() {
        TicketMetadata meta = new TicketMetadata();
        meta.setSource(TicketSource.API);
        assertThat(meta.getSource()).isEqualTo(TicketSource.API);
    }

    @Test
    void ticketMetadata_setAndGetBrowser() {
        TicketMetadata meta = new TicketMetadata();
        meta.setBrowser("Safari");
        assertThat(meta.getBrowser()).isEqualTo("Safari");
    }

    @Test
    void ticketMetadata_setAndGetDeviceType() {
        TicketMetadata meta = new TicketMetadata();
        meta.setDeviceType(DeviceType.TABLET);
        assertThat(meta.getDeviceType()).isEqualTo(DeviceType.TABLET);
    }

    // -------------------------------------------------------------------------
    // DeviceType enum
    // -------------------------------------------------------------------------

    @Test
    void deviceType_getValueReturnsLowerCase() {
        assertThat(DeviceType.DESKTOP.getValue()).isEqualTo("desktop");
        assertThat(DeviceType.MOBILE.getValue()).isEqualTo("mobile");
        assertThat(DeviceType.TABLET.getValue()).isEqualTo("tablet");
    }

    @Test
    void deviceType_fromValue_exactMatch() {
        assertThat(DeviceType.fromValue("desktop")).isEqualTo(DeviceType.DESKTOP);
        assertThat(DeviceType.fromValue("mobile")).isEqualTo(DeviceType.MOBILE);
        assertThat(DeviceType.fromValue("tablet")).isEqualTo(DeviceType.TABLET);
    }

    @Test
    void deviceType_fromValue_caseInsensitive() {
        assertThat(DeviceType.fromValue("DESKTOP")).isEqualTo(DeviceType.DESKTOP);
        assertThat(DeviceType.fromValue("Mobile")).isEqualTo(DeviceType.MOBILE);
        assertThat(DeviceType.fromValue("TABLET")).isEqualTo(DeviceType.TABLET);
    }

    @Test
    void deviceType_fromValue_unknownThrowsException() {
        assertThatThrownBy(() -> DeviceType.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown DeviceType: unknown");
    }

    // -------------------------------------------------------------------------
    // TicketCategory enum
    // -------------------------------------------------------------------------

    @Test
    void ticketCategory_getValueReturnsExpectedString() {
        assertThat(TicketCategory.ACCOUNT_ACCESS.getValue()).isEqualTo("account_access");
        assertThat(TicketCategory.TECHNICAL_ISSUE.getValue()).isEqualTo("technical_issue");
        assertThat(TicketCategory.BILLING_QUESTION.getValue()).isEqualTo("billing_question");
        assertThat(TicketCategory.FEATURE_REQUEST.getValue()).isEqualTo("feature_request");
        assertThat(TicketCategory.BUG_REPORT.getValue()).isEqualTo("bug_report");
        assertThat(TicketCategory.OTHER.getValue()).isEqualTo("other");
    }

    @Test
    void ticketCategory_fromValue_exactMatch() {
        assertThat(TicketCategory.fromValue("account_access")).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
        assertThat(TicketCategory.fromValue("technical_issue")).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
        assertThat(TicketCategory.fromValue("billing_question")).isEqualTo(TicketCategory.BILLING_QUESTION);
        assertThat(TicketCategory.fromValue("feature_request")).isEqualTo(TicketCategory.FEATURE_REQUEST);
        assertThat(TicketCategory.fromValue("bug_report")).isEqualTo(TicketCategory.BUG_REPORT);
        assertThat(TicketCategory.fromValue("other")).isEqualTo(TicketCategory.OTHER);
    }

    @Test
    void ticketCategory_fromValue_caseInsensitive() {
        assertThat(TicketCategory.fromValue("ACCOUNT_ACCESS")).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
        assertThat(TicketCategory.fromValue("Bug_Report")).isEqualTo(TicketCategory.BUG_REPORT);
    }

    @Test
    void ticketCategory_fromValue_unknownThrowsException() {
        assertThatThrownBy(() -> TicketCategory.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown TicketCategory: invalid");
    }

    // -------------------------------------------------------------------------
    // TicketPriority enum
    // -------------------------------------------------------------------------

    @Test
    void ticketPriority_getValueReturnsExpectedString() {
        assertThat(TicketPriority.URGENT.getValue()).isEqualTo("urgent");
        assertThat(TicketPriority.HIGH.getValue()).isEqualTo("high");
        assertThat(TicketPriority.MEDIUM.getValue()).isEqualTo("medium");
        assertThat(TicketPriority.LOW.getValue()).isEqualTo("low");
    }

    @Test
    void ticketPriority_fromValue_exactMatch() {
        assertThat(TicketPriority.fromValue("urgent")).isEqualTo(TicketPriority.URGENT);
        assertThat(TicketPriority.fromValue("high")).isEqualTo(TicketPriority.HIGH);
        assertThat(TicketPriority.fromValue("medium")).isEqualTo(TicketPriority.MEDIUM);
        assertThat(TicketPriority.fromValue("low")).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void ticketPriority_fromValue_caseInsensitive() {
        assertThat(TicketPriority.fromValue("URGENT")).isEqualTo(TicketPriority.URGENT);
        assertThat(TicketPriority.fromValue("High")).isEqualTo(TicketPriority.HIGH);
        assertThat(TicketPriority.fromValue("MEDIUM")).isEqualTo(TicketPriority.MEDIUM);
        assertThat(TicketPriority.fromValue("LOW")).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void ticketPriority_fromValue_unknownThrowsException() {
        assertThatThrownBy(() -> TicketPriority.fromValue("critical"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown TicketPriority: critical");
    }

    // -------------------------------------------------------------------------
    // TicketSource enum
    // -------------------------------------------------------------------------

    @Test
    void ticketSource_getValueReturnsExpectedString() {
        assertThat(TicketSource.WEB_FORM.getValue()).isEqualTo("web_form");
        assertThat(TicketSource.EMAIL.getValue()).isEqualTo("email");
        assertThat(TicketSource.API.getValue()).isEqualTo("api");
        assertThat(TicketSource.CHAT.getValue()).isEqualTo("chat");
        assertThat(TicketSource.PHONE.getValue()).isEqualTo("phone");
    }

    @Test
    void ticketSource_fromValue_exactMatch() {
        assertThat(TicketSource.fromValue("web_form")).isEqualTo(TicketSource.WEB_FORM);
        assertThat(TicketSource.fromValue("email")).isEqualTo(TicketSource.EMAIL);
        assertThat(TicketSource.fromValue("api")).isEqualTo(TicketSource.API);
        assertThat(TicketSource.fromValue("chat")).isEqualTo(TicketSource.CHAT);
        assertThat(TicketSource.fromValue("phone")).isEqualTo(TicketSource.PHONE);
    }

    @Test
    void ticketSource_fromValue_caseInsensitive() {
        assertThat(TicketSource.fromValue("WEB_FORM")).isEqualTo(TicketSource.WEB_FORM);
        assertThat(TicketSource.fromValue("EMAIL")).isEqualTo(TicketSource.EMAIL);
        assertThat(TicketSource.fromValue("API")).isEqualTo(TicketSource.API);
    }

    @Test
    void ticketSource_fromValue_unknownThrowsException() {
        assertThatThrownBy(() -> TicketSource.fromValue("fax"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown TicketSource: fax");
    }

    // -------------------------------------------------------------------------
    // TicketStatus enum
    // -------------------------------------------------------------------------

    @Test
    void ticketStatus_getValueReturnsExpectedString() {
        assertThat(TicketStatus.NEW.getValue()).isEqualTo("new");
        assertThat(TicketStatus.IN_PROGRESS.getValue()).isEqualTo("in_progress");
        assertThat(TicketStatus.WAITING_CUSTOMER.getValue()).isEqualTo("waiting_customer");
        assertThat(TicketStatus.RESOLVED.getValue()).isEqualTo("resolved");
        assertThat(TicketStatus.CLOSED.getValue()).isEqualTo("closed");
    }

    @Test
    void ticketStatus_fromValue_exactMatch() {
        assertThat(TicketStatus.fromValue("new")).isEqualTo(TicketStatus.NEW);
        assertThat(TicketStatus.fromValue("in_progress")).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(TicketStatus.fromValue("waiting_customer")).isEqualTo(TicketStatus.WAITING_CUSTOMER);
        assertThat(TicketStatus.fromValue("resolved")).isEqualTo(TicketStatus.RESOLVED);
        assertThat(TicketStatus.fromValue("closed")).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void ticketStatus_fromValue_caseInsensitive() {
        assertThat(TicketStatus.fromValue("NEW")).isEqualTo(TicketStatus.NEW);
        assertThat(TicketStatus.fromValue("IN_PROGRESS")).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(TicketStatus.fromValue("Resolved")).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void ticketStatus_fromValue_unknownThrowsException() {
        assertThatThrownBy(() -> TicketStatus.fromValue("archived"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown TicketStatus: archived");
    }
}
