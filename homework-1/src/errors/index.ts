import { ValidationError } from '../models';

export class AppError extends Error {
  constructor(
    public statusCode: number,
    message: string
  ) {
    super(message);
    this.name = 'AppError';
  }
}

export class ValidationException extends AppError {
  constructor(public errors: ValidationError[]) {
    super(400, 'Validation failed');
    this.name = 'ValidationException';
  }
}

export class NotFoundException extends AppError {
  constructor(resource: string, id: string) {
    super(404, `${resource} ${id} not found`);
    this.name = 'NotFoundException';
  }
}

export function formatErrorResponse(error: unknown): { statusCode: number; body: object } {
  if (error instanceof ValidationException) {
    return {
      statusCode: error.statusCode,
      body: { error: error.message, details: error.errors }
    };
  }

  if (error instanceof AppError) {
    return {
      statusCode: error.statusCode,
      body: { error: error.message }
    };
  }

  const message = error instanceof Error ? error.message : 'Unknown error';
  return {
    statusCode: 500,
    body: { error: 'Internal server error', message }
  };
}
