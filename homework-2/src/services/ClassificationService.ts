import { TicketCategory, TicketPriority } from '../models/Ticket';

export interface ClassificationResult {
    category: TicketCategory;
    priority: TicketPriority;
    confidence: number;
    reasoning: string;
    keywords: string[];
}

interface KeywordMap {
    [key: string]: string[];
}

export class ClassificationService {
    private readonly categoryKeywords: KeywordMap = {
        account_access: ['login', 'password', '2fa', "can't sign in"],
        technical_issue: ['error', 'bug', 'crash', 'not working'],
        billing_question: ['payment', 'invoice', 'refund', 'charge'],
        feature_request: ['add', 'enhancement', 'suggestion', 'would like'],
        bug_report: ['reproduce', 'steps', 'expected', 'actual'],
    };

    private readonly priorityKeywords: KeywordMap = {
        urgent: ["can't access", 'critical', 'production down', 'security breach'],
        high: ['important', 'blocking', 'asap', 'urgent'],
        low: ['minor', 'cosmetic', 'suggestion', 'typo'],
    };

    /**
     * Classifies a support ticket based on keyword matching
     * @param subject - Ticket subject line
     * @param description - Ticket description text
     * @returns Classification result with category, priority, confidence, reasoning, and matched keywords
     */
    public classify(subject: string, description: string): ClassificationResult {
        const combinedText = `${subject} ${description}`.toLowerCase();

        // Classify category
        const categoryResult = this.matchKeywords(
            combinedText,
            this.categoryKeywords,
            'other' as TicketCategory
        );

        // Classify priority
        const priorityResult = this.matchKeywords(
            combinedText,
            this.priorityKeywords,
            'medium' as TicketPriority
        );

        // Generate reasoning
        const reasoning = this.generateReasoning(
            categoryResult.category as TicketCategory,
            priorityResult.category as TicketPriority,
            categoryResult.matchedKeywords,
            priorityResult.matchedKeywords
        );

        // Calculate overall confidence (average of category and priority confidence)
        const overallConfidence =
            categoryResult.category === 'other'
                ? priorityResult.confidence
                : priorityResult.category === 'medium'
                  ? categoryResult.confidence
                  : (categoryResult.confidence + priorityResult.confidence) / 2;

        // Combine all matched keywords
        const allKeywords = [...categoryResult.matchedKeywords, ...priorityResult.matchedKeywords];

        return {
            category: categoryResult.category as TicketCategory,
            priority: priorityResult.category as TicketPriority,
            confidence: Number(overallConfidence.toFixed(2)),
            reasoning,
            keywords: allKeywords,
        };
    }

    /**
     * Matches keywords in text and calculates confidence
     */
    private matchKeywords(
        text: string,
        keywordMap: KeywordMap,
        defaultValue: string
    ): {
        category: string;
        confidence: number;
        matchedKeywords: string[];
    } {
        let bestMatch = {
            category: defaultValue,
            confidence: 0,
            matchedKeywords: [] as string[],
        };

        for (const [category, keywords] of Object.entries(keywordMap)) {
            const matchedKeywords: string[] = [];

            for (const keyword of keywords) {
                if (text.includes(keyword.toLowerCase())) {
                    matchedKeywords.push(keyword);
                }
            }

            if (matchedKeywords.length > 0) {
                const confidence = matchedKeywords.length / keywords.length;

                if (confidence > bestMatch.confidence) {
                    bestMatch = {
                        category,
                        confidence,
                        matchedKeywords,
                    };
                }
            }
        }

        return bestMatch;
    }

    /**
     * Generates human-readable reasoning for the classification
     */
    private generateReasoning(
        category: TicketCategory,
        priority: TicketPriority,
        categoryKeywords: string[],
        priorityKeywords: string[]
    ): string {
        const parts: string[] = [];

        // Category reasoning
        if (categoryKeywords.length > 0) {
            parts.push(
                `Classified as '${category}' based on keywords: ${categoryKeywords.join(', ')}`
            );
        } else {
            parts.push(`Classified as '${category}' (default, no specific keywords matched)`);
        }

        // Priority reasoning
        if (priorityKeywords.length > 0) {
            parts.push(
                `Priority set to '${priority}' based on keywords: ${priorityKeywords.join(', ')}`
            );
        } else {
            parts.push(`Priority set to '${priority}' (default, no specific keywords matched)`);
        }

        return parts.join('. ') + '.';
    }
}
