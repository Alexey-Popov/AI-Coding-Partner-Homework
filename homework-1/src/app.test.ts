import request from 'supertest';
import app from './app';

describe('App', () => {
  describe('GET /', () => {
    it('should return API info and available endpoints', async () => {
      const response = await request(app).get('/');

      expect(response.status).toBe(200);
      expect(response.body.message).toBe('Banking Transactions API');
      expect(response.body.version).toBe('1.0.0');
      expect(response.body.endpoints).toBeDefined();
      expect(response.body.endpoints.transactions).toBeDefined();
      expect(response.body.endpoints.accounts).toBeDefined();
    });
  });

  describe('404 Handler', () => {
    it('should return 404 for unknown endpoints', async () => {
      const response = await request(app).get('/unknown-endpoint');

      expect(response.status).toBe(404);
      expect(response.body.error).toBe('Endpoint not found');
    });

    it('should return 404 for unknown POST endpoints', async () => {
      const response = await request(app).post('/unknown');

      expect(response.status).toBe(404);
      expect(response.body.error).toBe('Endpoint not found');
    });
  });

  describe('Error Handling Middleware', () => {
    it('should return 500 when an error is passed to next()', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const response = await request(app).get('/__test__/error');

      expect(response.status).toBe(500);
      expect(response.body.error).toBe('Something went wrong!');
      expect(consoleSpy).toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });
});
