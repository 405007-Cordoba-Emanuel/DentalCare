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

  protected handleAuthRequest(
    request$: Observable<AuthResponse>,
    successMessage: string
  ): Observable<AuthResponse> {
    return request$.pipe(
      tap((response) => {
        this.handlerService.handleAuthResponse(response);
        // actualizar BehaviorSubject con el usuario
        const user: User = {
          id: 0,
          firstName: response.firstName,
          lastName: response.lastName,
          email: response.email,
          profileImage: '',
          picture: response.picture,
          role: response.role,
          token: response.token,
        };
        this.localStorageService.setAuthToken(response.token);
        this.localStorageService.setUserData(user);
        this.currentUserSubject.next(user);
      }),
      tap(() => this.notificationService.success(successMessage)),
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
