import { Transaction } from '../models/transaction';

/**
 * CSV Exporter utility for transactions
 */
class CSVExporter {
  /**
   * Convert transactions to CSV format
   */
  exportTransactionsToCSV(transactions: Transaction[]): string {
    // Define CSV headers
    const headers = [
      'id',
      'fromAccount',
      'toAccount',
      'amount',
      'currency',
      'type',
      'timestamp',
      'status'
    ];

    // Create CSV header row
    const csvRows: string[] = [headers.join(',')];

    // Add data rows
    transactions.forEach(transaction => {
      const row = [
        this.escapeCSVField(transaction.id),
        this.escapeCSVField(transaction.fromAccount),
        this.escapeCSVField(transaction.toAccount),
        transaction.amount.toString(),
        this.escapeCSVField(transaction.currency),
        this.escapeCSVField(transaction.type),
        this.escapeCSVField(transaction.timestamp),
        this.escapeCSVField(transaction.status)
      ];
      csvRows.push(row.join(','));
    });

    return csvRows.join('\n');
  }

  /**
   * Escape special characters in CSV fields
   */
  private escapeCSVField(field: string): string {
    // If field contains comma, quote, or newline, wrap in quotes and escape quotes
    if (field.includes(',') || field.includes('"') || field.includes('\n')) {
      return `"${field.replace(/"/g, '""')}"`;
    }
    return field;
  }

  /**
   * Generate CSV filename with timestamp
   */
  generateFilename(): string {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    return `transactions-${timestamp}.csv`;
  }
}

export const csvExporter = new CSVExporter();

