import {
  AppError,
  ValidationException,
  NotFoundException,
  formatErrorResponse
} from './index';

describe('Errors', () => {
  describe('AppError', () => {
    it('should create error with status code and message', () => {
      const error = new AppError(400, 'Bad request');

      expect(error.statusCode).toBe(400);
      expect(error.message).toBe('Bad request');
      expect(error.name).toBe('AppError');
    });
  });

  describe('ValidationException', () => {
    it('should create error with validation errors', () => {
      const errors = [{ field: 'amount', message: 'Required' }];
      const error = new ValidationException(errors);

      expect(error.statusCode).toBe(400);
      expect(error.message).toBe('Validation failed');
      expect(error.errors).toEqual(errors);
      expect(error.name).toBe('ValidationException');
    });
  });

  describe('NotFoundException', () => {
    it('should create error with resource and id', () => {
      const error = new NotFoundException('Transaction', '123');

      expect(error.statusCode).toBe(404);
      expect(error.message).toBe('Transaction 123 not found');
      expect(error.name).toBe('NotFoundException');
    });
  });

  describe('formatErrorResponse', () => {
    it('should format ValidationException', () => {
      const errors = [{ field: 'amount', message: 'Required' }];
      const error = new ValidationException(errors);

      const result = formatErrorResponse(error);

      expect(result.statusCode).toBe(400);
      expect(result.body).toEqual({
        error: 'Validation failed',
        details: errors
      });
    });

    it('should format AppError', () => {
      const error = new AppError(403, 'Forbidden');

      const result = formatErrorResponse(error);

      expect(result.statusCode).toBe(403);
      expect(result.body).toEqual({ error: 'Forbidden' });
    });

    it('should format NotFoundException', () => {
      const error = new NotFoundException('Account', 'ACC001');

      const result = formatErrorResponse(error);

      expect(result.statusCode).toBe(404);
      expect(result.body).toEqual({ error: 'Account ACC001 not found' });
    });

    it('should format standard Error', () => {
      const error = new Error('Something went wrong');

      const result = formatErrorResponse(error);

      expect(result.statusCode).toBe(500);
      expect(result.body).toEqual({
        error: 'Internal server error',
        message: 'Something went wrong'
      });
    });

    it('should format unknown error', () => {
      const result = formatErrorResponse('string error');

      expect(result.statusCode).toBe(500);
      expect(result.body).toEqual({
        error: 'Internal server error',
        message: 'Unknown error'
      });
    });

    it('should format null error', () => {
      const result = formatErrorResponse(null);

      expect(result.statusCode).toBe(500);
      expect(result.body).toEqual({
        error: 'Internal server error',
        message: 'Unknown error'
      });
    });
  });
});
