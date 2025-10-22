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

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    // Inicializar usuario desde localStorage al crear el servicio
    this.initializeUserFromStorage();
  }

  private initializeUserFromStorage(): void {
    const token = this.localStorageService.getAuthToken();
    const userDataString = this.localStorageService.getUserData();
    
    if (token && userDataString) {
      try {
        const userData = JSON.parse(userDataString);
        this.currentUserSubject.next(userData);
      } catch (error) {
        console.error('Error parsing user data from localStorage:', error);
      }
    }
  }

  protected handleAuthRequest(
    request$: Observable<AuthResponse>
  ): Observable<AuthResponse> {
    return request$.pipe(
      tap((response) => {
        this.handlerService.handleAuthResponse(response);
        const user: User = {
          id: response.id,
          firstName: response.firstName,
          lastName: response.lastName,
          email: response.email,
          picture: response.picture,
          role: response.role,
          token: response.token,
        };
        this.localStorageService.setAuthToken(response.token);
        this.localStorageService.setUserData(user);
        this.currentUserSubject.next(user);
      }),
      catchError(this.handlerService.handleError)
    );
  }

  // ✅ Métodos compartidos para guards y componentes
  isAuthenticated(): boolean {
    return !!this.localStorageService.getAuthToken();
  }

  get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  logout(): void {
    this.localStorageService.clearLocalStorage();
    this.currentUserSubject.next(null);
  }
}
