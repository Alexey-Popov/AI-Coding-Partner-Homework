import { Response } from 'express';

export function sendSuccess<T>(
    res: Response,
    data: T,
    additionalData?: Record<string, unknown>
): void {
    res.status(200).json({
        success: true,
        data,
        ...additionalData,
    });
}

export function sendCreated<T>(
    res: Response,
    data: T,
    additionalData?: Record<string, unknown>
): void {
    res.status(201).json({
        success: true,
        data,
        ...additionalData,
    });
}

export function sendNotFound(res: Response, resourceId: string): void {
    res.status(404).json({
        success: false,
        error: 'Ticket not found',
        details: { id: resourceId },
    });
}

export function sendDeleted(res: Response, id: string): void {
    res.status(200).json({
        success: true,
        message: 'Ticket deleted successfully',
        data: { id },
    });
}
