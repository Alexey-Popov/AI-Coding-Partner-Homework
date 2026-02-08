import * as fs from 'fs';
import { parseString } from 'xml2js';
import { safeValidateCreateTicket, CreateTicketSchemaType } from '../models/TicketValidator';

export interface XmlImportResult {
  success: boolean;
  imported: number;
  failed: number;
  errors: Array<{ index: number; field: string; message: string }>;
  validTickets: CreateTicketSchemaType[];
}

export class XmlImportService {
  public async importFromFile(filePath: string): Promise<XmlImportResult> {
    const content = fs.readFileSync(filePath, 'utf-8');
    return this.importFromString(content);
  }

  public async importFromString(xmlContent: string): Promise<XmlImportResult> {
    const result: XmlImportResult = {
      success: true,
      imported: 0,
      failed: 0,
      errors: [],
      validTickets: [],
    };

    return new Promise((resolve) => {
      parseString(xmlContent, (err, parsed) => {
        if (err) {
          result.success = false;
          result.errors.push({
            index: 0,
            field: 'xml',
            message: err.message || 'Invalid XML format',
          });
          resolve(result);
          return;
        }

        let tickets: unknown[] = [];

        if (parsed?.tickets?.ticket) {
          tickets = Array.isArray(parsed.tickets.ticket)
            ? parsed.tickets.ticket
            : [parsed.tickets.ticket];
        } else {
          result.success = false;
          result.errors.push({
            index: 0,
            field: 'format',
            message: 'XML must have a root <tickets> element with <ticket> children',
          });
          resolve(result);
          return;
        }

        tickets.forEach((ticketData: any, index) => {
          const normalized = this.normalizeXmlTicket(ticketData);
          const validation = safeValidateCreateTicket(normalized);

          if (!validation.success) {
            result.failed++;
            result.success = false;
            validation.error.issues.forEach((err) => {
              result.errors.push({
                index: index + 1,
                field: err.path.join('.'),
                message: err.message,
              });
            });
          } else {
            result.imported++;
            result.validTickets.push(validation.data);
          }
        });

        resolve(result);
      });
    });
  }

  private normalizeXmlTicket(xmlTicket: any): any {
    const getValue = (val: any): any => {
      if (Array.isArray(val) && val.length === 1) {
        return val[0];
      }
      return val;
    };

    return {
      customer_id: getValue(xmlTicket.customer_id),
      customer_email: getValue(xmlTicket.customer_email),
      customer_name: getValue(xmlTicket.customer_name),
      subject: getValue(xmlTicket.subject),
      description: getValue(xmlTicket.description),
      assigned_to: getValue(xmlTicket.assigned_to) || null,
      tags: xmlTicket.tags?.[0]?.tag || [],
      metadata: {
        source: getValue(xmlTicket.metadata?.[0]?.source),
        browser: getValue(xmlTicket.metadata?.[0]?.browser),
        device_type: getValue(xmlTicket.metadata?.[0]?.device_type),
      },
    };
  }
}
