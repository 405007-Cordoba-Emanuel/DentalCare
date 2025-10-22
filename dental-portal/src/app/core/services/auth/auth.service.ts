import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthResponse, EmailAuthRequest, EmailRegisterRequest } from '../../../interfaces/auth/auth-response.interface';
import { BaseAuthService } from './base-auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService extends BaseAuthService {
  private apiUrl: string = 'http://localhost:8081/api/users/auth';

  login(loginRequest: EmailAuthRequest): Observable<AuthResponse> {
    return this.handleAuthRequest(
      this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest)
    );
  }

  register(registerRequest: EmailRegisterRequest): Observable<AuthResponse> {
    return this.handleAuthRequest(
      this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest)
    );
  }
}
