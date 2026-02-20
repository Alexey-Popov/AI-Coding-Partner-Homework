package com.supportiq.ticket.classification;

import com.supportiq.ticket.enums.TicketCategory;
import com.supportiq.ticket.enums.TicketPriority;
import com.supportiq.ticket.service.classification.ClassificationResult;
import com.supportiq.ticket.service.classification.ClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationServiceTest {

    private ClassificationService classificationService;

    @BeforeEach
    void setUp() {
        classificationService = new ClassificationService();
    }

    @Test
    void classify_accountAccessKeywords_returnsAccountAccess() {
        ClassificationResult result = classificationService.classify(
                "Cannot login", "My password reset is not working and I am locked out of my account");

        assertThat(result.category()).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
        assertThat(result.matchedKeywords()).isNotEmpty();
    }

    @Test
    void classify_technicalIssueKeywords_returnsTechnicalIssue() {
        ClassificationResult result = classificationService.classify(
                "Application error", "The app keeps crashing with a timeout and is very slow");

        assertThat(result.category()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
    }

    @Test
    void classify_billingKeywords_returnsBillingQuestion() {
        ClassificationResult result = classificationService.classify(
                "Invoice problem", "I need a refund for an incorrect charge on my subscription billing");

        assertThat(result.category()).isEqualTo(TicketCategory.BILLING_QUESTION);
    }

    @Test
    void classify_featureRequestKeywords_returnsFeatureRequest() {
        ClassificationResult result = classificationService.classify(
                "Feature suggestion", "I would like you to add a new integration for enhancement");

        assertThat(result.category()).isEqualTo(TicketCategory.FEATURE_REQUEST);
    }

    @Test
    void classify_bugReportKeywords_returnsBugReport() {
        ClassificationResult result = classificationService.classify(
                "Bug found", "There is a defect where data is missing and not displaying correctly. I can reproduce the glitch.");

        assertThat(result.category()).isEqualTo(TicketCategory.BUG_REPORT);
    }

    @Test
    void classify_noKeywords_returnsOther() {
        ClassificationResult result = classificationService.classify(
                "General question", "What are your business hours?");

        assertThat(result.category()).isEqualTo(TicketCategory.OTHER);
        assertThat(result.confidence()).isEqualTo(0.3);
    }

    @Test
    void classify_urgentKeywords_returnsUrgentPriority() {
        ClassificationResult result = classificationService.classify(
                "URGENT: System down", "This is an emergency, production is completely down and we are experiencing critical data loss");

        assertThat(result.priority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void classify_lowPriorityKeywords_returnsLowPriority() {
        ClassificationResult result = classificationService.classify(
                "Minor cosmetic issue", "A minor visual glitch, no rush, just a nice to have when possible");

        assertThat(result.priority()).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void classify_confidenceIncreasesWithKeywords() {
        ClassificationResult fewKeywords = classificationService.classify(
                "Login issue", "Cannot login");

        ClassificationResult manyKeywords = classificationService.classify(
                "Login and password reset", "Cannot login, account locked, authentication fails, credentials wrong, session expired");

        assertThat(manyKeywords.confidence()).isGreaterThanOrEqualTo(fewKeywords.confidence());
    }

    @Test
    void classify_confidenceCappedAtOne() {
        ClassificationResult result = classificationService.classify(
                "Urgent emergency critical login password reset locked account access",
                "Authentication credentials two-factor 2fa mfa logout session sign in blocked cannot work");

        assertThat(result.confidence()).isLessThanOrEqualTo(1.0);
    }

    @Test
    void classify_reasoningContainsCategory() {
        ClassificationResult result = classificationService.classify(
                "Billing issue", "I need a refund for this charge");

        assertThat(result.reasoning()).contains("billing_question");
    }

    @Test
    void classify_defaultPriorityIsMedium() {
        ClassificationResult result = classificationService.classify(
                "Login issue", "Cannot access my account");

        assertThat(result.priority()).isEqualTo(TicketPriority.MEDIUM);
    }
}
