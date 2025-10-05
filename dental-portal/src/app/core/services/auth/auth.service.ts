import { Observable } from 'rxjs';
import { AuthResponse, EmailAuthRequest, EmailRegisterRequest } from '../../../interfaces/auth/auth-response.interface';
import { BaseAuthService } from './base-auth.service';

export abstract class AuthService extends BaseAuthService {
  private apiUrl: string = 'http://localhost:8080/api';

  login(loginRequest: EmailAuthRequest): Observable<AuthResponse> {
    return this.handleAuthRequest(
      this.http.post<AuthResponse>(`${this.apiUrl}/login`, loginRequest),
      'Inicio de sesión exitoso'
    );
  }

  register(registerRequest: EmailRegisterRequest): Observable<AuthResponse> {
    return this.handleAuthRequest(
      this.http.post<AuthResponse>(`${this.apiUrl}/register`, registerRequest),
      'Registro exitoso. Bienvenido a Dental Care'
    );
  }
}
