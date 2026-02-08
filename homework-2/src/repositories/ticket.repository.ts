import { Ticket, TicketStatus, TicketPriority, TicketCategory } from '../models/Ticket';

export interface TicketFilters {
    status?: TicketStatus;
    priority?: TicketPriority;
    category?: TicketCategory;
    limit?: number;
    offset?: number;
}

export class TicketRepository {
    private tickets: Ticket[] = [];

    findAll(): Ticket[] {
        return [...this.tickets];
    }

    findById(id: string): Ticket | undefined {
        return this.tickets.find((ticket) => ticket.id === id);
    }

    findWithFilters(filters: TicketFilters): Ticket[] {
        let result = [...this.tickets];

        if (filters.status) {
            result = result.filter((ticket) => ticket.status === filters.status);
        }

        if (filters.priority) {
            result = result.filter((ticket) => ticket.priority === filters.priority);
        }

        if (filters.category) {
            result = result.filter((ticket) => ticket.category === filters.category);
        }

        const offset = filters.offset ?? 0;
        const limit = filters.limit;

        if (limit !== undefined) {
            result = result.slice(offset, offset + limit);
        } else if (offset > 0) {
            result = result.slice(offset);
        }

        return result;
    }

    create(ticket: Ticket): Ticket {
        this.tickets.push(ticket);
        return ticket;
    }

    update(id: string, updatedTicket: Ticket): Ticket | null {
        const index = this.tickets.findIndex((ticket) => ticket.id === id);
        if (index === -1) {
            return null;
        }
        this.tickets[index] = updatedTicket;
        return updatedTicket;
    }

    delete(id: string): Ticket | null {
        const index = this.tickets.findIndex((ticket) => ticket.id === id);
        if (index === -1) {
            return null;
        }
        const deletedTicket = this.tickets.splice(index, 1)[0];
        return deletedTicket ?? null;
    }

    count(): number {
        return this.tickets.length;
    }

    addBulk(tickets: Ticket[]): void {
        this.tickets.push(...tickets);
    }
}
