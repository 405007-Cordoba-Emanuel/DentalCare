import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthResponse, EmailAuthRequest, EmailRegisterRequest } from '../../../interfaces/auth/auth-response.interface';
import { BaseAuthService } from './base-auth.service';

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface MessageResponse {
  message: string;
}

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

  forgotPassword(request: ForgotPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/reset-password`, request);
  }
}
