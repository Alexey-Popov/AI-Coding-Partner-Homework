import { jwtDecode } from 'jwt-decode';

interface JWTPayload {
  sub: string;
  email: string;
  role: string;
  status: string;
  exp: number;
}

class AuthStore {
  private token: string | null = null;

  constructor() {
    this.token = localStorage.getItem('access_token');
  }

  setToken(token: string): void {
    this.token = token;
    localStorage.setItem('access_token', token);
  }

  getToken(): string | null {
    return this.token;
  }

  clearToken(): void {
    this.token = null;
    localStorage.removeItem('access_token');
  }

  getRole(): string | null {
    if (!this.token) return null;

    try {
      const decoded = jwtDecode<JWTPayload>(this.token);
      return decoded.role || null;
    } catch (e) {
      return null;
    }
  }

  getStatus(): string | null {
    if (!this.token) return null;

    try {
      const decoded = jwtDecode<JWTPayload>(this.token);
      return decoded.status || null;
    } catch (e) {
      return null;
    }
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isBlocked(): boolean {
    return this.getStatus() === 'BLOCKED';
  }

  isAuthenticated(): boolean {
    if (!this.token) return false;

    try {
      // Decode JWT and check expiry
      const decoded = jwtDecode<JWTPayload>(this.token);
      const now = Date.now() / 1000;

      // Token expired
      if (decoded.exp < now) {
        console.warn('Token expired, clearing authentication');
        this.clearToken();
        return false;
      }

      // Check if user is blocked
      if (decoded.status === 'BLOCKED') {
        console.warn('User is blocked, clearing authentication');
        this.clearToken();
        return false;
      }

      return true;
    } catch (e) {
      // Invalid token format
      console.error('Invalid token format:', e);
      this.clearToken();
      return false;
    }
  }
}

export const authStore = new AuthStore();
