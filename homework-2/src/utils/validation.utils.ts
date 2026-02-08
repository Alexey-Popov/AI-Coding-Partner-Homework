import { validate as isValidUUID } from 'uuid';
import { Response } from 'express';

export function validateUUID(id: string, res: Response): boolean {
    if (!isValidUUID(id)) {
        res.status(400).json({
            success: false,
            error: 'Invalid ticket ID format',
            details: { field: 'id', message: 'Must be a valid UUID' },
        });
        return false;
    }
    return true;
}

export function handleValidationError(res: Response, errors: any[]): void {
    res.status(400).json({
        success: false,
        error: 'Validation failed',
        details: errors.map((err: any) => ({
            field: err.path.join('.'),
            message: err.message,
        })),
    });
}
