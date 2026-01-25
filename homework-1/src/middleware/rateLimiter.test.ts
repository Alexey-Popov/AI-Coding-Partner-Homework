import express, { Express } from 'express';
import request from 'supertest';
import { createRateLimiter, resetRateLimiter } from './rateLimiter';

describe('Rate Limiter Middleware', () => {
  let app: Express;

  beforeEach(() => {
    resetRateLimiter();
    app = express();
    app.set('trust proxy', true);
  });

  describe('Basic functionality', () => {
    it('should allow requests under the limit', async () => {
      app.use(createRateLimiter({ maxRequests: 5, windowMs: 60000 }));
      app.get('/test', (_req, res) => res.json({ success: true }));

      for (let i = 0; i < 5; i++) {
        const response = await request(app).get('/test');
        expect(response.status).toBe(200);
      }
    });

    it('should block requests over the limit with 429 status', async () => {
      app.use(createRateLimiter({ maxRequests: 3, windowMs: 60000 }));
      app.get('/test', (_req, res) => res.json({ success: true }));

      for (let i = 0; i < 3; i++) {
        await request(app).get('/test');
      }

      const response = await request(app).get('/test');
      expect(response.status).toBe(429);
      expect(response.body.error).toBe('Too Many Requests');
    });

    it('should return Retry-After header when rate limited', async () => {
      app.use(createRateLimiter({ maxRequests: 1, windowMs: 60000 }));
      app.get('/test', (_req, res) => res.json({ success: true }));

      await request(app).get('/test');
      const response = await request(app).get('/test');

      expect(response.status).toBe(429);
      expect(response.headers['retry-after']).toBeDefined();
      expect(parseInt(response.headers['retry-after'])).toBeGreaterThan(0);
    });

    it('should include retryAfter in response body', async () => {
      app.use(createRateLimiter({ maxRequests: 1, windowMs: 60000 }));
      app.get('/test', (_req, res) => res.json({ success: true }));

      await request(app).get('/test');
      const response = await request(app).get('/test');

      expect(response.body.retryAfter).toBeDefined();
      expect(typeof response.body.retryAfter).toBe('number');
    });
  });

  describe('IP-based limiting', () => {
    it('should track different IPs separately', async () => {
      app.use(createRateLimiter({ maxRequests: 2, windowMs: 60000 }));
      app.get('/test', (_req, res) => res.json({ success: true }));

      await request(app).get('/test').set('X-Forwarded-For', '1.1.1.1');
      await request(app).get('/test').set('X-Forwarded-For', '1.1.1.1');
      const blockedResponse = await request(app).get('/test').set('X-Forwarded-For', '1.1.1.1');
      expect(blockedResponse.status).toBe(429);

      const differentIpResponse = await request(app).get('/test').set('X-Forwarded-For', '2.2.2.2');
      expect(differentIpResponse.status).toBe(200);
    });

    it('should handle X-Forwarded-For with multiple IPs', async () => {
      app.use(createRateLimiter({ maxRequests: 1, windowMs: 60000 }));
      app.get('/test', (_req, res) => res.json({ success: true }));

      await request(app).get('/test').set('X-Forwarded-For', '1.1.1.1, 2.2.2.2, 3.3.3.3');
      const response = await request(app).get('/test').set('X-Forwarded-For', '1.1.1.1, 4.4.4.4');

      expect(response.status).toBe(429);
    });
  });

  describe('Window reset', () => {
    it('should reset count after window expires', async () => {
      jest.useFakeTimers();

      app.use(createRateLimiter({ maxRequests: 1, windowMs: 1000 }));
      app.get('/test', (_req, res) => res.json({ success: true }));

      const firstResponse = await request(app).get('/test');
      expect(firstResponse.status).toBe(200);

      const blockedResponse = await request(app).get('/test');
      expect(blockedResponse.status).toBe(429);

      jest.advanceTimersByTime(1001);

      const newWindowResponse = await request(app).get('/test');
      expect(newWindowResponse.status).toBe(200);

      jest.useRealTimers();
    });
  });

  describe('Default configuration', () => {
    it('should use default 100 requests per minute', async () => {
      app.use(createRateLimiter());
      app.get('/test', (_req, res) => res.json({ success: true }));

      for (let i = 0; i < 100; i++) {
        const response = await request(app).get('/test');
        expect(response.status).toBe(200);
      }

      const blockedResponse = await request(app).get('/test');
      expect(blockedResponse.status).toBe(429);
      expect(blockedResponse.body.message).toContain('100 requests');
    });
  });
});
