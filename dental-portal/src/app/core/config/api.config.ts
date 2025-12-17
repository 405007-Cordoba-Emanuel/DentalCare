import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiConfig {
  private readonly isProduction = this.checkIfProduction();

  private checkIfProduction(): boolean {
    if (typeof window === 'undefined') {
      return false; // SSR context
    }
    const hostname = window.location.hostname;
    return !hostname.includes('localhost') && !hostname.includes('127.0.0.1');
  }

  // Base URLs
  private get usersServiceBaseUrl(): string {
    return this.isProduction 
      ? '/api/users' 
      : 'http://localhost:8081/api/users';
  }

  private get coreServiceBaseUrl(): string {
    return this.isProduction 
      ? '/api/core' 
      : 'http://localhost:8082/api/core';
  }

  private get wsBaseUrl(): string {
    if (this.isProduction && typeof window !== 'undefined') {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      return `${protocol}//${window.location.host}/ws`;
    }
    return 'http://localhost:8082/ws';
  }

  // Users Service endpoints
  get usersApiUrl(): string {
    return this.usersServiceBaseUrl;
  }

  get usersAuthUrl(): string {
    return `${this.usersServiceBaseUrl}/auth`;
  }

  // Core Service endpoints
  get coreApiUrl(): string {
    return this.coreServiceBaseUrl;
  }

  get coreChatUrl(): string {
    return `${this.coreServiceBaseUrl}/chat`;
  }

  get coreDentistUrl(): string {
    return `${this.coreServiceBaseUrl}/dentist`;
  }

  get corePatientUrl(): string {
    return `${this.coreServiceBaseUrl}/patient`;
  }

  // WebSocket URL
  get webSocketUrl(): string {
    return this.wsBaseUrl;
  }
}

