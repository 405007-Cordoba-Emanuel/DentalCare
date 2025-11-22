import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { AuthResponse } from '../../../interfaces/auth/auth-response.interface';
import { User } from '../../../interfaces/user/user.interface';
import { NotificationService } from '../notifications/notification.service';
import { HandlerService } from './handler.service';
import { LocalStorageService } from './local-storage.service';

export abstract class BaseAuthService {
  protected http = inject(HttpClient);
  protected notificationService = inject(NotificationService);
  protected handlerService = inject(HandlerService);
  protected localStorageService = inject(LocalStorageService);

  // Usar el observable compartido del HandlerService
  public currentUser$ = this.handlerService.currentUser$;

  constructor() {
    // El HandlerService ya inicializa el usuario desde localStorage
  }

  protected handleAuthRequest(
    request$: Observable<AuthResponse>
  ): Observable<AuthResponse> {
    return request$.pipe(
      tap((response) => {
        // handleAuthResponse ya actualiza el currentUserSubject compartido
        this.handlerService.handleAuthResponse(response);
      }),
      catchError(this.handlerService.handleError)
    );
  }

  // ✅ Métodos compartidos para guards y componentes
  isAuthenticated(): boolean {
    return !!this.localStorageService.getAuthToken();
  }

  get currentUser(): User | null {
    // Obtener el valor actual del HandlerService
    return this.handlerService.getCurrentUserValue();
  }

  logout(): void {
    this.localStorageService.clearLocalStorage();
    // El HandlerService necesita exponer un método para limpiar el usuario
    // Por ahora, actualizamos directamente el localStorage y el observable se actualizará
    this.handlerService.clearCurrentUser();
  }
}
