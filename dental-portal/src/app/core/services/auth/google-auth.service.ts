import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthResponse, GoogleAuthRequest } from '../../../interfaces/auth/auth-response.interface';
import { GOOGLE_CONFIG } from '../../config/google.config';
import { BaseAuthService } from './base-auth.service';
import { ApiConfig } from '../../config/api.config';

@Injectable({
  providedIn: 'root',
})
export class GoogleAuthService extends BaseAuthService {
  private apiConfig = inject(ApiConfig);
  private readonly API_URL = `${this.apiConfig.usersAuthUrl}/google`;

  loginWithGoogle(idToken: string): Observable<AuthResponse> {
    const googleAuthRequest: GoogleAuthRequest = { idToken };

    return this.handleAuthRequest(
      this.http.post<AuthResponse>(`${this.API_URL}/login`, googleAuthRequest, {
        headers: {
          'Content-Type': 'application/json'
        }
      })
    );
  }
}
