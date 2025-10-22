import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthResponse, GoogleAuthRequest } from '../../../interfaces/auth/auth-response.interface';
import { GOOGLE_CONFIG } from '../../config/google.config';
import { BaseAuthService } from './base-auth.service';

@Injectable({
  providedIn: 'root',
})
export class GoogleAuthService extends BaseAuthService {
  private readonly API_URL = 'http://localhost:8081/api/users/auth/google';

  loginWithGoogle(idToken: string): Observable<AuthResponse> {
    const googleAuthRequest: GoogleAuthRequest = { idToken };

    // Debug: Log the request details
    console.log('Making Google auth request to:', `${this.API_URL}/login`);
    console.log('Request payload:', googleAuthRequest);

    return this.handleAuthRequest(
      this.http.post<AuthResponse>(`${this.API_URL}/login`, googleAuthRequest, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
    );
  }
}
