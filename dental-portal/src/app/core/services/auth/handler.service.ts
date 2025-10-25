import { inject, Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, throwError } from 'rxjs';
import { AuthResponse } from '../../../interfaces/auth/auth-response.interface';
import { User } from '../../../interfaces/user/user.interface';
import { LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root',
})
export class HandlerService {
  private localStorageService = inject(LocalStorageService);
  private currentUserSubject = new BehaviorSubject<User | null>(null);

  handleError(error: HttpErrorResponse) {
    let errorMessage = 'Ha ocurrido un error';

    if (error.error instanceof ErrorEvent) {
      // Error del lado del cliente
      errorMessage = error.error.message;
    } else {
      // Error del lado del servidor
      switch (error.status) {
        case 400:
          errorMessage = error.error.message || 'Datos de entrada inválidos';
          break;
        case 401:
          errorMessage = 'Credenciales inválidas';
          break;
        case 409:
          errorMessage = 'El usuario ya existe con este email';
          break;
        case 422:
          errorMessage =
            error.error.message || 'Datos de validación incorrectos';
          break;
        case 500:
          errorMessage = 'Error interno del servidor';
          break;
        default:
          errorMessage = error.error.message || 'Error del servidor';
      }
    }

    return throwError(() => new Error(errorMessage));
  }

  handleAuthResponse(response: AuthResponse): void {
    const user: User = {
      id: response.id,
      firstName: response.firstName,
      lastName: response.lastName,
      email: response.email,
      picture: response.picture,
      role: response.role,
      token: response.token,
      dentistId: response.dentistId,
      patientId: response.patientId,
    };

    this.localStorageService.setAuthToken(response.token);
    this.localStorageService.setUserData(user);
    this.currentUserSubject.next(user);
  }
}
