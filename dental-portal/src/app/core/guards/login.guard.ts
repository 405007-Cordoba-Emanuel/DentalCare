import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { LocalStorageService } from '../services/auth/local-storage.service';

export const loginGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const localStorageService = inject(LocalStorageService);

  const isAuthenticated = authService.isAuthenticated();

  // Si está autenticado, redirigir según el rol
  if (isAuthenticated) {
    let currentUser = authService.currentUser;
    let userRole = currentUser?.role;
    
    // Si currentUser es null o no tiene role, leer desde localStorage
    if (!currentUser || !userRole) {
      const userDataString = localStorageService.getUserData();
      if (userDataString) {
        try {
          currentUser = JSON.parse(userDataString);
          userRole = currentUser?.role;
        } catch (error) {
          console.error('Error parsing user data from localStorage:', error);
        }
      }
    }
    
    console.log('User already authenticated, redirecting based on role:', userRole);
    
    // Si aún no hay role, no redirigir para evitar bucles infinitos
    if (!userRole) {
      console.warn('User authenticated but role is undefined, allowing access to login');
      return true;
    }
    
    switch (userRole) {
      case 'DENTIST':
        router.navigate(['/dentist']);
        return false;
      case 'PATIENT':
        router.navigate(['/patient']);
        return false;
      case 'ADMIN':
        router.navigate(['/dentist']); // Por defecto al dashboard de dentista
        return false;
      default:
        router.navigate(['/patient']); // Por defecto al dashboard de paciente
        return false;
    }
  }

  return true;
};
