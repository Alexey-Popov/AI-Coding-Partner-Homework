package com.css.service;

import com.css.model.Ticket;
import com.css.model.TicketCategory;
import com.css.model.TicketPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TicketClassificationServiceTest {

    private TicketClassificationService service;

    @BeforeEach
    void setUp() {
        service = new TicketClassificationService();
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Ticket ticket(String subject, String description) {
        Ticket t = new Ticket();
        t.setSubject(subject);
        t.setDescription(description);
        return t;
    }

    // -------------------------------------------------------------------------
    // Category classification
    // -------------------------------------------------------------------------

    @Test
    void classify_accountAccessKeywordInSubject_returnsAccountAccessCategory() {
        Ticket t = ticket("I forgot password", "Please help me reset it.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
    }

    @Test
    void classify_loginKeyword_returnsAccountAccessCategory() {
        Ticket t = ticket("Cannot login to the system", "");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
    }

    @Test
    void classify_technicalIssueKeywordInDescription_returnsTechnicalIssueCategory() {
        Ticket t = ticket("App problem", "The application keeps throwing an error and we see a stack trace.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
    }

    @Test
    void classify_crashKeyword_returnsTechnicalIssueCategory() {
        Ticket t = ticket("System crash", "App crashed on startup.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
    }

    @Test
    void classify_billingKeyword_returnsBillingQuestionCategory() {
        Ticket t = ticket("Invoice issue", "I received a wrong invoice with an unexpected charge.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
    }

    @Test
    void classify_refundKeyword_returnsBillingQuestionCategory() {
        Ticket t = ticket("Refund request", "I would like a refund for my payment.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
    }

    @Test
    void classify_featureKeyword_returnsFeatureRequestCategory() {
        Ticket t = ticket("Feature suggestion", "This would be great to have as an improvement.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.FEATURE_REQUEST);
    }

    @Test
    void classify_enhancementKeyword_returnsFeatureRequestCategory() {
        Ticket t = ticket("Enhancement request", "Please add an enhancement to the dashboard.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.FEATURE_REQUEST);
    }

    @Test
    void classify_bugKeyword_returnsBugReportCategory() {
        Ticket t = ticket("Bug found", "There is a defect that I can reproduce.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.BUG_REPORT);
    }

    @Test
    void classify_stepsToReproduceKeyword_returnsBugReportCategory() {
        Ticket t = ticket("Defect report", "Steps to reproduce: open the app, click save.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.BUG_REPORT);
    }

    @Test
    void classify_noKeywordsMatch_returnsOtherCategory() {
        Ticket t = ticket("General inquiry", "I have a general question about the product.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    // -------------------------------------------------------------------------
    // Priority classification
    // -------------------------------------------------------------------------

    @Test
    void classify_urgentKeywordCritical_returnsUrgentPriority() {
        Ticket t = ticket("Critical issue", "This is a critical outage.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void classify_urgentKeywordProductionDown_returnsUrgentPriority() {
        Ticket t = ticket("Production down", "The entire production environment is unavailable.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void classify_urgentKeywordSecurity_returnsUrgentPriority() {
        Ticket t = ticket("Security concern", "There is a security vulnerability.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void classify_urgentKeywordCannotLogin_returnsUrgentPriority() {
        Ticket t = ticket("Cannot login", "I cannot login to my account.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void classify_urgentKeywordCannotAccess_returnsUrgentPriority() {
        Ticket t = ticket("Access denied", "I cannot access my account.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void classify_highKeywordImportant_returnsHighPriority() {
        Ticket t = ticket("Important request", "This is important and needs attention.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void classify_highKeywordBlocking_returnsHighPriority() {
        Ticket t = ticket("Blocking issue", "This is blocking our release.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void classify_highKeywordAsap_returnsHighPriority() {
        Ticket t = ticket("Need help asap", "Please respond asap.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void classify_highKeywordUrgent_returnsHighPriority() {
        Ticket t = ticket("Urgent help needed", "This is really urgent.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void classify_lowKeywordMinor_returnsLowPriority() {
        Ticket t = ticket("Minor problem", "There is a minor cosmetic issue.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void classify_lowKeywordSuggestion_returnsLowPriority() {
        Ticket t = ticket("A suggestion", "Just a small suggestion for the UI.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.LOW);
    }

    @Test
    void classify_noMatchingPriorityKeywords_returnsMediumPriority() {
        Ticket t = ticket("Regular question", "I just have a question about the product.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void classify_urgentTakesPrecedenceOverHighKeywords() {
        // "critical" is urgent; "important" is high — urgent must win
        Ticket t = ticket("Critical and important", "This is critical and important and blocking.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void classify_highTakesPrecedenceOverLowKeywords() {
        // "blocking" is high; "minor" is low — high must win
        Ticket t = ticket("Blocking but minor", "This is blocking but also a minor thing.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    // -------------------------------------------------------------------------
    // Confidence
    // -------------------------------------------------------------------------

    @Test
    void classify_noKeywords_confidenceIsZero() {
        Ticket t = ticket("Hello", "Just checking in.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getConfidence()).isEqualTo(0.0);
    }

    @Test
    void classify_oneKeyword_confidenceIsOneThird() {
        Ticket t = ticket("Invoice question", "I have a question.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getConfidence()).isCloseTo(1.0 / 3.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void classify_threeOrMoreKeywords_confidenceIsCappedAtOne() {
        // "invoice", "payment", "refund", "charge" — 4 billing keywords
        Ticket t = ticket("Invoice and payment", "Need a refund for the charge on my receipt.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getConfidence()).isEqualTo(1.0);
    }

    @Test
    void classify_twoKeywords_confidenceIsTwoThirds() {
        // "error" and "timeout" — 2 technical keywords
        Ticket t = ticket("Error occurred", "We got a timeout during processing.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getConfidence()).isCloseTo(2.0 / 3.0, org.assertj.core.data.Offset.offset(0.001));
    }

    // -------------------------------------------------------------------------
    // Keywords list
    // -------------------------------------------------------------------------

    @Test
    void classify_returnsMatchedKeywordsList() {
        Ticket t = ticket("Login failed", "Password reset needed.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getKeywords()).containsAnyOf("login", "password", "failed");
    }

    @Test
    void classify_keywordsAreDistinct() {
        // "bug" appears in both subject and description — should appear only once
        Ticket t = ticket("Bug report", "This is a bug in the system.");
        ClassificationResult result = service.classify(t);
        long bugCount = result.getKeywords().stream().filter("bug"::equals).count();
        assertThat(bugCount).isEqualTo(1);
    }

    @Test
    void classify_noKeywordsMatch_emptyKeywordsList() {
        Ticket t = ticket("Hello", "General question here.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getKeywords()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Reasoning
    // -------------------------------------------------------------------------

    @Test
    void classify_reasoningContainsCategoryAndPriority() {
        Ticket t = ticket("Invoice billing", "Need a refund.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getReasoning())
                .containsIgnoringCase("BILLING_QUESTION")
                .containsIgnoringCase("Matched keywords");
    }

    @Test
    void classify_noKeywords_reasoningContainsNone() {
        Ticket t = ticket("Hello", "Just a question.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getReasoning()).contains("none");
    }

    @Test
    void classify_reasoningContainsConfidenceValue() {
        Ticket t = ticket("Invoice query", "I have a billing question.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getReasoning()).contains("Confidence:");
    }

    // -------------------------------------------------------------------------
    // Null / edge cases
    // -------------------------------------------------------------------------

    @Test
    void classify_nullSubject_doesNotThrow() {
        Ticket t = ticket(null, "The application has an error.");
        ClassificationResult result = service.classify(t);
        assertThat(result).isNotNull();
        assertThat(result.getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
    }

    @Test
    void classify_nullDescription_doesNotThrow() {
        Ticket t = ticket("Invoice problem", null);
        ClassificationResult result = service.classify(t);
        assertThat(result).isNotNull();
        assertThat(result.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
    }

    @Test
    void classify_bothNullSubjectAndDescription_returnsOtherWithZeroConfidence() {
        Ticket t = ticket(null, null);
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.OTHER);
        assertThat(result.getConfidence()).isEqualTo(0.0);
        assertThat(result.getKeywords()).isEmpty();
    }

    @Test
    void classify_emptySubjectAndDescription_returnsOtherWithZeroConfidence() {
        Ticket t = ticket("", "");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.OTHER);
        assertThat(result.getConfidence()).isEqualTo(0.0);
    }

    @Test
    void classify_keywordsMatchedCaseInsensitively() {
        Ticket t = ticket("INVOICE PROBLEM", "Please help with my BILLING.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
    }

    @Test
    void classify_keywordInSubjectAndDescription_bothConsidered() {
        // "error" in description, "timeout" in subject — both should be matched
        Ticket t = ticket("Request timeout issue", "We received an error response.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getKeywords()).containsAnyOf("timeout", "error");
    }

    // -------------------------------------------------------------------------
    // Category disambiguation: highest match count wins
    // -------------------------------------------------------------------------

    @Test
    void classify_multipleCategoryMatches_highestCountWins() {
        // "error", "crash", "timeout" → 3 technical keywords vs "bug" → 1 bug keyword
        Ticket t = ticket("Error and crash", "We see a timeout. This is a bug too.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
    }

    // -------------------------------------------------------------------------
    // Parameterized: urgent priority keywords
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "can't access",
            "critical",
            "production down",
            "security",
            "cannot access",
            "cannot login"
    })
    void classify_urgentKeyword_returnsUrgentPriority(String keyword) {
        Ticket t = ticket("Issue", "Problem: " + keyword + " right now.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority())
                .as("Expected URGENT priority for keyword: %s", keyword)
                .isEqualTo(TicketPriority.URGENT);
    }

    // -------------------------------------------------------------------------
    // Parameterized: high priority keywords
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({"important", "blocking", "asap", "urgent"})
    void classify_highPriorityKeyword_returnsHighPriority(String keyword) {
        Ticket t = ticket("Issue", "This is " + keyword + " for our team.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority())
                .as("Expected HIGH priority for keyword: %s", keyword)
                .isEqualTo(TicketPriority.HIGH);
    }

    // -------------------------------------------------------------------------
    // Parameterized: low priority keywords
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({"minor", "cosmetic", "suggestion"})
    void classify_lowPriorityKeyword_returnsLowPriority(String keyword) {
        Ticket t = ticket("Small thing", "This is a " + keyword + " change.");
        ClassificationResult result = service.classify(t);
        assertThat(result.getPriority())
                .as("Expected LOW priority for keyword: %s", keyword)
                .isEqualTo(TicketPriority.LOW);
    }
}
